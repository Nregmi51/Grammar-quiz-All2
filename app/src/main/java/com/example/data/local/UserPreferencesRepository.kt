package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.grammarPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "grammar_craft_prefs"
)

data class GrammarPreferences(
    val savedGitHubUrl: String = "",
    val lastSyncedLabel: String = "",
    val lastSyncedAt: Long = 0L,
    val watchIntervalSeconds: Int = 12
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val GITHUB_URL = stringPreferencesKey("github_url")
        val LAST_SYNCED_LABEL = stringPreferencesKey("last_synced_label")
        val LAST_SYNCED_AT = longPreferencesKey("last_synced_at")
        val WATCH_INTERVAL_SEC = intPreferencesKey("watch_interval_sec")
    }

    val preferences: Flow<GrammarPreferences> = context.grammarPrefsDataStore.data.map { prefs ->
        GrammarPreferences(
            savedGitHubUrl = prefs[Keys.GITHUB_URL].orEmpty(),
            lastSyncedLabel = prefs[Keys.LAST_SYNCED_LABEL].orEmpty(),
            lastSyncedAt = prefs[Keys.LAST_SYNCED_AT] ?: 0L,
            watchIntervalSeconds = prefs[Keys.WATCH_INTERVAL_SEC] ?: 12
        )
    }

    suspend fun saveGitHubSyncInfo(url: String, repoLabel: String, timestamp: Long) {
        context.grammarPrefsDataStore.edit { prefs ->
            prefs[Keys.GITHUB_URL] = url
            prefs[Keys.LAST_SYNCED_LABEL] = repoLabel
            prefs[Keys.LAST_SYNCED_AT] = timestamp
        }
    }

    suspend fun saveWatchIntervalSeconds(seconds: Int) {
        context.grammarPrefsDataStore.edit { prefs ->
            prefs[Keys.WATCH_INTERVAL_SEC] = seconds.coerceIn(5, 45)
        }
    }
}
