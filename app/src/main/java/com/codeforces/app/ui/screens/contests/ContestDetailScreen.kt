package com.codeforces.app.ui.screens.contests

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.codeforces.app.data.api.ContestDto
import com.codeforces.app.ui.navigation.Screen
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
                CircularProgressIndicator(color = CodeforcesRed)
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
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
                    colors = ButtonDefaults.buttonColors(containerColor = CodeforcesRed)
                ) {
                    Icon(Icons.Rounded.Leaderboard, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("View Standings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = CodeforcesRed, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = CfTextPrimary)
        }
    }
}
