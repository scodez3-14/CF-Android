package com.codeforces.app.ui.screens.blog

import android.content.Intent
import android.net.Uri
import android.text.Html
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeforces.app.data.api.BlogEntryDto
import com.codeforces.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogScreen(
    handle: String,
    onBack: () -> Unit,
    viewModel: BlogViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(handle) { viewModel.load(handle) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blog Entries", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CodeforcesAccent)
            }
            return@Scaffold
        }
        if (state.entries.isEmpty() && !state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No blog entries found.", color = CfTextSecondary)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.entries, key = { it.id }) { entry ->
                BlogEntryCard(entry = entry) {
                    val url = "https://codeforces.com/blog/entry/${entry.id}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
        }
    }
}

@Composable
fun BlogEntryCard(entry: BlogEntryDto, onClick: () -> Unit) {
    val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateStr = fmt.format(Date(entry.creationTimeSeconds * 1000))
    val titlePlain = Html.fromHtml(entry.title, Html.FROM_HTML_MODE_LEGACY).toString()

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titlePlain, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CfTextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = CfTextSecondary, modifier = Modifier.size(14.dp))
                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = CfTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.Star, contentDescription = null, tint = CodeforcesAccent, modifier = Modifier.size(16.dp))
                    Text(entry.rating.toString(), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CodeforcesAccent)
                }
            }
        }
    }
}
