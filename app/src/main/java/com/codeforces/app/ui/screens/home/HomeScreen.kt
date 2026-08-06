package com.codeforces.app.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.codeforces.app.data.api.ContestDto
import com.codeforces.app.ui.components.AnimatedCountUpText
import com.codeforces.app.ui.components.RevealItem
import com.codeforces.app.ui.components.ShimmerCardRow
import com.codeforces.app.ui.components.ShimmerHeroCard
import com.codeforces.app.ui.components.SkeletonBox
import com.codeforces.app.ui.components.rememberShimmerBrush
import com.codeforces.app.ui.navigation.Screen
import com.codeforces.app.ui.screens.profile.SubmissionRow
import com.codeforces.app.ui.screens.profile.rankColor
import com.codeforces.app.ui.theme.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onOpenTab: (Int) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val handle by viewModel.handle.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Code, contentDescription = null, tint = CodeforcesAccent, modifier = Modifier.size(22.dp))
                        Text("Codeforces", fontWeight = FontWeight.ExtraBold, color = CfTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfBackground)
            )
        }
    ) { padding ->
        val showingSkeleton = state.isLoading && state.user == null && state.dailyProblem == null
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showingSkeleton) {
                val brush = rememberShimmerBrush()
                ShimmerHeroCard(brush, Modifier.padding(horizontal = 16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SkeletonBox(brush, Modifier.fillMaxWidth(0.4f).height(18.dp), cornerRadius = 6.dp)
                    ShimmerCardRow(brush, shapeRadius = 20.dp)
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SkeletonBox(brush, Modifier.fillMaxWidth(0.45f).height(18.dp), cornerRadius = 6.dp)
                    ShimmerCardRow(brush, shapeRadius = 12.dp)
                    ShimmerCardRow(brush, shapeRadius = 12.dp)
                    ShimmerCardRow(brush, shapeRadius = 12.dp)
                }
            } else {
                // Profile hero card
                state.user?.let { user ->
                    RevealItem(visible = true, delayMillis = 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { onOpenTab(3) },
                            colors = CardDefaults.cardColors(containerColor = CfCardSurface),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(CfCardSurface, CodeforcesAccent.copy(alpha = 0.15f))
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    AsyncImage(
                                        model = user.titlePhoto ?: user.avatar,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.size(60.dp).clip(CircleShape).background(CfSurface)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.handle,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                            color = CfTextPrimary
                                        )
                                        Text(
                                            text = user.rank?.replaceFirstChar { it.uppercase() } ?: "Unrated",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = rankColor(user.rank)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        AnimatedCountUpText(
                                            target = user.rating,
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = rankColor(user.rank)
                                        )
                                        Text("Rating", style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Problem of the Day
                state.dailyProblem?.let { problem ->
                    SectionHeader("Problem of the Day", onSeeAll = null)
                    RevealItem(visible = true, delayMillis = 60) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    navController.navigate(
                                        Screen.ProblemDetail.createRoute(
                                            problem.contestId.toString(),
                                            problem.index,
                                            problem.name
                                        )
                                    )
                                },
                            colors = CardDefaults.cardColors(containerColor = CfCardSurface),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CodeforcesAccent.copy(alpha = 0.08f))
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(CodeforcesAccent.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = CodeforcesAccent, modifier = Modifier.size(24.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = problem.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = CfTextPrimary,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = "${problem.contestId}${problem.index} · ${problem.rating ?: "?"} rating · ${problem.tags.take(3).joinToString(", ")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CfTextSecondary,
                                        maxLines = 1
                                    )
                                }
                                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = CfTextDisabled)
                            }
                        }
                    }
                }

                // Upcoming Contests
                if (state.upcomingContests.isNotEmpty()) {
                    SectionHeader("Upcoming Contests", onSeeAll = { onOpenTab(2) })
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.upcomingContests.take(3).forEach { contest ->
                            RevealItem(visible = true, delayMillis = 120) {
                                ContestCard(contest = contest, onClick = {
                                    navController.navigate(Screen.ContestDetail.createRoute(contest.id))
                                })
                            }
                        }
                    }
                }

                // Recent Submissions
                if (state.recentSubmissions.isNotEmpty()) {
                    SectionHeader("Recent Submissions", onSeeAll = {
                        handle?.let { navController.navigate(Screen.Submissions.createRoute(it)) }
                    })
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.recentSubmissions.take(5).forEach { sub ->
                            RevealItem(visible = true, delayMillis = 180) {
                                SubmissionRow(
                                    problemName = sub.problem.name,
                                    verdict = sub.verdict ?: "IN_QUEUE",
                                    language = sub.programmingLanguage,
                                    timeMs = sub.timeConsumedMillis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        onSeeAll?.let {
            TextButton(onClick = it) {
                Text("See all", color = CfAccentLight, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun ContestCard(contest: ContestDto, onClick: () -> Unit) {
    val now = System.currentTimeMillis() / 1000
    val startTime = contest.startTimeSeconds ?: 0L
    val diff = startTime - now
    val timeLabel = when {
        diff <= 0 -> "Started"
        diff < 3600 -> "${diff / 60}m"
        diff < 86400 -> "${diff / 3600}h ${(diff % 3600) / 60}m"
        else -> "${diff / 86400}d ${(diff % 86400) / 3600}h"
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = CodeforcesAccent, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contest.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = CfTextPrimary, maxLines = 2)
                Text("${contest.durationSeconds / 3600}h duration", style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("in", style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
                Text(timeLabel, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CodeforcesAccent)
            }
        }
    }
}
