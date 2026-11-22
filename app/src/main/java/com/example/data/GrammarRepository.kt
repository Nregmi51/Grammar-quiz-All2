package com.example.data

import com.example.data.local.BundledGrammarSeed
import com.example.data.local.GrammarDao
import com.example.data.local.GrammarItemEntity
import com.example.data.local.GrammarPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.remote.GitHubFetchResult
import com.example.data.remote.GitHubHtmlFetcher
import com.example.domain.HtmlGrammarProcessor
import com.example.model.GrammarItem
import com.example.model.SourceOrigin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed class SyncOutcome {
    data class Success(val count: Int, val repoLabel: String, val topicCount: Int) : SyncOutcome()
    data class Failure(val errorMessage: String) : SyncOutcome()
}

class GrammarRepository(
    private val dao: GrammarDao,
    private val prefsRepo: UserPreferencesRepository,
    private val fetcher: GitHubHtmlFetcher = GitHubHtmlFetcher()
) {
    companion object {
        const val DEFAULT_GITHUB_REPO_URL = "https://github.com/Nregmi51/Grammar-quiz-All"
    }

    val allItems: Flow<List<GrammarItem>> = dao.observeAllItems()
        .map { entities -> entities.map { it.toDomain() } }

    val preferences: Flow<GrammarPreferences> = prefsRepo.preferences

    suspend fun ensureBundledSeeded() {
        if (dao.countItems() == 0) {
            val seeds = BundledGrammarSeed.createInitialItems()
            dao.insertAll(seeds.map { GrammarItemEntity.fromDomain(it) })
        }
    }

    suspend fun hasGitHubItems(): Boolean = dao.countGitHubItems() > 0

    suspend fun syncFromGitHub(url: String): SyncOutcome {
        return when (val result = fetcher.fetchHtmlFiles(url)) {
            is GitHubFetchResult.Error -> SyncOutcome.Failure(result.message)
            is GitHubFetchResult.Success -> {
                val processed = result.files.map { file ->
                    HtmlGrammarProcessor.processHtml(
                        rawHtml = file.htmlContent,
                        fileName = file.fileName,
                        sourceUrl = file.rawUrl,
                        sourceOrigin = SourceOrigin.GITHUB
                    )
                }
                // Replace bundled fallback & old GitHub imports so the user sees their live GitHub library cleanly
                dao.deleteBySourceOrigin(SourceOrigin.BUNDLED.name)
                dao.deleteBySourceOrigin(SourceOrigin.GITHUB.name)
                dao.insertAll(processed.map { GrammarItemEntity.fromDomain(it) })
                prefsRepo.saveGitHubSyncInfo(
                    url = url.trim(),
                    repoLabel = result.resolvedRepoLabel,
                    timestamp = System.currentTimeMillis()
                )
                val distinctTopics = processed.map { it.grammarTopic }.distinct().size
                SyncOutcome.Success(
                    count = processed.size,
                    repoLabel = result.resolvedRepoLabel,
                    topicCount = distinctTopics
                )
            }
        }
    }

    suspend fun clearGitHubImportsAndKeepBundled() {
        dao.deleteBySourceOrigin(SourceOrigin.GITHUB.name)
        ensureBundledSeeded()
        prefsRepo.saveGitHubSyncInfo(url = "", repoLabel = "", timestamp = 0L)
    }

    suspend fun setItemCompleted(id: String, completed: Boolean) {
        dao.updateCompleted(id, completed)
    }

    suspend fun updateWatchInterval(seconds: Int) {
        prefsRepo.saveWatchIntervalSeconds(seconds)
    }
}
