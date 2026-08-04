package com.codeforces.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CachedUserEntity::class, CachedProblemEntity::class, CachedContestEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CodeforcesDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun problemDao(): ProblemDao
    abstract fun contestDao(): ContestDao
}
