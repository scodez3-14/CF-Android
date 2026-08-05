package com.codeforces.app.di

import android.content.Context
import android.webkit.WebSettings
import androidx.room.Room
import com.codeforces.app.data.api.CodeforcesApiService
import com.codeforces.app.data.db.*
import com.codeforces.app.data.scraper.CfSubmitter
import com.codeforces.app.data.scraper.PersistentCookieJar
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            // Codeforces is strict with unauthenticated bots; a User-Agent helps
            // avoid 403s. Only set it when the caller didn't provide one, so the
            // scraper's mobile Chrome UA keeps working.
            .addInterceptor { chain ->
                val original = chain.request()
                val request = if (original.header("User-Agent") == null) {
                    original.newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) CodeforcesApp/1.0")
                        .build()
                } else original
                chain.proceed(request)
            }
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

    @Provides
    @Singleton
    fun providePersistentCookieJar(@ApplicationContext context: Context): PersistentCookieJar {
        return PersistentCookieJar(context)
    }

    @Provides
    @Singleton
    fun provideCfSubmitter(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        cookieJar: PersistentCookieJar
    ): CfSubmitter {
        return CfSubmitter(okHttpClient, cookieJar, WebSettings.getDefaultUserAgent(context))
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
        ).build()
    }

    @Provides
    fun provideUserDao(db: CodeforcesDatabase): UserDao = db.userDao()

    @Provides
    fun provideProblemDao(db: CodeforcesDatabase): ProblemDao = db.problemDao()

    @Provides
    fun provideContestDao(db: CodeforcesDatabase): ContestDao = db.contestDao()
}
