package com.codeforces.app.data.repository

import com.codeforces.app.data.api.*
import com.codeforces.app.data.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class Resource<T> {
    class Loading<T> : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}

@Singleton
class CodeforcesRepository @Inject constructor(
    private val api: CodeforcesApiService,
    private val userDao: UserDao,
    private val problemDao: ProblemDao,
    private val contestDao: ContestDao
) {

    companion object {
        private const val USER_TTL_MS = 5 * 60 * 1000L        // 5 minutes
        private const val CONTEST_TTL_MS = 15 * 60 * 1000L    // 15 minutes
        private const val PROBLEMS_TTL_MS = 24 * 60 * 60 * 1000L // 24 hours
        private const val RATED_LIST_TTL_MS = 60 * 60 * 1000L // 1 hour
    }

    private data class RatedListCache(
        val activeOnly: Boolean,
        val timestamp: Long,
        val users: List<UserDto>
    )

    @Volatile
    private var ratedListCache: RatedListCache? = null

    // ── API Rate Limiter ────────────────────────────────────────────────────
    // Codeforces enforces ~1 request per 2 s from the same IP.  A Mutex
    // serialises calls and the timestamp check inserts the minimum gap.
    private val apiMutex = Mutex()
    @Volatile private var lastApiCallMs = 0L
    private val API_MIN_INTERVAL_MS = 2_100L  // slightly over 2 s

    private suspend fun <T> rateLimited(block: suspend () -> T): T = apiMutex.withLock {
        val elapsed = System.currentTimeMillis() - lastApiCallMs
        if (elapsed < API_MIN_INTERVAL_MS) delay(API_MIN_INTERVAL_MS - elapsed)
        try { block() } finally { lastApiCallMs = System.currentTimeMillis() }
    }

    // ── Recent submissions cache (avoids redundant API calls) ────────────
    private data class StatusCache(val handle: String, val from: Int, val count: Int,
                                   val timestamp: Long, val data: List<SubmissionDto>)
    @Volatile private var statusCache: StatusCache? = null
    private val STATUS_TTL_MS = 15_000L  // 15 seconds

    private fun isFresh(timestamp: Long, ttlMs: Long): Boolean =
        System.currentTimeMillis() - timestamp < ttlMs

    // ── Users ──────────────────────────────────────────────────────────────────

    fun getUserInfo(handle: String): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())
        val cached = userDao.getUser(handle)
        // Fresh cache: skip the network entirely.
        if (cached != null && isFresh(cached.cachedAt, USER_TTL_MS)) {
            emit(Resource.Success(cached.toDto()))
            return@flow
        }
        // Stale cache: show it immediately, refresh in the background.
        if (cached != null) emit(Resource.Success(cached.toDto()))
        try {
            val response = rateLimited { api.getUserInfo(handle) }
            if (response.status == "OK" && response.result != null) {
                val user = response.result.first()
                userDao.insertUser(user.toEntity())
                emit(Resource.Success(user))
            } else if (cached == null) {
                emit(Resource.Error(response.comment ?: "Unknown error"))
            }
        } catch (e: Exception) {
            if (cached == null) emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getUserRating(handle: String): Resource<List<RatingChangeDto>> = try {
        rateLimited {
            val response = api.getUserRating(handle)
            if (response.status == "OK") Resource.Success(response.result ?: emptyList())
            else Resource.Error(response.comment ?: "Error")
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getUserStatus(handle: String, from: Int = 1, count: Int = 100): Resource<List<SubmissionDto>> {
        // Serve a fresh cache hit without touching the network.
        statusCache?.let { c ->
            if (c.handle == handle && c.from == from && c.count == count && isFresh(c.timestamp, STATUS_TTL_MS)) {
                return Resource.Success(c.data)
            }
        }
        return try {
            rateLimited {
                val response = api.getUserStatus(handle, from, count)
                if (response.status == "OK") {
                    val data = response.result ?: emptyList()
                    statusCache = StatusCache(handle, from, count, System.currentTimeMillis(), data)
                    Resource.Success(data)
                } else Resource.Error(response.comment ?: "Error")
            }
        } catch (e: Exception) {
            // On network error, return the stale cache if we have one for
            // this handle so the user at least sees *something*.
            statusCache?.takeIf { it.handle == handle }?.let {
                return Resource.Success(it.data)
            }
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    fun getRatedList(activeOnly: Boolean = true): Flow<Resource<List<UserDto>>> = flow {
        emit(Resource.Loading())
        // user.ratedList is a huge payload; cache it in memory so the leaderboard
        // doesn't re-download the entire ranked list on every open.
        ratedListCache?.let { cached ->
            if (cached.activeOnly == activeOnly && isFresh(cached.timestamp, RATED_LIST_TTL_MS)) {
                emit(Resource.Success(cached.users))
                return@flow
            }
        }
        try {
            val response = rateLimited { api.getRatedList(activeOnly) }
            if (response.status == "OK") {
                val users = response.result ?: emptyList()
                ratedListCache = RatedListCache(activeOnly, System.currentTimeMillis(), users)
                emit(Resource.Success(users))
            } else {
                emit(Resource.Error(response.comment ?: "Error"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getUserBlogEntries(handle: String): Resource<List<BlogEntryDto>> = try {
        rateLimited {
            val response = api.getUserBlogEntries(handle)
            if (response.status == "OK") Resource.Success(response.result ?: emptyList())
            else Resource.Error(response.comment ?: "Error")
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    // ── Problems ───────────────────────────────────────────────────────────────

    fun getProblems(tags: String? = null, forceRefresh: Boolean = false): Flow<Resource<ProblemSetResultDto>> = flow {
        emit(Resource.Loading())
        // The full Codeforces problem set is large. Show Room data immediately and
        // only wait on the network when the cache is empty, stale, or refreshed.
        var showedCache = false
        if (tags == null && !forceRefresh) {
            val cachedProblems = problemDao.getAllProblemsOnce()
            if (cachedProblems.isNotEmpty()) {
                val result = withContext(Dispatchers.Default) { cachedProblems.toProblemSetResult() }
                emit(Resource.Success(result))
                showedCache = true
                val cacheAge = System.currentTimeMillis() - (cachedProblems.maxOfOrNull { it.cachedAt } ?: 0L)
                if (cacheAge < PROBLEMS_TTL_MS) return@flow
            }
        }
        try {
            val response = rateLimited { api.getProblems(tags) }
            if (response.status == "OK" && response.result != null) {
                // Cache problems
                if (tags == null) {
                    val entities = withContext(Dispatchers.Default) {
                        val solvedCounts = response.result.problemStatistics.associateBy(
                            keySelector = { it.cacheKey() },
                            valueTransform = { it.solvedCount }
                        )
                        response.result.problems.map { it.toEntity(solvedCounts) }
                    }
                    problemDao.insertProblems(entities)
                }
                emit(Resource.Success(response.result))
            } else if (!showedCache) {
                emit(Resource.Error(response.comment ?: "Error"))
            }
        } catch (e: Exception) {
            if (!showedCache) emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ── Contests ───────────────────────────────────────────────────────────────

    fun getContestList(): Flow<Resource<List<ContestDto>>> = flow {
        emit(Resource.Loading())
        // Home and Problems screens both call this; serve a fresh cache instead of
        // hitting the network twice every launch.
        val cached = contestDao.getAllContestsOnce()
        val cacheAge = cached.maxOfOrNull { it.cachedAt } ?: 0L
        if (cached.isNotEmpty() && isFresh(cacheAge, CONTEST_TTL_MS)) {
            emit(Resource.Success(cached.toContestList()))
            return@flow
        }
        if (cached.isNotEmpty()) emit(Resource.Success(cached.toContestList()))
        try {
            val response = rateLimited { api.getContestList() }
            if (response.status == "OK" && response.result != null) {
                contestDao.insertContests(response.result.map { it.toEntity() })
                emit(Resource.Success(response.result))
            } else if (cached.isEmpty()) {
                emit(Resource.Error(response.comment ?: "Error"))
            }
        } catch (e: Exception) {
            if (cached.isEmpty()) emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getContestStandings(contestId: Int): Resource<StandingsDto> = try {
        rateLimited {
            val response = api.getContestStandings(contestId)
            val result = response.result
            if (response.status == "OK" && result != null) Resource.Success(result)
            else Resource.Error(response.comment ?: "Error")
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getDailyProblem(): ProblemDto? {
        val count = problemDao.count()
        if (count > 0) {
            val daysSinceEpoch = java.time.LocalDate.now().toEpochDay()
            val offset = (daysSinceEpoch % count).toInt()
            return problemDao.getProblemAtOffset(offset)?.toDto()
        }
        try {
            val response = rateLimited { api.getProblems(null) }
            if (response.status == "OK" && response.result != null) {
                val solvedCounts = response.result.problemStatistics.associateBy({ it.cacheKey() }, { it.solvedCount })
                val entities = response.result.problems.map { it.toEntity(solvedCounts) }
                problemDao.insertProblems(entities)
                val daysSinceEpoch = java.time.LocalDate.now().toEpochDay()
                val offset = (daysSinceEpoch % entities.size).toInt()
                return entities[offset].toDto()
            }
        } catch (_: Exception) {
        }
        return null
    }
    suspend fun getContestRatingChanges(contestId: Int): Resource<List<RatingChangeDto>> = try {
        rateLimited {
            val response = api.getContestRatingChanges(contestId)
            if (response.status == "OK") Resource.Success(response.result ?: emptyList())
            else Resource.Error(response.comment ?: "Error")
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    // ── Blog / Recent ──────────────────────────────────────────────────────────

    suspend fun getBlogEntry(blogEntryId: Int): Resource<BlogEntryDto> = try {
        rateLimited {
            val response = api.getBlogEntry(blogEntryId)
            val result = response.result
            if (response.status == "OK" && result != null) Resource.Success(result)
            else Resource.Error(response.comment ?: "Error")
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    fun getRecentActions(maxCount: Int = 30): Flow<Resource<List<RecentActionDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = rateLimited { api.getRecentActions(maxCount) }
            if (response.status == "OK") emit(Resource.Success(response.result ?: emptyList()))
            else emit(Resource.Error(response.comment ?: "Error"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ── Cache Management ───────────────────────────────────────────────────────

    suspend fun clearAllCache() {
        userDao.clearAll()
        problemDao.clearAll()
        contestDao.clearAll()
    }
}

// ── Mapping Extensions ──────────────────────────────────────────────────────

private fun CachedUserEntity.toDto() = UserDto(
    handle = handle, firstName = firstName, lastName = lastName,
    country = country, city = city, organization = organization,
    contribution = contribution, rank = rank, rating = rating,
    maxRank = maxRank, maxRating = maxRating, friendOfCount = friendOfCount,
    avatar = avatar, titlePhoto = titlePhoto,
    lastOnlineTimeSeconds = lastOnlineTimeSeconds,
    registrationTimeSeconds = registrationTimeSeconds
)

private fun UserDto.toEntity() = CachedUserEntity(
    handle = handle, firstName = firstName, lastName = lastName,
    country = country, city = city, organization = organization,
    contribution = contribution, rank = rank, rating = rating,
    maxRank = maxRank, maxRating = maxRating, friendOfCount = friendOfCount,
    avatar = avatar, titlePhoto = titlePhoto,
    lastOnlineTimeSeconds = lastOnlineTimeSeconds,
    registrationTimeSeconds = registrationTimeSeconds
)

private fun ProblemDto.toEntity(solvedCounts: Map<String, Int>): CachedProblemEntity {
    val solvedCount = solvedCounts[cacheKey()] ?: 0
    return CachedProblemEntity(
        id = "${contestId}_${index}",
        contestId = contestId, index = index, name = name,
        rating = rating, tags = tags, solvedCount = solvedCount
    )
}

private fun ProblemDto.cacheKey() = "${contestId}_${index}"

private fun CachedProblemEntity.toDto() = ProblemDto(
    contestId = contestId,
    index = index,
    name = name,
    rating = rating,
    tags = tags
)

private fun ProblemStatisticsDto.cacheKey() = "${contestId}_${index}"

private fun List<CachedProblemEntity>.toProblemSetResult() = ProblemSetResultDto(
    problems = map { cached ->
        ProblemDto(
            contestId = cached.contestId,
            index = cached.index,
            name = cached.name,
            rating = cached.rating,
            tags = cached.tags
        )
    },
    problemStatistics = map { cached ->
        ProblemStatisticsDto(
            contestId = cached.contestId,
            index = cached.index,
            solvedCount = cached.solvedCount
        )
    }
)

private fun ContestDto.toEntity() = CachedContestEntity(
    id = id, name = name, type = type, phase = phase,
    durationSeconds = durationSeconds, startTimeSeconds = startTimeSeconds
)

private fun List<CachedContestEntity>.toContestList() = map {
    ContestDto(
        id = it.id, name = it.name, type = it.type, phase = it.phase,
        durationSeconds = it.durationSeconds, startTimeSeconds = it.startTimeSeconds
    )
}
