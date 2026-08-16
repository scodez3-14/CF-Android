package com.codeforces.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codeforces.app.data.update.ReleaseInfo
import com.codeforces.app.ui.navigation.AppNavGraph
import com.codeforces.app.ui.navigation.Screen
import com.codeforces.app.ui.screens.contests.ContestListScreen
import com.codeforces.app.ui.screens.home.HomeScreen
import com.codeforces.app.ui.screens.onboarding.OnboardingScreen
import com.codeforces.app.ui.screens.problems.ProblemsScreen
import com.codeforces.app.ui.screens.profile.ProfileScreen
import com.codeforces.app.ui.screens.search.SearchScreen
import com.codeforces.app.ui.theme.CodeforcesAccent
import com.codeforces.app.ui.theme.CodeforcesTheme
import com.codeforces.app.ui.theme.CfSurface
import com.codeforces.app.ui.theme.CfDivider
import com.codeforces.app.ui.theme.CfCardSurface
import com.codeforces.app.ui.theme.CfTextPrimary
import com.codeforces.app.ui.theme.CfTextSecondary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Force maximum refresh rate (e.g., 144Hz) on supported devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.supportedModes?.maxByOrNull { it.refreshRate }?.let { mode ->
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = mode.modeId
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            CodeforcesTheme {
                MainAppContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppForegroundState.setForeground(true)
    }

    override fun onPause() {
        super.onPause()
        AppForegroundState.setForeground(false)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    val handle by mainViewModel.handle.collectAsStateWithLifecycle(initialValue = null)
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val updateInfo by mainViewModel.updateInfo.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show update dialog whenever a new release is detected
    updateInfo?.let { release ->
        UpdateAvailableDialog(
            release = release,
            onDismiss = { mainViewModel.dismissUpdate() }
        )
    }

    when {
        isLoading -> Unit
        handle.isNullOrBlank() -> OnboardingScreen(onComplete = {})
        else -> {
            val scope = rememberCoroutineScope()
            val saveableStateHolder = rememberSaveableStateHolder()

            var selectedTab by rememberSaveable { mutableStateOf(0) }
            val pagerState = rememberPagerState(initialPage = selectedTab) { bottomNavItems.size }

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { page -> selectedTab = page }
            }

            val showBottomBar = currentRoute == Screen.TabHost.route

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        Column {
                            HorizontalDivider(color = CfDivider)
                            NavigationBar(
                                containerColor = CfSurface,
                                tonalElevation = 0.dp
                            ) {
                                bottomNavItems.forEachIndexed { index, item ->
                                    val isSelected = selectedTab == index
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    index,
                                                    animationSpec = tween(
                                                        durationMillis = 350,
                                                        easing = FastOutSlowInEasing
                                                    )
                                                )
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) item.selectedIcon else item.icon,
                                                contentDescription = item.label,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        },
                                        label = { Text(item.label) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = CodeforcesAccent,
                                            selectedTextColor = CodeforcesAccent,
                                            indicatorColor = CodeforcesAccent.copy(alpha = 0.15f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    HorizontalPager(
                        state = pagerState,
                        beyondBoundsPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                        val scale = 1f - (pageOffset * 0.08f).coerceIn(0f, 1f)
                        val alpha = 1f - (pageOffset * 0.3f).coerceIn(0f, 1f)

                        Box(
                            modifier = Modifier.fillMaxSize().graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                        ) {
                            saveableStateHolder.SaveableStateProvider(page) {
                                when (page) {
                                    0 -> HomeScreen(
                                        navController = navController,
                                        onOpenTab = { index ->
                                            scope.launch {
                                                pagerState.animateScrollToPage(
                                                    index,
                                                    animationSpec = tween(
                                                        durationMillis = 350,
                                                        easing = FastOutSlowInEasing
                                                    )
                                                )
                                            }
                                        }
                                    )
                                    1 -> ProblemsScreen(navController = navController)
                                    2 -> ContestListScreen(navController = navController)
                                    3 -> ProfileScreen(handle = null, navController = navController, onBack = null)
                                    4 -> SearchScreen(navController = navController)
                                }
                            }
                        }
                    }
                    AppNavGraph(
                        navController = navController,
                        startDestination = Screen.TabHost.route,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// ── GitHub Update Dialog ────────────────────────────────────────────────────

@Composable
fun UpdateAvailableDialog(release: ReleaseInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CfCardSurface,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                Icons.Rounded.SystemUpdate,
                contentDescription = null,
                tint = CodeforcesAccent,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Update Available 🎉",
                    color = CfTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    release.tagName,
                    color = CodeforcesAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "A new version of the app is available on GitHub.",
                    color = CfTextSecondary,
                    fontSize = 14.sp
                )
                if (release.body.isNotBlank()) {
                    HorizontalDivider(color = CfDivider)
                    Text(
                        "What's new:",
                        color = CfTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    // Show first 400 chars of the changelog
                    Text(
                        release.body.take(400).let { if (release.body.length > 400) "$it…" else it },
                        color = CfTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl))
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Download Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", color = CfTextSecondary)
            }
        }
    )
}
