package com.codeforces.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_users")
data class CachedUserEntity(
    @PrimaryKey val handle: String,
    val firstName: String?,
    val lastName: String?,
    val country: String?,
    val city: String?,
    val organization: String?,
    val contribution: Int,
    val rank: String?,
    val rating: Int,
    val maxRank: String?,
    val maxRating: Int,
    val friendOfCount: Int,
    val avatar: String?,
    val titlePhoto: String?,
    val lastOnlineTimeSeconds: Long,
    val registrationTimeSeconds: Long,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_problems")
data class CachedProblemEntity(
    @PrimaryKey val id: String, // contestId + index
    val contestId: Int?,
    val index: String,
    val name: String,
    val rating: Int?,
    val tags: List<String>,
    val solvedCount: Int,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_contests")
data class CachedContestEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String,
    val phase: String,
    val durationSeconds: Long,
    val startTimeSeconds: Long?,
    val cachedAt: Long = System.currentTimeMillis()
)
