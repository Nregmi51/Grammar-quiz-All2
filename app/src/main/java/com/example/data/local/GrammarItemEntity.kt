package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.GrammarCategory
import com.example.model.GrammarItem
import com.example.model.SourceOrigin

@Entity(tableName = "grammar_items")
data class GrammarItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val grammarTopic: String,
    val category: String,
    val sourceOrigin: String,
    val sourceUrl: String,
    val rawHtml: String,
    val revisedHtml: String,
    val revisedSummary: String,
    val keyPointsJoined: String,
    val hasExplicitBackground: Boolean,
    val isCompleted: Boolean,
    val updatedAt: Long
) {
    fun toDomain(): GrammarItem {
        val parsedCategory = runCatching { GrammarCategory.valueOf(category) }
            .getOrDefault(GrammarCategory.READING_RULE)
        val parsedOrigin = runCatching { SourceOrigin.valueOf(sourceOrigin) }
            .getOrDefault(SourceOrigin.BUNDLED)
        val keyPoints = if (keyPointsJoined.isBlank()) {
            emptyList()
        } else {
            keyPointsJoined.split("|||").filter { it.isNotBlank() }
        }
        return GrammarItem(
            id = id,
            title = title,
            grammarTopic = grammarTopic,
            category = parsedCategory,
            sourceOrigin = parsedOrigin,
            sourceUrl = sourceUrl,
            rawHtml = rawHtml,
            revisedHtml = revisedHtml,
            revisedSummary = revisedSummary,
            keyPoints = keyPoints,
            hasExplicitBackground = hasExplicitBackground,
            isCompleted = isCompleted,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(item: GrammarItem): GrammarItemEntity {
            return GrammarItemEntity(
                id = item.id,
                title = item.title,
                grammarTopic = item.grammarTopic,
                category = item.category.name,
                sourceOrigin = item.sourceOrigin.name,
                sourceUrl = item.sourceUrl,
                rawHtml = item.rawHtml,
                revisedHtml = item.revisedHtml,
                revisedSummary = item.revisedSummary,
                keyPointsJoined = item.keyPoints.joinToString("|||"),
                hasExplicitBackground = item.hasExplicitBackground,
                isCompleted = item.isCompleted,
                updatedAt = item.updatedAt
            )
        }
    }
}
