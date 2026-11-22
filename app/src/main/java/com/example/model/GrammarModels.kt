package com.example.model

enum class GrammarCategory(val label: String, val shortLabel: String) {
    READING_RULE("Reading Rules", "Rules"),
    QUIZ("Interactive Quiz", "Quiz"),
    COMPOSITION("Design Composition", "Composition"),
    WATCH_LESSON("Watch & Learn", "Watch")
}

enum class SourceOrigin(val label: String) {
    BUNDLED("Starter Pack"),
    GITHUB("GitHub")
}

object GrammarTopics {
    const val ARTICLES = "Articles (a / an / the)"
    const val PREPOSITIONS = "Prepositions"
    const val SUBJECT_VERB = "Subject-Verb Agreement"
    const val CONDITIONALS = "Conditional Sentences"
    const val REPORTED_SPEECH = "Reported Speech"
    const val ACTIVE_PASSIVE = "Active & Passive Voice"
    const val VERB_TENSES = "Verb Tenses"
    const val CONJUNCTIONS_NEGATION = "Conjunctions & Negation"
    const val SEE_MIXED_PRACTICE = "SEE & Grade Practice Sets"
    const val COMPOSITIONS_ESSAYS = "Essays & Design Compositions"
    const val GENERAL = "General Grammar"

    val orderedTopics = listOf(
        PREPOSITIONS,
        ARTICLES,
        SUBJECT_VERB,
        CONDITIONALS,
        REPORTED_SPEECH,
        ACTIVE_PASSIVE,
        VERB_TENSES,
        CONJUNCTIONS_NEGATION,
        SEE_MIXED_PRACTICE,
        COMPOSITIONS_ESSAYS,
        GENERAL
    )
}

data class GrammarItem(
    val id: String,
    val title: String,
    val grammarTopic: String,
    val category: GrammarCategory,
    val sourceOrigin: SourceOrigin,
    val sourceUrl: String,
    val rawHtml: String,
    val revisedHtml: String,
    val revisedSummary: String,
    val keyPoints: List<String>,
    val hasExplicitBackground: Boolean,
    val isCompleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

data class TopicGroup(
    val topic: String,
    val items: List<GrammarItem>,
    val completedCount: Int
)
