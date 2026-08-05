package com.codeforces.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM cached_users WHERE handle = :handle")
    suspend fun getUser(handle: String): CachedUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: CachedUserEntity)

    @Query("DELETE FROM cached_users")
    suspend fun clearAll()
}

@Dao
interface ProblemDao {
    @Query("SELECT * FROM cached_problems ORDER BY rating ASC")
    fun getAllProblems(): Flow<List<CachedProblemEntity>>

    @Query("SELECT * FROM cached_problems ORDER BY rating ASC")
    suspend fun getAllProblemsOnce(): List<CachedProblemEntity>

    @Query("SELECT * FROM cached_problems WHERE rating BETWEEN :minRating AND :maxRating ORDER BY rating ASC")
    fun getProblemsByRating(minRating: Int, maxRating: Int): Flow<List<CachedProblemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblems(problems: List<CachedProblemEntity>)

    @Query("DELETE FROM cached_problems")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM cached_problems")
    suspend fun count(): Int
}

@Dao
interface ContestDao {
    @Query("SELECT * FROM cached_contests ORDER BY startTimeSeconds DESC")
    fun getAllContests(): Flow<List<CachedContestEntity>>

    @Query("SELECT * FROM cached_contests ORDER BY startTimeSeconds DESC")
    suspend fun getAllContestsOnce(): List<CachedContestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContests(contests: List<CachedContestEntity>)

    @Query("DELETE FROM cached_contests")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM cached_contests")
    suspend fun count(): Int
}
