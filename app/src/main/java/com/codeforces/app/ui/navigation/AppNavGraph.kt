package com.codeforces.app.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.codeforces.app.ui.screens.blog.BlogScreen
import com.codeforces.app.ui.screens.contests.ContestDetailScreen
import com.codeforces.app.ui.screens.contests.EditorialScreen
import com.codeforces.app.ui.screens.leaderboard.LeaderboardScreen
import com.codeforces.app.ui.screens.login.LoginScreen
import com.codeforces.app.ui.screens.login.WebLoginScreen
import com.codeforces.app.ui.screens.problems.ProblemDetailScreen
import android.net.Uri
import com.codeforces.app.ui.screens.profile.ProfileScreen
import com.codeforces.app.ui.screens.settings.SettingsScreen
import com.codeforces.app.ui.screens.standings.StandingsScreen
import com.codeforces.app.ui.screens.submissions.SubmissionDetailScreen
import com.codeforces.app.ui.screens.submissions.SubmissionsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.TabHost.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it / 3 }
        },
        exitTransition = {
            fadeOut(tween(180, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { -it / 3 }
        },
        popExitTransition = {
            fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                    slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { it / 2 }
        }
    ) {
        composable(Screen.TabHost.route) { }

        composable(
            Screen.ProblemDetail.route,
            arguments = listOf(
                navArgument("contestId") { type = NavType.StringType },
                navArgument("index") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ProblemDetailScreen(
                contestId = backStackEntry.arguments?.getString("contestId") ?: "",
                index = backStackEntry.arguments?.getString("index") ?: "",
                name = Uri.decode(backStackEntry.arguments?.getString("name") ?: ""),
                onLogin = { navController.navigate(Screen.Login.route) },
                onBack = { navController.popBackStack() },
                onOpenSubmission = { cid, sid, h ->
                    navController.navigate(Screen.SubmissionDetail.createRoute(cid, sid, h))
                }
            )
        }
        composable(
            Screen.ContestDetail.route,
            arguments = listOf(navArgument("contestId") { type = NavType.IntType })
        ) { backStackEntry ->
            ContestDetailScreen(
                contestId = backStackEntry.arguments?.getInt("contestId") ?: -1,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.Standings.route,
            arguments = listOf(navArgument("contestId") { type = NavType.IntType })
        ) { backStackEntry ->
            StandingsScreen(
                contestId = backStackEntry.arguments?.getInt("contestId") ?: -1,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.Editorial.route,
            arguments = listOf(
                navArgument("contestId") { type = NavType.IntType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            EditorialScreen(
                contestId = backStackEntry.arguments?.getInt("contestId") ?: -1,
                name = Uri.decode(backStackEntry.arguments?.getString("name") ?: "Editorial"),
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.Profile.route,
            arguments = listOf(navArgument("handle") { type = NavType.StringType })
        ) { backStackEntry ->
            ProfileScreen(
                handle = backStackEntry.arguments?.getString("handle"),
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.Submissions.route,
            arguments = listOf(navArgument("handle") { type = NavType.StringType })
        ) { backStackEntry ->
            SubmissionsScreen(
                handle = backStackEntry.arguments?.getString("handle") ?: "",
                navController = navController,
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(
            Screen.SubmissionDetail.route,
            arguments = listOf(
                navArgument("contestId") { type = NavType.StringType },
                navArgument("submissionId") { type = NavType.LongType },
                navArgument("handle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            SubmissionDetailScreen(
                contestId = backStackEntry.arguments?.getString("contestId") ?: "",
                submissionId = backStackEntry.arguments?.getLong("submissionId") ?: 0L,
                handle = Uri.decode(backStackEntry.arguments?.getString("handle") ?: ""),
                navController = navController,
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(navController = navController)
        }
        composable(
            Screen.Blog.route,
            arguments = listOf(navArgument("handle") { type = NavType.StringType })
        ) { backStackEntry ->
            BlogScreen(
                handle = backStackEntry.arguments?.getString("handle") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoggedIn = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                onBrowserLogin = { navController.navigate(Screen.WebLogin.route) }
            )
        }
        composable(Screen.WebLogin.route) {
            WebLoginScreen(
                onLoggedIn = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
