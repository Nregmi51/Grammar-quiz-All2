package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GrammarDao {
    @Query("SELECT * FROM grammar_items ORDER BY grammarTopic ASC, title ASC")
    fun observeAllItems(): Flow<List<GrammarItemEntity>>

    @Query("SELECT COUNT(*) FROM grammar_items")
    suspend fun countItems(): Int

    @Query("SELECT COUNT(*) FROM grammar_items WHERE sourceOrigin = 'GITHUB'")
    suspend fun countGitHubItems(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<GrammarItemEntity>)

    @Query("UPDATE grammar_items SET isCompleted = :completed WHERE id = :id")
    suspend fun updateCompleted(id: String, completed: Boolean)

    @Query("DELETE FROM grammar_items WHERE sourceOrigin = :origin")
    suspend fun deleteBySourceOrigin(origin: String)
}
