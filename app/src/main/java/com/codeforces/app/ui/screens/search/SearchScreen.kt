package com.codeforces.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.codeforces.app.ui.navigation.Screen
import com.codeforces.app.ui.screens.profile.rankColor
import com.codeforces.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(CfSurface)) {
                TopAppBar(
                    title = { Text("Search Users", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Leaderboard.route) }) {
                            Icon(Icons.Rounded.Leaderboard, contentDescription = "Leaderboard")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
                )
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { viewModel.onQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Enter Codeforces handle...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CodeforcesAccent, strokeWidth = 2.dp)
                        else if (state.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CodeforcesAccent),
                    shape = RoundedCornerShape(12.dp)
                )
                HorizontalDivider(color = CfDivider)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            state.error?.let {
                Card(colors = CardDefaults.cardColors(containerColor = VerdictWA.copy(alpha = 0.1f))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Error, contentDescription = null, tint = VerdictWA)
                        Spacer(Modifier.width(12.dp))
                        Text("User not found or error: $it", color = VerdictWA)
                    }
                }
            }
            state.result?.let { user ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        navController.navigate(Screen.Profile.createRoute(user.handle))
                    },
                    colors = CardDefaults.cardColors(containerColor = CfCardSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AsyncImage(
                            model = user.titlePhoto ?: user.avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(CfSurface)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.handle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CfTextPrimary)
                            Text(user.rank?.replaceFirstChar { it.uppercase() } ?: "Unrated", color = rankColor(user.rank), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            user.country?.let { Text("🌍 $it", style = MaterialTheme.typography.bodySmall, color = CfTextSecondary) }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(user.rating.toString(), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = rankColor(user.rank))
                            Text("Rating", style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
                        }
                    }
                }
            }
            if (state.query.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.PersonSearch, contentDescription = null, tint = CfTextSecondary, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Search for any Codeforces user", color = CfTextSecondary)
                        Text("Type at least 2 characters", style = MaterialTheme.typography.bodySmall, color = CfTextDisabled)
                    }
                }
            }
        }
    }
}
