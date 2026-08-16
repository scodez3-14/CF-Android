package com.codeforces.app.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object TabHost : Screen("tab_host")
    object Home : Screen("home")
    object Problems : Screen("problems")
    object ProblemDetail : Screen("problem_detail/{contestId}/{index}/{name}") {
        fun createRoute(contestId: String, index: String, name: String) = "problem_detail/$contestId/$index/${Uri.encode(name)}"
    }
    object Contests : Screen("contests")
    object ContestDetail : Screen("contest_detail/{contestId}") {
        fun createRoute(contestId: Int) = "contest_detail/$contestId"
    }
    object Standings : Screen("standings/{contestId}") {
        fun createRoute(contestId: Int) = "standings/$contestId"
    }
    object Editorial : Screen("editorial/{contestId}/{name}") {
        fun createRoute(contestId: Int, name: String) = "editorial/$contestId/${Uri.encode(name)}"
    }
    object Profile : Screen("profile/{handle}") {
        fun createRoute(handle: String) = "profile/$handle"
    }
    object MyProfile : Screen("my_profile")
    object Submissions : Screen("submissions/{handle}") {
        fun createRoute(handle: String) = "submissions/$handle"
    }
    object SubmissionDetail : Screen("submission_detail/{contestId}/{submissionId}/{handle}") {
        fun createRoute(contestId: String, submissionId: Long, handle: String) =
            "submission_detail/$contestId/$submissionId/${Uri.encode(handle)}"
    }
    object Search : Screen("search")
    object Leaderboard : Screen("leaderboard")
    object Blog : Screen("blog/{handle}") {
        fun createRoute(handle: String) = "blog/$handle"
    }
    object Settings : Screen("settings")
    object Login : Screen("login")
    object WebLogin : Screen("web_login")
}
