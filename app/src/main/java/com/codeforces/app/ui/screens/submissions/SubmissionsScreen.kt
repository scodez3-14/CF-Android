package com.codeforces.app.ui.screens.submissions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.codeforces.app.ui.screens.profile.SubmissionRow
import com.codeforces.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionsScreen(
    handle: String,
    onBack: () -> Unit,
    viewModel: SubmissionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val verdictOptions = listOf(null, "OK", "WRONG_ANSWER", "TIME_LIMIT_EXCEEDED", "RUNTIME_ERROR", "COMPILATION_ERROR")
    val verdictLabels = listOf("All", "AC", "WA", "TLE", "RTE", "CE")

    LaunchedEffect(handle) { viewModel.load(handle) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submissions · $handle", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Verdict filter row
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                verdictOptions.forEachIndexed { i, verdict ->
                    FilterChip(
                        selected = state.verdictFilter == verdict,
                        onClick = { viewModel.setFilter(verdict) },
                        label = { Text(verdictLabels[i], fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CodeforcesAccent.copy(alpha = 0.2f),
                            selectedLabelColor = CfAccentLight
                        )
                    )
                }
            }
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CodeforcesAccent)
                }
                return@Column
            }
            LazyColumn {
                items(state.filtered, key = { it.id }) { sub ->
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
