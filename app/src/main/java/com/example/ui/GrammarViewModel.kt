package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GrammarRepository
import com.example.data.SyncOutcome
import com.example.data.local.GrammarDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.domain.HtmlGrammarProcessor
import com.example.model.GrammarCategory
import com.example.model.GrammarItem
import com.example.model.GrammarTopics
import com.example.model.SourceOrigin
import com.example.model.TopicGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SyncUiState {
    data object Idle : SyncUiState()
    data object Syncing : SyncUiState()
    data class Success(val message: String) : SyncUiState()
    data class Error(val message: String) : SyncUiState()
}

data class GrammarUiState(
    val allItems: List<GrammarItem> = emptyList(),
    val topicGroups: List<TopicGroup> = emptyList(),
    val filteredTopicGroups: List<TopicGroup> = emptyList(),
    val selectedTopicFilter: String? = null,
    val selectedCategoryFilter: GrammarCategory? = null,
    val searchQuery: String = "",
    val gitHubInputUrl: String = GrammarRepository.DEFAULT_GITHUB_REPO_URL,
    val savedRepoLabel: String = "",
    val lastSyncedAt: Long = 0L,
    val syncStatus: SyncUiState = SyncUiState.Idle,
    val activeItemId: String? = null,
    val activeTopicDeck: String? = null,
    val isWatchAutoPlay: Boolean = false,
    val watchIntervalSec: Int = 12,
    val watchCountdownSec: Int = 12,
    val webTextZoomPercent: Int = 100
) {
    val totalCount: Int get() = allItems.size
    val completedCount: Int get() = allItems.count { it.isCompleted }
    val githubItemCount: Int get() = allItems.count { it.sourceOrigin == SourceOrigin.GITHUB }
    val bundledItemCount: Int get() = allItems.count { it.sourceOrigin == SourceOrigin.BUNDLED }

    val activeItem: GrammarItem?
        get() = allItems.firstOrNull { it.id == activeItemId } ?: allItems.firstOrNull()

    val currentPlayTitle: String
        get() = if (activeTopicDeck != null) {
            "$activeTopicDeck — Topic Deck"
        } else {
            activeItem?.title ?: "Select a Grammar Module"
        }

    val currentPlayHtml: String
        get() {
            val deckTopic = activeTopicDeck
            if (deckTopic != null) {
                val deckItems = allItems.filter { it.grammarTopic == deckTopic }
                if (deckItems.isNotEmpty()) {
                    return HtmlGrammarProcessor.buildTopicDeckHtml(deckTopic, deckItems)
                }
            }
            return activeItem?.revisedHtml ?: ""
        }
}

class GrammarViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GrammarDatabase.getInstance(application)
    private val prefsRepo = UserPreferencesRepository(application)
    private val repository = GrammarRepository(database.grammarDao(), prefsRepo)

    private val selectedTopicFilter = MutableStateFlow<String?>(null)
    private val selectedCategoryFilter = MutableStateFlow<GrammarCategory?>(null)
    private val searchQuery = MutableStateFlow("")
    private val gitHubInputUrl = MutableStateFlow(GrammarRepository.DEFAULT_GITHUB_REPO_URL)
    private val syncStatus = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    private val activeItemId = MutableStateFlow<String?>(null)
    private val activeTopicDeck = MutableStateFlow<String?>(null)
    private val isWatchAutoPlay = MutableStateFlow(false)
    private val watchCountdownSec = MutableStateFlow(12)
    private val webTextZoomPercent = MutableStateFlow(100)

    private var watchJob: Job? = null

    private data class FilterState(
        val topic: String?,
        val category: GrammarCategory?,
        val query: String
    )

    private data class PlayerState(
        val activeItemId: String?,
        val activeTopicDeck: String?,
        val isAutoPlay: Boolean,
        val countdown: Int,
        val zoomPercent: Int
    )

    private val filterFlow = combine(
        selectedTopicFilter,
        selectedCategoryFilter,
        searchQuery
    ) { topic, category, query ->
        FilterState(topic, category, query)
    }

    private val playerFlow = combine(
        activeItemId,
        activeTopicDeck,
        isWatchAutoPlay,
        watchCountdownSec,
        webTextZoomPercent
    ) { itemId, deck, autoPlay, countdown, zoom ->
        PlayerState(itemId, deck, autoPlay, countdown, zoom)
    }

    val uiState: StateFlow<GrammarUiState> = combine(
        repository.allItems,
        repository.preferences,
        filterFlow,
        gitHubInputUrl,
        syncStatus,
        playerFlow
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val items = values[0] as List<GrammarItem>
        val prefs = values[1] as com.example.data.local.GrammarPreferences
        val filter = values[2] as FilterState
        val inputUrl = values[3] as String
        val status = values[4] as SyncUiState
        val player = values[5] as PlayerState

        val orderedGroups = buildTopicGroups(items)
        val filteredItems = items.filter { item ->
            val matchesTopic = filter.topic == null || item.grammarTopic == filter.topic
            val matchesCat = filter.category == null || item.category == filter.category
            val q = filter.query.trim()
            val matchesQuery = q.isBlank() ||
                item.title.contains(q, ignoreCase = true) ||
                item.grammarTopic.contains(q, ignoreCase = true) ||
                item.revisedSummary.contains(q, ignoreCase = true)
            matchesTopic && matchesCat && matchesQuery
        }
        val filteredGroups = buildTopicGroups(filteredItems)

        val resolvedInputUrl = inputUrl.ifEmpty {
            prefs.savedGitHubUrl.ifEmpty { GrammarRepository.DEFAULT_GITHUB_REPO_URL }
        }

        GrammarUiState(
            allItems = items,
            topicGroups = orderedGroups,
            filteredTopicGroups = filteredGroups,
            selectedTopicFilter = filter.topic,
            selectedCategoryFilter = filter.category,
            searchQuery = filter.query,
            gitHubInputUrl = resolvedInputUrl,
            savedRepoLabel = prefs.lastSyncedLabel,
            lastSyncedAt = prefs.lastSyncedAt,
            syncStatus = status,
            activeItemId = player.activeItemId ?: items.firstOrNull()?.id,
            activeTopicDeck = player.activeTopicDeck,
            isWatchAutoPlay = player.isAutoPlay,
            watchIntervalSec = prefs.watchIntervalSeconds,
            watchCountdownSec = player.countdown,
            webTextZoomPercent = player.zoomPercent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GrammarUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureBundledSeeded()
            if (!repository.hasGitHubItems()) {
                syncGitHubUrlInternal(GrammarRepository.DEFAULT_GITHUB_REPO_URL, isAutoSync = true)
            }
        }
    }

    private fun buildTopicGroups(items: List<GrammarItem>): List<TopicGroup> {
        val grouped = items.groupBy { it.grammarTopic }
        val orderedKeys = GrammarTopics.orderedTopics.filter { grouped.containsKey(it) } +
            grouped.keys.filter { it !in GrammarTopics.orderedTopics }
        return orderedKeys.map { topic ->
            val topicItems = grouped[topic].orEmpty()
            TopicGroup(
                topic = topic,
                items = topicItems,
                completedCount = topicItems.count { it.isCompleted }
            )
        }
    }

    fun onGitHubUrlChanged(url: String) {
        gitHubInputUrl.value = url
        if (syncStatus.value is SyncUiState.Error) {
            syncStatus.value = SyncUiState.Idle
        }
    }

    fun syncGitHubRepo() {
        val targetUrl = uiState.value.gitHubInputUrl.trim()
        viewModelScope.launch {
            syncGitHubUrlInternal(targetUrl, isAutoSync = false)
        }
    }

    private suspend fun syncGitHubUrlInternal(targetUrl: String, isAutoSync: Boolean) {
        syncStatus.value = SyncUiState.Syncing
        when (val outcome = repository.syncFromGitHub(targetUrl)) {
            is SyncOutcome.Success -> {
                syncStatus.value = SyncUiState.Success(
                    "Loaded & revised ${outcome.count} mobile-ready HTML file(s) from ${outcome.repoLabel} across ${outcome.topicCount} grammar topic(s)."
                )
            }
            is SyncOutcome.Failure -> {
                syncStatus.value = if (isAutoSync) {
                    SyncUiState.Error("Offline fallback active. Tap 'Fetch & Group HTMLs' when online to sync ${GrammarRepository.DEFAULT_GITHUB_REPO_URL}.")
                } else {
                    SyncUiState.Error(outcome.errorMessage)
                }
            }
        }
    }

    fun clearGitHubImports() {
        viewModelScope.launch {
            repository.clearGitHubImportsAndKeepBundled()
            gitHubInputUrl.value = GrammarRepository.DEFAULT_GITHUB_REPO_URL
            syncStatus.value = SyncUiState.Success("Reset to built-in Grammar Craft starter modules.")
        }
    }

    fun dismissSyncBanner() {
        syncStatus.value = SyncUiState.Idle
    }

    fun selectTopicFilter(topic: String?) {
        selectedTopicFilter.value = if (selectedTopicFilter.value == topic) null else topic
    }

    fun selectCategoryFilter(category: GrammarCategory?) {
        selectedCategoryFilter.value = if (selectedCategoryFilter.value == category) null else category
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun openItemInPlayWatch(itemId: String) {
        activeTopicDeck.value = null
        activeItemId.value = itemId
    }

    fun openTopicDeckInPlayWatch(topic: String) {
        stopWatchAutoPlay()
        activeTopicDeck.value = topic
    }

    fun exitTopicDeckToSingleItem() {
        activeTopicDeck.value = null
        val firstInTopic = uiState.value.allItems.firstOrNull()?.id
        if (activeItemId.value == null) activeItemId.value = firstInTopic
    }

    fun toggleCompleted(item: GrammarItem) {
        viewModelScope.launch {
            repository.setItemCompleted(item.id, !item.isCompleted)
        }
    }

    fun selectNextPlayItem() {
        val items = uiState.value.allItems
        if (items.isEmpty()) return
        activeTopicDeck.value = null
        val currentIdx = items.indexOfFirst { it.id == uiState.value.activeItem?.id }
        val nextIdx = if (currentIdx < 0) 0 else (currentIdx + 1) % items.size
        activeItemId.value = items[nextIdx].id
    }

    fun selectPreviousPlayItem() {
        val items = uiState.value.allItems
        if (items.isEmpty()) return
        activeTopicDeck.value = null
        val currentIdx = items.indexOfFirst { it.id == uiState.value.activeItem?.id }
        val prevIdx = if (currentIdx <= 0) items.lastIndex else currentIdx - 1
        activeItemId.value = items[prevIdx].id
    }

    fun toggleWatchAutoPlay() {
        if (isWatchAutoPlay.value) {
            stopWatchAutoPlay()
        } else {
            startWatchAutoPlay()
        }
    }

    private fun startWatchAutoPlay() {
        activeTopicDeck.value = null
        isWatchAutoPlay.value = true
        val interval = uiState.value.watchIntervalSec
        watchCountdownSec.value = interval
        watchJob?.cancel()
        watchJob = viewModelScope.launch {
            while (isWatchAutoPlay.value) {
                delay(1000L)
                val remaining = watchCountdownSec.value - 1
                if (remaining <= 0) {
                    selectNextPlayItem()
                    watchCountdownSec.value = uiState.value.watchIntervalSec
                } else {
                    watchCountdownSec.value = remaining
                }
            }
        }
    }

    fun stopWatchAutoPlay() {
        isWatchAutoPlay.value = false
        watchJob?.cancel()
        watchJob = null
        watchCountdownSec.value = uiState.value.watchIntervalSec
    }

    fun adjustTextZoom(delta: Int) {
        webTextZoomPercent.value = (webTextZoomPercent.value + delta).coerceIn(80, 150)
    }

    override fun onCleared() {
        super.onCleared()
        watchJob?.cancel()
    }
}
