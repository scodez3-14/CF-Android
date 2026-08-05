package com.codeforces.app.ui.screens.problems

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.geometry.Offset
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
                                    tint = if (activeFilterCount > 0) CodeforcesAccent else CfTextSecondary
                                )
                            }
                            if (activeFilterCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .clip(CircleShape)
                                        .background(CodeforcesAccent),
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
                        IconButton(onClick = { viewModel.loadProblems(forceRefresh = true) }) {
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
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CodeforcesAccent,
                        unfocusedBorderColor = CfDivider,
                        focusedContainerColor = CfCardSurface,
                        unfocusedContainerColor = CfCardSurface,
                        cursorColor = CodeforcesAccent
                    )
                )

                QuickRatingFilters(
                    ratingEnabled = state.ratingFilterEnabled,
                    minRating = state.minRating,
                    maxRating = state.maxRating,
                    savedOnly = state.savedOnly,
                    onSavedOnlyChange = { viewModel.setSavedOnly(it) },
                    onSelectRange = { min, max ->
                        viewModel.setRatingFilterEnabled(true)
                        viewModel.setRatingRange(min, max)
                    },
                    onShowAll = { viewModel.clearFilters() }
                )

                AnimatedVisibility(visible = activeFilterCount > 0) {
                    ActiveFiltersRow(
                        selectedTags = state.selectedTags,
                        ratingEnabled = state.ratingFilterEnabled,
                        minRating = state.minRating,
                        maxRating = state.maxRating,
                        onRemoveTag = { viewModel.toggleTag(it) },
                        onClearAll = { viewModel.clearFilters() }
                    )
                }

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
                        onClearAll = { viewModel.clearFilters() },
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
                val brush = rememberShimmerBrush()
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(8) { ProblemCardSkeleton(brush) }
                }
            }
            state.error != null && state.problems.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.WifiOff, contentDescription = null, tint = CodeforcesAccent, modifier = Modifier.size(48.dp))
                        Text("Failed to load", color = CfTextPrimary, fontWeight = FontWeight.Bold)
                        Text(state.error ?: "", color = CfTextSecondary, fontSize = 13.sp)
                        Button(onClick = { viewModel.loadProblems() }, colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent)) {
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
                val groups = remember(state.filteredProblems) {
                    state.filteredProblems
                        .groupBy { it.contestId }
                        .toList()
                        .sortedByDescending { it.first ?: Int.MAX_VALUE }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    groups.forEach { (contestId, problems) ->
                        stickyHeader(key = "header_${contestId ?: "practice"}") {
                            val ratings = problems.mapNotNull { it.rating }
                            ContestHeader(
                                name = contestId?.let { state.contestNames[it] },
                                count = problems.size,
                                range = if (ratings.isNotEmpty()) "${ratings.min()}–${ratings.max()}" else null,
                                tint = if (ratings.isNotEmpty()) ratingColor(ratings.min()) else CfTextSecondary
                            )
                        }
                        items(problems, key = { "${it.contestId}_${it.index}" }) { problem ->
                            val solvedCount = state.statisticsByProblem["${problem.contestId}_${problem.index}"] ?: 0
                            val problemId = "${problem.contestId}_${problem.index}"
                            ProblemCard(
                                problem = problem,
                                solvedCount = solvedCount,
                                isBookmarked = problemId in state.bookmarks,
                                onBookmarkToggle = { viewModel.toggleBookmark(problemId) },
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
                    }
                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickRatingFilters(
    ratingEnabled: Boolean,
    minRating: Int,
    maxRating: Int,
    savedOnly: Boolean,
    onSavedOnlyChange: (Boolean) -> Unit,
    onSelectRange: (Int, Int) -> Unit,
    onShowAll: () -> Unit
) {
    val ranges = listOf(
        "All" to (800 to 3500),
        "800–1200" to (800 to 1200),
        "1200–1600" to (1200 to 1600),
        "1600–2000" to (1600 to 2000),
        "2000+" to (2000 to 3500)
    )
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = savedOnly,
                onClick = { onSavedOnlyChange(!savedOnly) },
                label = { Text("Saved", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        if (savedOnly) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CodeforcesAccent.copy(alpha = 0.18f),
                    selectedLabelColor = CfTextPrimary,
                    selectedLeadingIconColor = CfAccentLight,
                    containerColor = CfCardSurface,
                    labelColor = CfTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = savedOnly,
                    selectedBorderColor = CodeforcesAccent.copy(alpha = 0.65f),
                    borderColor = CfDivider
                )
            )
        }
        items(ranges) { (label, range) ->
            val isAll = label == "All"
            val selected = if (isAll) !ratingEnabled else ratingEnabled && minRating == range.first && maxRating == range.second
            FilterChip(
                selected = selected,
                onClick = { if (isAll) onShowAll() else onSelectRange(range.first, range.second) },
                label = { Text(label, fontSize = 12.sp) },
                leadingIcon = if (selected && !isAll) {
                    { Icon(Icons.Rounded.Bolt, contentDescription = null, modifier = Modifier.size(15.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CodeforcesAccent.copy(alpha = 0.18f),
                    selectedLabelColor = CfTextPrimary,
                    selectedLeadingIconColor = CfAccentLight,
                    containerColor = CfCardSurface,
                    labelColor = CfTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    selectedBorderColor = CodeforcesAccent.copy(alpha = 0.65f),
                    borderColor = CfDivider
                )
            )
        }
    }
}

@Composable
private fun ActiveFiltersRow(
    selectedTags: Set<String>,
    ratingEnabled: Boolean,
    minRating: Int,
    maxRating: Int,
    onRemoveTag: (String) -> Unit,
    onClearAll: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Text("Filters", color = CfTextDisabled, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        if (ratingEnabled) {
            item {
                InputChip(
                    selected = true,
                    onClick = onClearAll,
                    label = { Text("$minRating–$maxRating", fontSize = 11.sp) },
                    trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Clear filters", modifier = Modifier.size(14.dp)) },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = ratingColor(minRating).copy(alpha = 0.18f),
                        selectedLabelColor = CfTextPrimary,
                        selectedTrailingIconColor = CfTextSecondary
                    )
                )
            }
        }
        items(selectedTags.toList(), key = { it }) { tag ->
            InputChip(
                selected = true,
                onClick = { onRemoveTag(tag) },
                label = { Text(tag, fontSize = 11.sp) },
                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Remove $tag", modifier = Modifier.size(14.dp)) },
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = CodeforcesAccent.copy(alpha = 0.16f),
                    selectedLabelColor = CfTextPrimary,
                    selectedTrailingIconColor = CfTextSecondary
                )
            )
        }
        item {
            TextButton(onClick = onClearAll, contentPadding = PaddingValues(horizontal = 6.dp)) {
                Text("Reset", color = CfAccentLight, fontSize = 12.sp)
            }
        }
    }
}

// ─── Filter Panel ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterPanel(
    allTags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    onClearAll: () -> Unit,
    ratingEnabled: Boolean,
    onRatingEnabledChange: (Boolean) -> Unit,
    minRating: Int,
    maxRating: Int,
    onRatingChange: (Int, Int) -> Unit
) {
    var showAllTags by remember { mutableStateOf(false) }
    val visibleTags = if (showAllTags) allTags else allTags.take(12)
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
            if (selectedTags.isNotEmpty() || ratingEnabled) {
                TextButton(
                    onClick = onClearAll,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("Reset filters", color = CodeforcesAccent, fontSize = 12.sp)
                }
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            visibleTags.forEach { tag ->
                val selected = tag in selectedTags
                FilterChip(
                    selected = selected,
                    onClick = { onTagToggle(tag) },
                    label = { Text(tag, fontSize = 11.sp, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CodeforcesAccent.copy(alpha = 0.2f),
                        selectedLabelColor = CfAccentLight,
                        containerColor = CfCardSurface,
                        labelColor = CfTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        selectedBorderColor = CodeforcesAccent.copy(alpha = 0.6f),
                        borderColor = CfDivider
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }
        }
        TextButton(onClick = { showAllTags = !showAllTags }, contentPadding = PaddingValues(0.dp)) {
            Text(if (showAllTags) "Show fewer tags" else "Show all ${allTags.size} tags", color = CfAccentLight, fontSize = 12.sp)
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
                    checkedTrackColor = CodeforcesAccent
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
                        thumbColor = CodeforcesAccent,
                        activeTrackColor = CodeforcesAccent,
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
fun ProblemCard(
    problem: ProblemDto,
    solvedCount: Int,
    onClick: () -> Unit,
    isBookmarked: Boolean = false,
    onBookmarkToggle: (() -> Unit)? = null
) {
    val color = ratingColor(problem.rating)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                // Index badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.15f))
                        .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = problem.index,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = color
                    )
                }

                // Middle: name + tags
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = problem.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = CfTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (problem.tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            problem.tags.take(3).forEach { tag ->
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    color = color.copy(alpha = 0.9f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(color.copy(alpha = 0.10f))
                                        .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (problem.tags.size > 3) {
                                Text(
                                    text = "+${problem.tags.size - 3}",
                                    fontSize = 10.sp,
                                    color = CfTextDisabled,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Right: rating pill + solved count
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (problem.rating != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.15f))
                                .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = problem.rating.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = color
                            )
                        }
                    }
                    if (solvedCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Rounded.People, contentDescription = null, tint = CfTextDisabled, modifier = Modifier.size(12.dp))
                            Text(
                                text = formatSolvedCount(solvedCount),
                                fontSize = 11.sp,
                                color = CfTextDisabled
                            )
                        }
                    }
                }

                if (onBookmarkToggle != null) {
                    IconButton(
                        onClick = onBookmarkToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) CodeforcesAccent else CfTextDisabled,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }

// ─── Contest Header (sticky) ───────────────────────────────────────────────────

@Composable
private fun ContestHeader(name: String?, count: Int, range: String?, tint: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CfBackground)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = name ?: "Practice",
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = CfTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (range != null) {
            Text(
                text = range,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = tint
            )
        }
        Text(
            text = count.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = CfTextSecondary
        )
    }
}

// ─── Skeleton loading ─────────────────────────────────────────────────────────

@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerShift"
    )
    return Brush.linearGradient(
        colors = listOf(
            CfCardSurface,
            CfTextPrimary.copy(alpha = 0.06f),
            CfCardSurface
        ),
        start = Offset(shift - 600f, 0f),
        end = Offset(shift, 400f)
    )
}

@Composable
private fun ProblemCardSkeleton(brush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = CfCardSurface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(46.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(brush)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(13.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
        }
    }
}

private fun formatSolvedCount(count: Int): String = when {
    count >= 1_000_000 -> "${count / 1_000_000}M"
    count >= 10_000 -> "${count / 1_000}k"
    count >= 1_000 -> "${count / 1000}.${(count % 1000) / 100}k"
    else -> count.toString()
}
