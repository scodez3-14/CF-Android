package com.codeforces.app.data.repository

import com.codeforces.app.data.api.*
import com.codeforces.app.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    // ── Users ──────────────────────────────────────────────────────────────────

    fun getUserInfo(handle: String): Flow<Resource<UserDto>> = flow {
        emit(Resource.Loading())
        // Try cache first
        userDao.getUser(handle)?.let { cached ->
            emit(Resource.Success(cached.toDto()))
        }
        // Fetch fresh from API
        try {
            val response = api.getUserInfo(handle)
            if (response.status == "OK" && response.result != null) {
                val user = response.result.first()
                userDao.insertUser(user.toEntity())
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error(response.comment ?: "Unknown error"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getUserRating(handle: String): Resource<List<RatingChangeDto>> = try {
        val response = api.getUserRating(handle)
        if (response.status == "OK") Resource.Success(response.result ?: emptyList())
        else Resource.Error(response.comment ?: "Error")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getUserStatus(handle: String, from: Int = 1, count: Int = 100): Resource<List<SubmissionDto>> = try {
        val response = api.getUserStatus(handle, from, count)
        if (response.status == "OK") Resource.Success(response.result ?: emptyList())
        else Resource.Error(response.comment ?: "Error")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    fun getRatedList(activeOnly: Boolean = true): Flow<Resource<List<UserDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getRatedList(activeOnly)
            if (response.status == "OK") emit(Resource.Success(response.result ?: emptyList()))
            else emit(Resource.Error(response.comment ?: "Error"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getUserBlogEntries(handle: String): Resource<List<BlogEntryDto>> = try {
        val response = api.getUserBlogEntries(handle)
        if (response.status == "OK") Resource.Success(response.result ?: emptyList())
        else Resource.Error(response.comment ?: "Error")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    // ── Problems ───────────────────────────────────────────────────────────────

    fun getProblems(tags: String? = null): Flow<Resource<ProblemSetResultDto>> = flow {
        emit(Resource.Loading())
        // Check cache
        val cacheCount = problemDao.count()
        if (cacheCount > 0 && tags == null) {
            // Cache hit for unfiltered; we'll still fetch fresh in background
        }
        try {
            val response = api.getProblems(tags)
            if (response.status == "OK" && response.result != null) {
                // Cache problems
                if (tags == null) {
                    val entities = response.result.problems.map { it.toEntity(response.result.problemStatistics) }
                    problemDao.insertProblems(entities)
                }
                emit(Resource.Success(response.result))
            } else {
                emit(Resource.Error(response.comment ?: "Error"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ── Contests ───────────────────────────────────────────────────────────────

    fun getContestList(): Flow<Resource<List<ContestDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getContestList()
            if (response.status == "OK" && response.result != null) {
                contestDao.insertContests(response.result.map { it.toEntity() })
                emit(Resource.Success(response.result))
            } else {
                emit(Resource.Error(response.comment ?: "Error"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun getContestStandings(contestId: Int, from: Int = 1, count: Int = 50): Resource<StandingsDto> = try {
        val response = api.getContestStandings(contestId, from, count)
        if (response.status == "OK") Resource.Success(response.result!!)
        else Resource.Error(response.comment ?: "Error")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    suspend fun getContestRatingChanges(contestId: Int): Resource<List<RatingChangeDto>> = try {
        val response = api.getContestRatingChanges(contestId)
        if (response.status == "OK") Resource.Success(response.result ?: emptyList())
        else Resource.Error(response.comment ?: "Error")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    // ── Blog / Recent ──────────────────────────────────────────────────────────

    suspend fun getBlogEntry(blogEntryId: Int): Resource<BlogEntryDto> = try {
        val response = api.getBlogEntry(blogEntryId)
        if (response.status == "OK") Resource.Success(response.result!!)
        else Resource.Error(response.comment ?: "Error")
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error")
    }

    fun getRecentActions(maxCount: Int = 30): Flow<Resource<List<RecentActionDto>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getRecentActions(maxCount)
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

private fun ProblemDto.toEntity(stats: List<ProblemStatisticsDto>): CachedProblemEntity {
    val solvedCount = stats.find { it.contestId == contestId && it.index == index }?.solvedCount ?: 0
    return CachedProblemEntity(
        id = "${contestId}_${index}",
        contestId = contestId, index = index, name = name,
        rating = rating, tags = tags, solvedCount = solvedCount
    )
}

private fun ContestDto.toEntity() = CachedContestEntity(
    id = id, name = name, type = type, phase = phase,
    durationSeconds = durationSeconds, startTimeSeconds = startTimeSeconds
)
