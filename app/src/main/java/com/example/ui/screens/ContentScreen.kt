package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.GrammarCategory
import com.example.model.GrammarItem
import com.example.model.SourceOrigin
import com.example.ui.GrammarUiState
import com.example.ui.components.DeepBlueFontBadge
import com.example.ui.components.DeepBlueFontHeader
import com.example.ui.components.DeepBlueRuleHighlight
import com.example.ui.components.YellowContentPlate
import com.example.ui.theme.DeepBlueBanner
import com.example.ui.theme.DeepBlueInk
import com.example.ui.theme.DeepBluePill
import com.example.ui.theme.DeepBlueSecondaryInk
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OnDeepBlueText
import com.example.ui.theme.PlateBorderGold
import com.example.ui.theme.PlateYellow
import com.example.ui.theme.PlateYellowElevated
import com.example.ui.theme.SuccessEmerald

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentScreen(
    uiState: GrammarUiState,
    contentPadding: PaddingValues,
    onSearchQueryChanged: (String) -> Unit,
    onSelectTopicFilter: (String?) -> Unit,
    onSelectCategoryFilter: (GrammarCategory?) -> Unit,
    onOpenItemPlayWatch: (String) -> Unit,
    onOpenTopicDeckPlayWatch: (String) -> Unit,
    onToggleCompleted: (GrammarItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search & Topic Filters Plate
            item {
                YellowContentPlate {
                    DeepBlueFontHeader(
                        title = "Revised Grammar Content",
                        subtitle = "Grouped by grammar topic • Tap any card to Play or Watch",
                        icon = Icons.Default.Topic
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("content_search_input"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search grammar topics",
                                tint = DeepBlueBanner
                            )
                        },
                        placeholder = { Text("Search tenses, rules, quizzes, compositions...") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepBlueInk,
                            unfocusedTextColor = DeepBlueInk,
                            focusedBorderColor = DeepBlueBanner,
                            unfocusedBorderColor = PlateBorderGold,
                            focusedContainerColor = PlateYellowElevated,
                            unfocusedContainerColor = PlateYellowElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == null,
                            onClick = { onSelectCategoryFilter(null) },
                            label = { Text("All Types") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepBlueBanner,
                                selectedLabelColor = OnDeepBlueText,
                                labelColor = DeepBlueInk
                            )
                        )
                        GrammarCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = uiState.selectedCategoryFilter == cat,
                                onClick = { onSelectCategoryFilter(cat) },
                                label = { Text(cat.shortLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepBlueBanner,
                                    selectedLabelColor = OnDeepBlueText,
                                    labelColor = DeepBlueInk
                                )
                            )
                        }
                    }

                    // Grammar topic chips
                    if (uiState.topicGroups.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.selectedTopicFilter == null,
                                onClick = { onSelectTopicFilter(null) },
                                label = { Text("All Topics") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldDeep,
                                    selectedLabelColor = OnDeepBlueText,
                                    labelColor = DeepBlueInk
                                )
                            )
                            uiState.topicGroups.forEach { group ->
                                FilterChip(
                                    selected = uiState.selectedTopicFilter == group.topic,
                                    onClick = { onSelectTopicFilter(group.topic) },
                                    label = { Text("${group.topic} (${group.items.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldDeep,
                                        selectedLabelColor = OnDeepBlueText,
                                        labelColor = DeepBlueInk
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.filteredTopicGroups.isEmpty()) {
                item {
                    YellowContentPlate {
                        DeepBlueFontHeader(
                            title = "No Matching Grammar Modules",
                            icon = Icons.Default.Search
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Try clearing your search or topic filter, or sync an HTML repository from the Intro tab.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepBlueInk
                        )
                    }
                }
            }

            // Topic-grouped plates
            uiState.filteredTopicGroups.forEach { group ->
                item(key = "header_${group.topic}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepBlueBanner)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group.topic,
                                style = MaterialTheme.typography.titleLarge,
                                color = OnDeepBlueText
                            )
                            Text(
                                text = "${group.items.size} module(s) • ${group.completedCount} completed",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onOpenTopicDeckPlayWatch(group.topic) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("play_topic_deck_${group.topic}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PlateYellow,
                                contentColor = DeepBlueInk
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Play Topic",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                items(
                    items = group.items,
                    key = { it.id }
                ) { item ->
                    GrammarModulePlate(
                        item = item,
                        onOpen = { onOpenItemPlayWatch(item.id) },
                        onToggleDone = { onToggleCompleted(item) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrammarModulePlate(
    item: GrammarItem,
    onOpen: () -> Unit,
    onToggleDone: () -> Unit
) {
    YellowContentPlate(
        modifier = Modifier
            .clickable(onClick = onOpen)
            .testTag("grammar_card_${item.id}")
    ) {
        // Deep blue title plate for the content font
        DeepBlueFontHeader(
            title = item.title,
            subtitle = item.grammarTopic
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Metadata badges
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DeepBlueFontBadge(text = item.category.label)
            DeepBlueFontBadge(
                text = item.sourceOrigin.label,
                backgroundColor = if (item.sourceOrigin == SourceOrigin.GITHUB) EmeraldDeep else DeepBluePill
            )
            if (item.hasExplicitBackground) {
                DeepBlueFontBadge(
                    text = "HTML Bg Format Kept",
                    icon = Icons.Default.Palette,
                    backgroundColor = DeepBluePill
                )
            }
            if (item.isCompleted) {
                DeepBlueFontBadge(
                    text = "Revised",
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = SuccessEmerald
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = item.revisedSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = DeepBlueInk
        )

        if (item.keyPoints.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item.keyPoints.take(3).forEachIndexed { idx, point ->
                    DeepBlueRuleHighlight(
                        indexLabel = "R${idx + 1}",
                        text = point
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpen,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("play_item_${item.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepBlueBanner,
                    contentColor = OnDeepBlueText
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (item.category == GrammarCategory.QUIZ) "Play Quiz" else "Play / Watch",
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onToggleDone,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("toggle_done_${item.id}"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (item.isCompleted) "Mark uncompleted" else "Mark completed",
                    tint = if (item.isCompleted) SuccessEmerald else DeepBlueSecondaryInk,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (item.isCompleted) "Done" else "Mark Done",
                    color = DeepBlueInk
                )
            }
        }
    }
}
