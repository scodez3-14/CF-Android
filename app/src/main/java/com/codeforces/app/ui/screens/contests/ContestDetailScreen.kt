package com.codeforces.app.ui.screens.contests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.codeforces.app.data.api.ContestDto
import com.codeforces.app.data.api.ProblemDto
import com.codeforces.app.ui.navigation.Screen
import com.codeforces.app.ui.screens.problems.ProblemCard
import com.codeforces.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestDetailScreen(
    contestId: Int,
    navController: NavController,
    onBack: () -> Unit,
    viewModel: ContestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val contest = (state.upcoming + state.past).find { it.id == contestId }
    val fmt = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())

    LaunchedEffect(contestId) { viewModel.loadContestProblems(contestId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contest Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        }
    ) { padding ->
        if (contest == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CodeforcesAccent)
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(contest.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = CfTextPrimary)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(contest.type) })
                AssistChip(onClick = {}, label = { Text(contest.phase) })
            }

            DetailRow(label = "Start Time", value = contest.startTimeSeconds?.let { fmt.format(Date(it * 1000)) } ?: "TBD", icon = Icons.Rounded.Schedule)
            DetailRow(label = "Duration", value = "${contest.durationSeconds / 3600}h ${(contest.durationSeconds % 3600) / 60}m", icon = Icons.Rounded.Timer)
            contest.preparedBy?.let { DetailRow(label = "Prepared by", value = it, icon = Icons.Rounded.Person) }

            if (contest.phase == "FINISHED") {
                Button(
                    onClick = { navController.navigate(Screen.Standings.createRoute(contest.id)) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent)
                ) {
                    Icon(Icons.Rounded.Leaderboard, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("View Standings", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { navController.navigate(Screen.Editorial.createRoute(contest.id, contest.name)) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CodeforcesAccent)
                ) {
                    Icon(Icons.Rounded.School, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Editorial", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = CfDivider)

                ContestProblemsSection(
                    problems = state.problems,
                    isLoading = state.problemsLoading,
                    error = state.problemsError,
                    onRetry = { viewModel.loadContestProblems(contestId) },
                    onProblemClick = { problem ->
                        navController.navigate(
                            Screen.ProblemDetail.createRoute(
                                problem.contestId?.toString() ?: contestId.toString(),
                                problem.index,
                                problem.name
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ContestProblemsSection(
    problems: List<ProblemDto>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onProblemClick: (ProblemDto) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Rounded.Quiz, contentDescription = null, tint = CodeforcesAccent)
            Text(
                "Problems",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CfTextPrimary
            )
            if (problems.isNotEmpty()) {
                Text(
                    "(${problems.size})",
                    fontSize = 13.sp,
                    color = CfTextSecondary
                )
            }
        }

        when {
            isLoading -> {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CodeforcesAccent, strokeWidth = 3.dp)
                }
            }
            error != null && problems.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.WifiOff, contentDescription = null, tint = CfTextSecondary, modifier = Modifier.size(36.dp))
                    Text(error, color = CfTextSecondary, fontSize = 13.sp)
                    TextButton(onClick = onRetry) {
                        Text("Retry", color = CodeforcesAccent)
                    }
                }
            }
            else -> {
                problems.forEach { problem ->
                    ProblemCard(problem = problem, solvedCount = 0, onClick = { onProblemClick(problem) })
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = CodeforcesAccent, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = CfTextPrimary)
        }
    }
}
