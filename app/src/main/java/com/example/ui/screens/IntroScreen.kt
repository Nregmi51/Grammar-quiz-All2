package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.GrammarUiState
import com.example.ui.SyncUiState
import com.example.ui.components.DeepBlueFontBadge
import com.example.ui.components.DeepBlueFontHeader
import com.example.ui.components.DeepBlueRuleHighlight
import com.example.ui.components.YellowContentPlate
import com.example.ui.theme.DeepBlueBanner
import com.example.ui.theme.DeepBlueInk
import com.example.ui.theme.DeepBlueSecondaryInk
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OnDeepBlueText
import com.example.ui.theme.PlateBorderGold
import com.example.ui.theme.PlateYellowElevated
import com.example.ui.theme.SuccessEmerald

@Composable
fun IntroScreen(
    uiState: GrammarUiState,
    contentPadding: PaddingValues,
    onGitHubUrlChanged: (String) -> Unit,
    onSyncGitHub: () -> Unit,
    onResetGitHub: () -> Unit,
    onDismissSyncBanner: () -> Unit,
    onNavigateToContent: () -> Unit,
    onNavigateToPlayWatch: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_grammar_hero),
                    contentDescription = "Grammar Craft studio illustration with books and fountain pen",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepBlueBanner.copy(alpha = 0.28f))
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DeepBlueBanner.copy(alpha = 0.92f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Grammar Craft",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnDeepBlueText
                    )
                    Text(
                        text = "Developed by NP Regmi • Grammar HTML Studio",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldAccent
                    )
                }
            }

            // Intro Overview Plate (Slightly Yellow Plate + Deep Blue Fonts/Backgrounds)
            YellowContentPlate {
                DeepBlueFontHeader(
                    title = "Welcome to Your Grammar Studio",
                    subtitle = "Revised & grouped by grammar topic",
                    icon = Icons.AutoMirrored.Filled.MenuBook
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Explore reading rules, design compositions, and interactive HTML quizzes on mobile. Load your own HTML collection from GitHub anytime — each lesson is revised, grouped by grammar topic, and played with its original HTML background format intact.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DeepBlueInk
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DeepBlueRuleHighlight(
                        indexLabel = "01",
                        text = "Intro & GitHub Sync: Link a public GitHub repo, folder, or raw HTML URL to import your files."
                    )
                    DeepBlueRuleHighlight(
                        indexLabel = "02",
                        text = "Content Plates: HTML lessons are revised into summaries & key rules and grouped by grammar topic."
                    )
                    DeepBlueRuleHighlight(
                        indexLabel = "03",
                        text = "Play or Watch: Run interactive quizzes or auto-watch topic decks keeping the HTML's exact background format."
                    )
                }
            }

            // GitHub Repository Import Plate
            YellowContentPlate(elevated = true) {
                DeepBlueFontHeader(
                    title = "Import HTMLs from GitHub",
                    subtitle = if (uiState.savedRepoLabel.isNotBlank()) {
                        "Connected: ${uiState.savedRepoLabel}"
                    } else {
                        "Paste your GitHub link when ready"
                    },
                    icon = Icons.Default.CloudDownload
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Supports repo URLs (github.com/user/repo), folder paths (/tree/main/grammar), single files, or owner/repo shorthand.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepBlueSecondaryInk
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.gitHubInputUrl,
                    onValueChange = onGitHubUrlChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("github_url_input"),
                    label = { Text("GitHub Repository or HTML URL") },
                    placeholder = { Text("https://github.com/username/grammar-htmls") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DeepBlueInk,
                        unfocusedTextColor = DeepBlueInk,
                        focusedBorderColor = DeepBlueBanner,
                        unfocusedBorderColor = PlateBorderGold,
                        focusedLabelColor = DeepBlueBanner,
                        unfocusedLabelColor = DeepBlueSecondaryInk,
                        focusedContainerColor = PlateYellowElevated,
                        unfocusedContainerColor = PlateYellowElevated
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSyncGitHub,
                        enabled = uiState.syncStatus !is SyncUiState.Syncing,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("sync_github_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepBlueBanner,
                            contentColor = OnDeepBlueText
                        )
                    ) {
                        if (uiState.syncStatus is SyncUiState.Syncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = OnDeepBlueText
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fetch & Group HTMLs")
                        }
                    }

                    if (uiState.githubItemCount > 0) {
                        OutlinedButton(
                            onClick = onResetGitHub,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("reset_github_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset GitHub imports",
                                tint = DeepBlueBanner,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = uiState.syncStatus is SyncUiState.Success || uiState.syncStatus is SyncUiState.Error) {
                    val isError = uiState.syncStatus is SyncUiState.Error
                    val msg = when (val s = uiState.syncStatus) {
                        is SyncUiState.Success -> s.message
                        is SyncUiState.Error -> s.message
                        else -> ""
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isError) ErrorCoral else SuccessEmerald)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnDeepBlueText,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissSyncBanner,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss sync message",
                                tint = OnDeepBlueText
                            )
                        }
                    }
                }
            }

            // Progress & Quick Jump Plate
            YellowContentPlate {
                DeepBlueFontHeader(
                    title = "Ready to Practice",
                    subtitle = "${uiState.topicGroups.size} grammar topics organized",
                    icon = Icons.Default.AutoAwesome
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeepBlueFontBadge(
                        text = "${uiState.topicGroups.size} Topics",
                        icon = Icons.Default.Topic
                    )
                    DeepBlueFontBadge(
                        text = "${uiState.totalCount} Modules",
                        icon = Icons.AutoMirrored.Filled.MenuBook
                    )
                    DeepBlueFontBadge(
                        text = "${uiState.completedCount} Done",
                        backgroundColor = SuccessEmerald,
                        icon = Icons.Default.CheckCircle
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToContent,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("browse_content_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldDeep,
                            contentColor = OnDeepBlueText
                        )
                    ) {
                        Text(
                            text = "Explore Content",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onNavigateToPlayWatch,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("launch_play_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepBlueBanner,
                            contentColor = OnDeepBlueText
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Play / Watch",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                DeepBlueFontBadge(
                    text = "Developed by NP Regmi",
                    backgroundColor = DeepBlueBanner,
                    contentColor = GoldAccent
                )
            }
        }
    }
}
