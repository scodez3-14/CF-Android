package com.codeforces.app.ui.screens.contests

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeforces.app.ui.components.CfWebView
import com.codeforces.app.ui.components.buildEditorialHtml
import com.codeforces.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorialScreen(
    contestId: Int,
    name: String,
    onBack: () -> Unit,
    viewModel: ContestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(contestId) { viewModel.loadEditorial(contestId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = CfTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        },
        containerColor = CfBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isEditorialLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CodeforcesAccent, strokeWidth = 3.dp)
                    }
                }
                state.editorialHtml != null -> {
                    CfWebView(
                        html = buildEditorialHtml(state.editorialHtml!!),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = CodeforcesAccent,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "No editorial found",
                                color = CfTextSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "The editorial for this contest\ncould not be found on Codeforces.",
                                color = CfTextDisabled,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            TextButton(
                                onClick = { viewModel.loadEditorial(contestId) },
                                colors = ButtonDefaults.textButtonColors(contentColor = CodeforcesAccent)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}
