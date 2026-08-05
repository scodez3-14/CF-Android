package com.codeforces.app.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CodeforcesApiService {

    // User
    @GET("user.info")
    suspend fun getUserInfo(
        @Query("handles") handles: String
    ): CfResponse<List<UserDto>>

    @GET("user.rating")
    suspend fun getUserRating(
        @Query("handle") handle: String
    ): CfResponse<List<RatingChangeDto>>

    @GET("user.status")
    suspend fun getUserStatus(
        @Query("handle") handle: String,
        @Query("from") from: Int = 1,
        @Query("count") count: Int = 100
    ): CfResponse<List<SubmissionDto>>

    @GET("user.ratedList")
    suspend fun getRatedList(
        @Query("activeOnly") activeOnly: Boolean = true,
        @Query("includeRetired") includeRetired: Boolean = false
    ): CfResponse<List<UserDto>>

    @GET("user.blogEntries")
    suspend fun getUserBlogEntries(
        @Query("handle") handle: String
    ): CfResponse<List<BlogEntryDto>>

    // Problems
    @GET("problemset.problems")
    suspend fun getProblems(
        @Query("tags") tags: String? = null,
        @Query("problemsetName") problemsetName: String? = null
    ): CfResponse<ProblemSetResultDto>

    // Contests
    @GET("contest.list")
    suspend fun getContestList(
        @Query("gym") gym: Boolean = false
    ): CfResponse<List<ContestDto>>

    @GET("contest.standings")
    suspend fun getContestStandings(
        @Query("contestId") contestId: Int
    ): CfResponse<StandingsDto>

    @GET("contest.ratingChanges")
    suspend fun getContestRatingChanges(
        @Query("contestId") contestId: Int
    ): CfResponse<List<RatingChangeDto>>

    // Blog
    @GET("blogEntry.view")
    suspend fun getBlogEntry(
        @Query("blogEntryId") blogEntryId: Int
    ): CfResponse<BlogEntryDto>

    @GET("recentActions")
    suspend fun getRecentActions(
        @Query("maxCount") maxCount: Int = 30
    ): CfResponse<List<RecentActionDto>>
}
