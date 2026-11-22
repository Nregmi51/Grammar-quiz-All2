package com.example

import com.example.domain.HtmlGrammarProcessor
import com.example.model.GrammarCategory
import com.example.model.GrammarTopics
import com.example.model.SourceOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun processHtml_fixesMissingViewportAndOverflowHiddenWhilePreservingBackground() {
        val desktopSlideHtml = """
            <!DOCTYPE html><html lang="en"><head>
            <meta charset="UTF-8">
            <title>35 Prepositions – Interactive Slides</title>
            <style>
              html,body{height:100%;overflow:hidden}
              .deck{width:100%;height:100%;background:linear-gradient(135deg, #1a2a6c, #b21f1f);}
            </style>
            </head><body><div class="deck"><div class="slide active">At, On, In</div></div></body></html>
        """.trimIndent()

        val result = HtmlGrammarProcessor.processHtml(
            rawHtml = desktopSlideHtml,
            fileName = "Rules-Preposition-1.html",
            sourceUrl = "https://raw.githubusercontent.com/Nregmi51/Grammar-quiz-All/main/Rules-Preposition-1.html",
            sourceOrigin = SourceOrigin.GITHUB
        )

        assertEquals(GrammarTopics.PREPOSITIONS, result.grammarTopic)
        assertEquals(GrammarCategory.WATCH_LESSON, result.category)
        assertTrue(result.hasExplicitBackground)
        assertTrue(result.revisedHtml.contains("name=\"viewport\""))
        assertTrue(result.revisedHtml.contains("grammar-craft-mobile-adapter"))
        assertTrue(result.revisedHtml.contains("linear-gradient(135deg, #1a2a6c, #b21f1f)"))
    }

    @Test
    fun processHtml_ignoresGenericReactArtifactTitleAndClassifiesTopics() {
        val reactHtml = """
            <!DOCTYPE html><html><head><title>React Artifact</title></head>
            <body><div id="root"></div><script>/* bundled */</script></body></html>
        """.trimIndent()

        val essay = HtmlGrammarProcessor.processHtml(
            rawHtml = reactHtml,
            fileName = "Essay-women-empowerment-nepal.html",
            sourceUrl = "https://raw.githubusercontent.com/Nregmi51/Grammar-quiz-All/main/Essay-women-empowerment-nepal.html",
            sourceOrigin = SourceOrigin.GITHUB
        )
        assertEquals(GrammarTopics.COMPOSITIONS_ESSAYS, essay.grammarTopic)
        assertEquals(GrammarCategory.COMPOSITION, essay.category)
        assertFalse(essay.title.equals("React Artifact", ignoreCase = true))
        assertTrue(essay.title.contains("Essay"))

        val conditional = HtmlGrammarProcessor.processHtml(
            rawHtml = "<html><head><title>Smartboard Conditional Quiz</title></head><body>If clause</body></html>",
            fileName = "IFcondtion-Quiz-3.html",
            sourceUrl = "https://raw.githubusercontent.com/Nregmi51/Grammar-quiz-All/main/IFcondtion-Quiz-3.html",
            sourceOrigin = SourceOrigin.GITHUB
        )
        assertEquals(GrammarTopics.CONDITIONALS, conditional.grammarTopic)
        assertEquals(GrammarCategory.QUIZ, conditional.category)

        val negation = HtmlGrammarProcessor.processHtml(
            rawHtml = reactHtml,
            fileName = "Cdc-5-Set-Tricky-Negation-Quiz.html",
            sourceUrl = "https://raw.githubusercontent.com/Nregmi51/Grammar-quiz-All/main/Cdc-5-Set-Tricky-Negation-Quiz.html",
            sourceOrigin = SourceOrigin.GITHUB
        )
        assertEquals(GrammarTopics.CONJUNCTIONS_NEGATION, negation.grammarTopic)
        assertEquals(GrammarCategory.QUIZ, negation.category)
    }

    @Test
    fun buildTopicDeckHtml_groupsModulesAndPreservesIframeFormats() {
        val item = HtmlGrammarProcessor.processHtml(
            rawHtml = "<body style=\"background-color:#222;\"><h2>Comma Rules</h2><p>Use commas after introductory clauses.</p></body>",
            fileName = "Rule-Conjunction.html",
            sourceUrl = "https://raw.githubusercontent.com/Nregmi51/Grammar-quiz-All/main/Rule-Conjunction.html",
            sourceOrigin = SourceOrigin.GITHUB
        )

        val deck = HtmlGrammarProcessor.buildTopicDeckHtml(item.grammarTopic, listOf(item))
        assertTrue(deck.contains("Comma Rules"))
        assertTrue(deck.contains("srcdoc="))
        assertTrue(deck.contains("#222"))
    }
}
