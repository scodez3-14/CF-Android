package com.codeforces.app.ui.screens.leaderboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.codeforces.app.ui.navigation.Screen
import com.codeforces.app.ui.screens.profile.rankColor
import com.codeforces.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Rated", fontWeight = FontWeight.Bold) },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Active only", style = MaterialTheme.typography.labelMedium, color = CfTextSecondary)
                        Switch(
                            checked = state.activeOnly,
                            onCheckedChange = { viewModel.load(it) },
                            modifier = Modifier.padding(horizontal = 8.dp),
                            colors = SwitchDefaults.colors(checkedThumbColor = CodeforcesAccent, checkedTrackColor = CodeforcesAccent.copy(alpha = 0.5f))
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CodeforcesAccent)
                    Spacer(Modifier.height(12.dp))
                    Text("Loading leaderboard…", color = CfTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            return@Scaffold
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(vertical = 8.dp)) {
            itemsIndexed(state.users, key = { _, u -> u.handle }) { index, user ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { navController.navigate(Screen.Profile.createRoute(user.handle)) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "#${index + 1}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = when (index) {
                            0 -> CodeforcesAccent
                            1 -> CfTextPrimary
                            2 -> CfTextSecondary
                            else -> CfTextDisabled
                        },
                        modifier = Modifier.width(40.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.handle, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = CfTextPrimary)
                        Text(
                            text = buildString {
                                user.country?.let { append("🌍 $it") }
                                if (!user.country.isNullOrEmpty() && !user.organization.isNullOrEmpty()) append(" · ")
                                user.organization?.let { append(it) }
                            }.ifBlank { user.rank?.replaceFirstChar { c -> c.uppercase() } ?: "" },
                            style = MaterialTheme.typography.labelSmall,
                            color = CfTextSecondary
                        )
                    }
                    Text(
                        text = user.rating.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = rankColor(user.rank)
                    )
                }
                HorizontalDivider(color = CfDivider.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
