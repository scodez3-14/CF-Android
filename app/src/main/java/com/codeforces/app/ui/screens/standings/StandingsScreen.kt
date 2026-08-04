package com.codeforces.app.ui.screens.standings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import com.codeforces.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(
    contestId: Int,
    onBack: () -> Unit,
    viewModel: StandingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(contestId) { viewModel.load(contestId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Standings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CodeforcesRed)
            }
            return@Scaffold
        }
        state.error?.let {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(it, color = VerdictWA)
            }
            return@Scaffold
        }
        state.standings?.let { standings ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Text(
                    standings.contest.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = CfTextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = CfDivider)
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("#", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CfTextSecondary, modifier = Modifier.width(32.dp))
                    Text("Handle", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CfTextSecondary, modifier = Modifier.weight(1f))
                    Text("Score", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CfTextSecondary, modifier = Modifier.width(60.dp))
                    Text("Pen", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CfTextSecondary, modifier = Modifier.width(40.dp))
                }
                HorizontalDivider(color = CfDivider)
                LazyColumn {
                    itemsIndexed(standings.rows) { index, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = row.rank.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = when (row.rank) {
                                    1 -> CodeforcesRed
                                    2 -> CfTextPrimary
                                    3 -> CfTextSecondary
                                    else -> CfTextDisabled
                                },
                                modifier = Modifier.width(32.dp)
                            )
                            Text(
                                text = row.party.members.firstOrNull()?.handle ?: "?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CfTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = row.points.toInt().toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = VerdictOK,
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = row.penalty.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = CfTextSecondary,
                                modifier = Modifier.width(40.dp)
                            )
                        }
                        HorizontalDivider(color = CfDivider.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}
