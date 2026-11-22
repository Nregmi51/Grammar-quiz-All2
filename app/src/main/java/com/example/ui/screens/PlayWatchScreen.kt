package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.GrammarItem
import com.example.ui.GrammarUiState
import com.example.ui.components.DeepBlueFontBadge
import com.example.ui.components.DeepBlueFontHeader
import com.example.ui.components.YellowContentPlate
import com.example.ui.theme.DeepBlueBanner
import com.example.ui.theme.DeepBlueInk
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OnDeepBlueText
import com.example.ui.theme.PlateBorderGold
import com.example.ui.theme.PlateYellow
import com.example.ui.theme.SuccessEmerald

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PlayWatchScreen(
    uiState: GrammarUiState,
    contentPadding: PaddingValues,
    onSelectItem: (String) -> Unit,
    onExitTopicDeck: () -> Unit,
    onPreviousItem: () -> Unit,
    onNextItem: () -> Unit,
    onToggleWatchAutoPlay: () -> Unit,
    onAdjustZoom: (Int) -> Unit,
    onToggleCompleted: (GrammarItem) -> Unit
) {
    val activeItem = uiState.activeItem
    val htmlToRender = uiState.currentPlayHtml
    val context = LocalContext.current

    var pageProgress by remember { mutableIntStateOf(100) }
    var showOnlyActiveTopicInStrip by remember { mutableStateOf(true) }

    val webView = remember(context) {
        WebView(context).apply {
            setBackgroundColor(AndroidColor.TRANSPARENT)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowContentAccess = true
                useWideViewPort = true
                loadWithOverviewMode = true
                builtInZoomControls = true
                displayZoomControls = false
                mediaPlaybackRequiresUserGesture = false
                javaScriptCanOpenWindowsAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    pageProgress = newProgress
                }
            }
        }
    }

    var lastLoadedHtml by remember { mutableStateOf("") }

    LaunchedEffect(htmlToRender) {
        if (htmlToRender.isNotBlank() && htmlToRender != lastLoadedHtml) {
            lastLoadedHtml = htmlToRender
            pageProgress = 15
            webView.loadDataWithBaseURL(
                "https://grammarcraft.local/",
                htmlToRender,
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    LaunchedEffect(uiState.webTextZoomPercent) {
        webView.settings.textZoom = uiState.webTextZoomPercent
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    val stripItems = remember(uiState.allItems, activeItem?.grammarTopic, showOnlyActiveTopicInStrip) {
        val topic = activeItem?.grammarTopic
        if (showOnlyActiveTopicInStrip && topic != null) {
            uiState.allItems.filter { it.grammarTopic == topic }.ifEmpty { uiState.allItems }
        } else {
            uiState.allItems
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Play / Watch Header Plate
            YellowContentPlate {
                DeepBlueFontHeader(
                    title = uiState.currentPlayTitle,
                    subtitle = if (uiState.activeTopicDeck != null) {
                        "Grouped Topic Deck • Preserving each HTML background"
                    } else {
                        "${activeItem?.grammarTopic ?: "Grammar"} • ${activeItem?.category?.label ?: ""}"
                    },
                    icon = Icons.Default.SmartDisplay,
                    trailing = {
                        if (uiState.activeTopicDeck != null) {
                            Button(
                                onClick = onExitTopicDeck,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PlateYellow,
                                    contentColor = DeepBlueInk
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Single View", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DeepBlueFontBadge(
                        text = "Mobile-Adapted • Bg Kept",
                        icon = Icons.Default.Palette,
                        backgroundColor = EmeraldDeep
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (uiState.isWatchAutoPlay) {
                            DeepBlueFontBadge(
                                text = "Watch • ${uiState.watchCountdownSec}s",
                                backgroundColor = GoldAccent,
                                contentColor = DeepBlueInk
                            )
                        }
                        Button(
                            onClick = onToggleWatchAutoPlay,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("toggle_watch_autoplay_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isWatchAutoPlay) EmeraldDeep else DeepBlueBanner,
                                contentColor = OnDeepBlueText
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isWatchAutoPlay) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (uiState.isWatchAutoPlay) "Pause Watch" else "Auto-Watch",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // Quick module switcher strip scoped to topic or all
                if (uiState.allItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = showOnlyActiveTopicInStrip,
                                onClick = { showOnlyActiveTopicInStrip = !showOnlyActiveTopicInStrip },
                                label = {
                                    Text(
                                        text = if (showOnlyActiveTopicInStrip) "Topic Only" else "All (${uiState.allItems.size})"
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldDeep,
                                    selectedLabelColor = OnDeepBlueText,
                                    labelColor = DeepBlueInk
                                )
                            )
                        }
                        items(stripItems, key = { it.id }) { item ->
                            val isSelected = uiState.activeTopicDeck == null && activeItem?.id == item.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectItem(item.id) },
                                label = {
                                    Text(
                                        text = item.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepBlueBanner,
                                    selectedLabelColor = OnDeepBlueText,
                                    labelColor = DeepBlueInk
                                )
                            )
                        }
                    }
                }
            }

            // HTML WebView Stage — preserves HTML's own background format + mobile responsive CSS
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("html_player_webview_container"),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, PlateBorderGold),
                color = PlateYellow,
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (pageProgress in 1..99) {
                        LinearProgressIndicator(
                            progress = { pageProgress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = EmeraldDeep,
                            trackColor = PlateYellow
                        )
                    }
                    if (htmlToRender.isBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Choose any HTML lesson from Content or Intro to Play or Watch.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = DeepBlueInk,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    } else {
                        AndroidView(
                            factory = { webView },
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }
                }
            }

            // Bottom Player Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(DeepBlueBanner)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPreviousItem,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("prev_module_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                            contentDescription = "Previous HTML module",
                            tint = OnDeepBlueText
                        )
                    }
                    IconButton(
                        onClick = onNextItem,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("next_module_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = "Next HTML module",
                            tint = OnDeepBlueText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onAdjustZoom(-10) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("zoom_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Decrease HTML text size",
                            tint = OnDeepBlueText
                        )
                    }
                    Text(
                        text = "${uiState.webTextZoomPercent}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldAccent
                    )
                    IconButton(
                        onClick = { onAdjustZoom(10) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("zoom_in_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Increase HTML text size",
                            tint = OnDeepBlueText
                        )
                    }
                }

                if (activeItem != null && uiState.activeTopicDeck == null) {
                    Button(
                        onClick = { onToggleCompleted(activeItem) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_complete_play_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeItem.isCompleted) SuccessEmerald else PlateYellow,
                            contentColor = if (activeItem.isCompleted) OnDeepBlueText else DeepBlueInk
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (activeItem.isCompleted) "Done" else "Complete",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
