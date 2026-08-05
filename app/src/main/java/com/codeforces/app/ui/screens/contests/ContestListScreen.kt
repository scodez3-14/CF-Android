package com.codeforces.app.ui.screens.contests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
fun ContestListScreen(
    navController: NavController,
    viewModel: ContestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Past")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contests", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CfSurface,
                contentColor = CodeforcesAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CodeforcesAccent
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CodeforcesAccent)
                }
                return@Column
            }
            val list = if (selectedTab == 0) state.upcoming else state.past
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(list, key = { it.id }) { contest ->
                    ContestListCard(contest = contest, isUpcoming = selectedTab == 0, onClick = {
                        navController.navigate(Screen.ContestDetail.createRoute(contest.id))
                    })
                }
            }
        }
    }
}

@Composable
fun ContestListCard(contest: ContestDto, isUpcoming: Boolean, onClick: () -> Unit) {
    val fmt = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val startStr = contest.startTimeSeconds?.let { fmt.format(Date(it * 1000)) } ?: "TBD"
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = if (isUpcoming) CodeforcesAccent else CfTextSecondary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(contest.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = CfTextPrimary)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = CfTextSecondary, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(startStr, style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
                    }
                    Text("${contest.durationSeconds / 3600}h", style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
                    Text(contest.type, style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
                }
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = CfTextSecondary)
        }
    }
}
