package com.codeforces.app.ui.screens.problems

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.codeforces.app.data.api.ProblemDto
import com.codeforces.app.ui.navigation.Screen
import com.codeforces.app.ui.theme.*
import kotlin.math.roundToInt

fun ratingColor(rating: Int?): Color = when {
    rating == null -> CfTextSecondary
    rating < 1200 -> RatingNewbie
    rating < 1400 -> RatingPupil
    rating < 1600 -> RatingSpecialist
    rating < 1900 -> RatingExpert
    rating < 2100 -> RatingCM
    rating < 2400 -> RatingIM
    else -> RatingGM
}

fun ratingLabel(rating: Int?): String = when {
    rating == null -> "?"
    rating < 1200 -> "Newbie"
    rating < 1400 -> "Pupil"
    rating < 1600 -> "Specialist"
    rating < 1900 -> "Expert"
    rating < 2100 -> "C.Master"
    rating < 2400 -> "I.Master"
    else -> "GM+"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemsScreen(
    navController: NavController,
    viewModel: ProblemsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }

    val activeFilterCount = state.selectedTags.size + if (state.ratingFilterEnabled) 1 else 0

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(CfSurface)) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Problem Set", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            if (!state.isLoading) {
                                Text(
                                    "${state.filteredProblems.size} of ${state.problems.size} problems",
                                    fontSize = 12.sp,
                                    color = CfTextSecondary
                                )
                            }
                        }
                    },
                    actions = {
                        // Filter button with badge
                        Box {
                            IconButton(onClick = { showFilters = !showFilters }) {
                                Icon(
                                    if (showFilters) Icons.Rounded.FilterListOff else Icons.Rounded.FilterList,
                                    contentDescription = "Filter",
                                    tint = if (activeFilterCount > 0) CodeforcesRed else CfTextSecondary
                                )
                            }
                            if (activeFilterCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .clip(CircleShape)
                                        .background(CodeforcesRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "$activeFilterCount",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        // Refresh
                        IconButton(onClick = { viewModel.loadProblems() }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = CfTextSecondary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
                )

                // Search bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("Search by name or contest ID…", color = CfTextSecondary) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = CfTextSecondary) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = CfTextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CodeforcesRed,
                        unfocusedBorderColor = CfDivider,
                        focusedContainerColor = CfCardSurface,
                        unfocusedContainerColor = CfCardSurface,
                        cursorColor = CodeforcesRed
                    )
                )

                // Collapsible filter panel
                AnimatedVisibility(
                    visible = showFilters,
                    enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
                ) {
                    FilterPanel(
                        allTags = viewModel.allTags,
                        selectedTags = state.selectedTags,
                        onTagToggle = { viewModel.toggleTag(it) },
                        onClearTags = { viewModel.clearTags() },
                        ratingEnabled = state.ratingFilterEnabled,
                        onRatingEnabledChange = { viewModel.setRatingFilterEnabled(it) },
                        minRating = state.minRating,
                        maxRating = state.maxRating,
                        onRatingChange = { min, max -> viewModel.setRatingRange(min, max) }
                    )
                }

                HorizontalDivider(color = CfDivider)
            }
        },
        containerColor = CfBackground
    ) { padding ->
        when {
            state.isLoading && state.problems.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(color = CodeforcesRed, strokeWidth = 3.dp)
                        Text("Fetching problems…", color = CfTextSecondary, fontSize = 14.sp)
                    }
                }
            }
            state.error != null && state.problems.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.WifiOff, contentDescription = null, tint = CodeforcesRed, modifier = Modifier.size(48.dp))
                        Text("Failed to load", color = CfTextPrimary, fontWeight = FontWeight.Bold)
                        Text(state.error ?: "", color = CfTextSecondary, fontSize = 13.sp)
                        Button(onClick = { viewModel.loadProblems() }, colors = ButtonDefaults.buttonColors(containerColor = CodeforcesRed)) {
                            Text("Retry")
                        }
                    }
                }
            }
            state.filteredProblems.isEmpty() && !state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.SearchOff, contentDescription = null, tint = CfTextSecondary, modifier = Modifier.size(48.dp))
                        Text("No problems match", color = CfTextPrimary, fontWeight = FontWeight.Bold)
                        Text("Try adjusting your filters", color = CfTextSecondary, fontSize = 13.sp)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredProblems, key = { "${it.contestId}_${it.index}" }) { problem ->
                        val solvedCount = state.statistics.find {
                            it.contestId == problem.contestId && it.index == problem.index
                        }?.solvedCount ?: 0
                        ProblemCard(
                            problem = problem,
                            solvedCount = solvedCount,
                            onClick = {
                                navController.navigate(
                                    Screen.ProblemDetail.createRoute(
                                        problem.contestId?.toString() ?: "0",
                                        problem.index,
                                        problem.name
                                    )
                                )
                            }
                        )
                    }
                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

// ─── Filter Panel ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterPanel(
    allTags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    onClearTags: () -> Unit,
    ratingEnabled: Boolean,
    onRatingEnabledChange: (Boolean) -> Unit,
    minRating: Int,
    maxRating: Int,
    onRatingChange: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CfSurface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tags section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Tags",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = CfTextSecondary
            )
            if (selectedTags.isNotEmpty()) {
                TextButton(
                    onClick = onClearTags,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("Clear (${selectedTags.size})", color = CodeforcesRed, fontSize = 12.sp)
                }
            }
        }

        // Tags in a wrapping flow layout (simulated with multiple rows)
        val chunked = allTags.chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            chunked.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { tag ->
                        val selected = tag in selectedTags
                        FilterChip(
                            selected = selected,
                            onClick = { onTagToggle(tag) },
                            label = { Text(tag, fontSize = 11.sp, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CodeforcesRed.copy(alpha = 0.2f),
                                selectedLabelColor = CfRedLight,
                                containerColor = CfCardSurface,
                                labelColor = CfTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = CodeforcesRed.copy(alpha = 0.6f),
                                borderColor = CfDivider
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = CfDivider)

        // Rating range section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Rating Range", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CfTextSecondary)
            Switch(
                checked = ratingEnabled,
                onCheckedChange = onRatingEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = CodeforcesRed
                )
            )
        }

        AnimatedVisibility(visible = ratingEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RatingBadgeSmall(minRating)
                    Text("→", color = CfTextSecondary, fontSize = 14.sp)
                    RatingBadgeSmall(maxRating)
                }
                var sliderValues by remember(minRating, maxRating) {
                    mutableStateOf(minRating.toFloat()..maxRating.toFloat())
                }
                RangeSlider(
                    value = sliderValues,
                    onValueChange = { range ->
                        sliderValues = range
                        val newMin = (range.start / 100).roundToInt() * 100
                        val newMax = (range.endInclusive / 100).roundToInt() * 100
                        onRatingChange(newMin.coerceAtLeast(800), newMax.coerceAtMost(3500))
                    },
                    valueRange = 800f..3500f,
                    steps = 26,
                    colors = SliderDefaults.colors(
                        thumbColor = CodeforcesRed,
                        activeTrackColor = CodeforcesRed,
                        inactiveTrackColor = CfDivider
                    )
                )
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun RatingBadgeSmall(rating: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ratingColor(rating).copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            "$rating",
            color = ratingColor(rating),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─── Problem Card ──────────────────────────────────────────────────────────────

@Composable
fun ProblemCard(problem: ProblemDto, solvedCount: Int, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val color = ratingColor(problem.rating)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rating badge — left accent strip + number
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(color.copy(alpha = 0.25f), color.copy(alpha = 0.10f))
                        )
                    )
                    .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = problem.rating?.toString() ?: "?",
                        color = color,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Middle: ID + name + tags
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                // Contest ID + index
                Text(
                    text = "${problem.contestId ?: ""}${problem.index}",
                    fontSize = 11.sp,
                    color = CodeforcesRed,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                // Problem name
                Text(
                    text = problem.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = CfTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Tags
                if (problem.tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        problem.tags.take(4).forEach { tag ->
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                color = CfTextSecondary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CfDivider.copy(alpha = 0.8f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        if (problem.tags.size > 4) {
                            Text(
                                text = "+${problem.tags.size - 4}",
                                fontSize = 10.sp,
                                color = CfTextDisabled,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Right: solved count + arrow
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (solvedCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Rounded.People, contentDescription = null, tint = CfTextDisabled, modifier = Modifier.size(12.dp))
                        Text(
                            text = if (solvedCount >= 1000) "${solvedCount / 1000}k" else "$solvedCount",
                            fontSize = 11.sp,
                            color = CfTextDisabled
                        )
                    }
                }
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = CfTextDisabled,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
