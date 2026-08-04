package com.codeforces.app.di

import android.content.Context
import androidx.room.Room
import com.codeforces.app.data.api.CodeforcesApiService
import com.codeforces.app.data.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://codeforces.com/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): CodeforcesApiService {
        return retrofit.create(CodeforcesApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CodeforcesDatabase {
        return Room.databaseBuilder(
            context,
            CodeforcesDatabase::class.java,
            "codeforces.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(db: CodeforcesDatabase): UserDao = db.userDao()

    @Provides
    fun provideProblemDao(db: CodeforcesDatabase): ProblemDao = db.problemDao()

    @Provides
    fun provideContestDao(db: CodeforcesDatabase): ContestDao = db.contestDao()
}
