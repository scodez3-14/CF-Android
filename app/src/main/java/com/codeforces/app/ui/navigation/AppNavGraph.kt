package com.codeforces.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.codeforces.app.ui.screens.blog.BlogScreen
import com.codeforces.app.ui.screens.contests.ContestDetailScreen
import com.codeforces.app.ui.screens.contests.ContestListScreen
import com.codeforces.app.ui.screens.contests.EditorialScreen
import com.codeforces.app.ui.screens.home.HomeScreen
import com.codeforces.app.ui.screens.leaderboard.LeaderboardScreen
import com.codeforces.app.ui.screens.login.LoginScreen
import com.codeforces.app.ui.screens.login.WebLoginScreen
import com.codeforces.app.ui.screens.onboarding.OnboardingScreen
import com.codeforces.app.ui.screens.problems.ProblemDetailScreen
import com.codeforces.app.ui.screens.problems.ProblemsScreen
import android.net.Uri
import com.codeforces.app.ui.screens.profile.ProfileScreen
import com.codeforces.app.ui.screens.search.SearchScreen
import com.codeforces.app.ui.screens.settings.SettingsScreen
import com.codeforces.app.ui.screens.standings.StandingsScreen
import com.codeforces.app.ui.screens.submissions.SubmissionsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(280)) +
                    fadeIn(animationSpec = tween(280))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(280)) +
                    fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(280)) +
                    fadeIn(animationSpec = tween(280))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(280)) +
                    fadeOut(animationSpec = tween(200))
        }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onComplete = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Problems.route) {
            ProblemsScreen(navController = navController)
        }
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
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Contests.route) {
            ContestListScreen(navController = navController)
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
        composable(Screen.MyProfile.route) {
            ProfileScreen(handle = null, navController = navController, onBack = null)
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
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
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
