package com.codeforces.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codeforces.app.ui.navigation.AppNavGraph
import com.codeforces.app.ui.navigation.Screen
import com.codeforces.app.ui.theme.CodeforcesTheme
import com.codeforces.app.ui.theme.CfSurface
import com.codeforces.app.ui.theme.CfDivider
import dagger.hilt.android.AndroidEntryPoint

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Screen.Home.route, Icons.Rounded.Home, Icons.Filled.Home),
    BottomNavItem("Problems", Screen.Problems.route, Icons.Rounded.Code, Icons.Filled.Code),
    BottomNavItem("Contests", Screen.Contests.route, Icons.Rounded.EmojiEvents, Icons.Filled.EmojiEvents),
    BottomNavItem("Profile", Screen.MyProfile.route, Icons.Rounded.Person, Icons.Filled.Person),
    BottomNavItem("More", Screen.Search.route, Icons.Rounded.Search, Icons.Filled.Search)
)

val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodeforcesTheme {
                MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    val handle by mainViewModel.handle.collectAsStateWithLifecycle(initialValue = null)
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = when {
        isLoading -> null
        handle.isNullOrBlank() -> Screen.Onboarding.route
        else -> Screen.Home.route
    }

    val showBottomBar = currentRoute in bottomNavRoutes

    if (startDestination != null) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = CfSurface
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            AppNavGraph(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
