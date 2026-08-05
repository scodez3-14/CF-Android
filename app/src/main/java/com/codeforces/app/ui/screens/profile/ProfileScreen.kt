package com.codeforces.app.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.codeforces.app.data.api.UserDto
import com.codeforces.app.ui.navigation.Screen
import com.codeforces.app.ui.theme.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.*

fun rankColor(rank: String?): Color = when {
    rank == null -> RatingNewbie
    rank.contains("legendary grandmaster", true) -> Color(0xFFCC0000)
    rank.contains("international grandmaster", true) -> Color(0xFFFF0000)
    rank.contains("grandmaster", true) -> Color(0xFFFF0000)
    rank.contains("international master", true) -> Color(0xFFFF8C00)
    rank.contains("master", true) -> Color(0xFFFF8C00)
    rank.contains("candidate master", true) -> Color(0xFFAA00AA)
    rank.contains("expert", true) -> Color(0xFF0000FF)
    rank.contains("specialist", true) -> Color(0xFF03A89E)
    rank.contains("pupil", true) -> Color(0xFF008000)
    else -> RatingNewbie
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    handle: String?,
    navController: NavController,
    onBack: (() -> Unit)?,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedHandle by viewModel.savedHandle.collectAsStateWithLifecycle(initialValue = null)
    val resolvedHandle = handle ?: savedHandle ?: ""
    val context = LocalContext.current

    LaunchedEffect(resolvedHandle) {
        if (resolvedHandle.isNotBlank()) viewModel.loadProfile(resolvedHandle)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(resolvedHandle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Submissions.createRoute(resolvedHandle))
                    }) {
                        Icon(Icons.Rounded.Assignment, contentDescription = "Submissions")
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.Blog.createRoute(resolvedHandle))
                    }) {
                        Icon(Icons.Rounded.Article, contentDescription = "Blog")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        }
    ) { padding ->
        if (state.isLoading && state.user == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CodeforcesAccent)
            }
            return@Scaffold
        }
        state.error?.let {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(it, color = VerdictWA)
            }
            return@Scaffold
        }
        state.user?.let { user ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CfSurface)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AsyncImage(
                            model = user.titlePhoto ?: user.avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(CfCardSurface)
                        )
                        Column {
                            Text(
                                text = "${user.firstName.orEmpty()} ${user.lastName.orEmpty()}".trim().ifBlank { user.handle },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = CfTextPrimary
                            )
                            Text(
                                text = user.rank?.replaceFirstChar { it.uppercase() } ?: "Unrated",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = rankColor(user.rank)
                            )
                            user.country?.let {
                                Text(text = "🌍 $it", style = MaterialTheme.typography.bodySmall, color = CfTextSecondary)
                            }
                            user.organization?.let {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Business, contentDescription = null, modifier = Modifier.size(12.dp), tint = CfTextSecondary)
                                    Spacer(Modifier.width(4.dp))
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = CfTextSecondary)
                                }
                            }
                        }
                    }
                }

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Rating", user.rating.toString(), modifier = Modifier.weight(1f))
                    StatCard("Max", user.maxRating.toString(), modifier = Modifier.weight(1f))
                    StatCard("Friends", user.friendOfCount.toString(), modifier = Modifier.weight(1f))
                    StatCard("Contrib", user.contribution.toString(), modifier = Modifier.weight(1f))
                }

                // Rating Chart
                if (state.ratingHistory.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Rating History", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(12.dp))
                            val ratingEntries = state.ratingHistory.map { it.newRating.toFloat() }
                            val model = entryModelOf(*ratingEntries.toTypedArray())
                            Chart(
                                chart = lineChart(),
                                model = model,
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }
                }

                // Recent Submissions
                if (state.recentSubmissions.isNotEmpty()) {
                    Text(
                        "Recent Submissions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                    state.recentSubmissions.take(10).forEach { sub ->
                        SubmissionRow(
                            problemName = sub.problem.name,
                            verdict = sub.verdict ?: "IN_QUEUE",
                            language = sub.programmingLanguage,
                            timeMs = sub.timeConsumedMillis
                        )
                    }
                    TextButton(
                        onClick = { navController.navigate(Screen.Submissions.createRoute(resolvedHandle)) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text("View all submissions →", color = CfAccentLight)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CodeforcesAccent)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
        }
    }
}

@Composable
fun SubmissionRow(problemName: String, verdict: String, language: String, timeMs: Int) {
    val verdictColor = when (verdict) {
        "OK" -> VerdictOK
        "WRONG_ANSWER" -> VerdictWA
        "TIME_LIMIT_EXCEEDED" -> VerdictTLE
        "MEMORY_LIMIT_EXCEEDED" -> VerdictMLE
        "RUNTIME_ERROR" -> VerdictRTE
        "COMPILATION_ERROR" -> VerdictCE
        else -> CfTextSecondary
    }
    val verdictText = when (verdict) {
        "OK" -> "AC"
        "WRONG_ANSWER" -> "WA"
        "TIME_LIMIT_EXCEEDED" -> "TLE"
        "MEMORY_LIMIT_EXCEEDED" -> "MLE"
        "RUNTIME_ERROR" -> "RTE"
        "COMPILATION_ERROR" -> "CE"
        "SKIPPED" -> "SKIP"
        else -> verdict.take(6)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CfCardSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(verdictColor.copy(alpha = 0.2f))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(verdictText, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = verdictColor)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(problemName, style = MaterialTheme.typography.bodyMedium, color = CfTextPrimary, maxLines = 1)
            Text(language, style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
        }
        Text("${timeMs}ms", style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
    }
}
