package com.codeforces.app.data.api

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

// ─── Wrapper ─────────────────────────────────────────────────────────────────

data class CfResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("result") val result: T?,
    @SerializedName("comment") val comment: String?
)

// ─── User ─────────────────────────────────────────────────────────────────────

@Immutable
data class UserDto(
    @SerializedName("handle") val handle: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("organization") val organization: String? = null,
    @SerializedName("contribution") val contribution: Int = 0,
    @SerializedName("rank") val rank: String? = null,
    @SerializedName("rating") val rating: Int = 0,
    @SerializedName("maxRank") val maxRank: String? = null,
    @SerializedName("maxRating") val maxRating: Int = 0,
    @SerializedName("lastOnlineTimeSeconds") val lastOnlineTimeSeconds: Long = 0,
    @SerializedName("registrationTimeSeconds") val registrationTimeSeconds: Long = 0,
    @SerializedName("friendOfCount") val friendOfCount: Int = 0,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("titlePhoto") val titlePhoto: String? = null
)

// ─── Rating Change ────────────────────────────────────────────────────────────

data class RatingChangeDto(
    @SerializedName("contestId") val contestId: Int,
    @SerializedName("contestName") val contestName: String,
    @SerializedName("handle") val handle: String,
    @SerializedName("rank") val rank: Int,
    @SerializedName("ratingUpdateTimeSeconds") val ratingUpdateTimeSeconds: Long,
    @SerializedName("oldRating") val oldRating: Int,
    @SerializedName("newRating") val newRating: Int
)

// ─── Problem ──────────────────────────────────────────────────────────────────

@Immutable
data class ProblemDto(
    @SerializedName("contestId") val contestId: Int? = null,
    @SerializedName("problemsetName") val problemsetName: String? = null,
    @SerializedName("index") val index: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String = "PROGRAMMING",
    @SerializedName("points") val points: Double? = null,
    @SerializedName("rating") val rating: Int? = null,
    @SerializedName("tags") val tags: List<String> = emptyList()
)

@Immutable
data class ProblemStatisticsDto(
    @SerializedName("contestId") val contestId: Int? = null,
    @SerializedName("index") val index: String,
    @SerializedName("solvedCount") val solvedCount: Int
)

data class ProblemSetResultDto(
    @SerializedName("problems") val problems: List<ProblemDto>,
    @SerializedName("problemStatistics") val problemStatistics: List<ProblemStatisticsDto>
)

// ─── Contest ──────────────────────────────────────────────────────────────────

@Immutable
data class ContestDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("phase") val phase: String,
    @SerializedName("frozen") val frozen: Boolean = false,
    @SerializedName("durationSeconds") val durationSeconds: Long,
    @SerializedName("startTimeSeconds") val startTimeSeconds: Long? = null,
    @SerializedName("relativeTimeSeconds") val relativeTimeSeconds: Long? = null,
    @SerializedName("preparedBy") val preparedBy: String? = null,
    @SerializedName("websiteUrl") val websiteUrl: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("difficulty") val difficulty: Int? = null,
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("icpcRegion") val icpcRegion: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("season") val season: String? = null
)

// ─── Submission ───────────────────────────────────────────────────────────────

@Immutable
data class SubmissionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("contestId") val contestId: Int? = null,
    @SerializedName("creationTimeSeconds") val creationTimeSeconds: Long,
    @SerializedName("relativeTimeSeconds") val relativeTimeSeconds: Long,
    @SerializedName("problem") val problem: ProblemDto,
    @SerializedName("author") val author: AuthorDto,
    @SerializedName("programmingLanguage") val programmingLanguage: String,
    @SerializedName("verdict") val verdict: String? = null,
    @SerializedName("testset") val testset: String = "TESTS",
    @SerializedName("passedTestCount") val passedTestCount: Int,
    @SerializedName("timeConsumedMillis") val timeConsumedMillis: Int,
    @SerializedName("memoryConsumedBytes") val memoryConsumedBytes: Long
)

data class AuthorDto(
    @SerializedName("contestId") val contestId: Int? = null,
    @SerializedName("members") val members: List<MemberDto>,
    @SerializedName("participantType") val participantType: String,
    @SerializedName("teamId") val teamId: Int? = null,
    @SerializedName("teamName") val teamName: String? = null,
    @SerializedName("ghost") val ghost: Boolean = false,
    @SerializedName("room") val room: Int? = null,
    @SerializedName("startTimeSeconds") val startTimeSeconds: Long? = null
)

data class MemberDto(
    @SerializedName("handle") val handle: String,
    @SerializedName("name") val name: String? = null
)

// ─── Standings ────────────────────────────────────────────────────────────────

data class StandingsDto(
    @SerializedName("contest") val contest: ContestDto,
    @SerializedName("problems") val problems: List<ProblemDto>,
    @SerializedName("rows") val rows: List<RanklistRowDto>
)

data class RanklistRowDto(
    @SerializedName("party") val party: AuthorDto,
    @SerializedName("rank") val rank: Int,
    @SerializedName("points") val points: Double,
    @SerializedName("penalty") val penalty: Int,
    @SerializedName("successfulHackCount") val successfulHackCount: Int,
    @SerializedName("unsuccessfulHackCount") val unsuccessfulHackCount: Int,
    @SerializedName("problemResults") val problemResults: List<ProblemResultDto>
)

data class ProblemResultDto(
    @SerializedName("points") val points: Double,
    @SerializedName("penalty") val penalty: Int? = null,
    @SerializedName("rejectedAttemptCount") val rejectedAttemptCount: Int,
    @SerializedName("type") val type: String,
    @SerializedName("bestSubmissionTimeSeconds") val bestSubmissionTimeSeconds: Long? = null
)

// ─── Blog Entry ───────────────────────────────────────────────────────────────

data class BlogEntryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("originalLocale") val originalLocale: String,
    @SerializedName("creationTimeSeconds") val creationTimeSeconds: Long,
    @SerializedName("authorHandle") val authorHandle: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String? = null,
    @SerializedName("locale") val locale: String,
    @SerializedName("modificationTimeSeconds") val modificationTimeSeconds: Long,
    @SerializedName("allowViewHistory") val allowViewHistory: Boolean,
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("rating") val rating: Int
)

// ─── Recent Actions ───────────────────────────────────────────────────────────

data class RecentActionDto(
    @SerializedName("timeSeconds") val timeSeconds: Long,
    @SerializedName("blogEntry") val blogEntry: BlogEntryDto? = null,
    @SerializedName("comment") val comment: CommentDto? = null
)

data class CommentDto(
    @SerializedName("id") val id: Int,
    @SerializedName("creationTimeSeconds") val creationTimeSeconds: Long,
    @SerializedName("commentatorHandle") val commentatorHandle: String,
    @SerializedName("locale") val locale: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("text") val text: String? = null
)
