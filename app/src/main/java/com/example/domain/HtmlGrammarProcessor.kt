package com.example.domain

import com.example.model.GrammarCategory
import com.example.model.GrammarItem
import com.example.model.GrammarTopics
import com.example.model.SourceOrigin
import java.security.MessageDigest
import java.util.Locale

object HtmlGrammarProcessor {

    private val titleRegex = Regex("(?is)<title[^>]*>(.*?)</title>")
    private val h1Regex = Regex("(?is)<h[12][^>]*>(.*?)</h[12]>")
    private val headingOrListRegex = Regex("(?is)<(h2|h3|li|strong)[^>]*>(.*?)</\\1>")
    private val scriptStyleRegex = Regex("(?is)<(script|style)[^>]*>.*?</\\1>")
    private val tagRegex = Regex("(?is)<[^>]+>")
    private val whitespaceRegex = Regex("\\s+")
    private val viewportTagRegex = Regex("(?is)<meta[^>]+name\\s*=\\s*[\"']viewport[\"'][^>]*>")
    private val bgRegex = Regex(
        "(?is)(background\\s*:|background-color\\s*:|background-image\\s*:|bgcolor\\s*=|linear-gradient|radial-gradient|bg-gradient|bg-\\[)"
    )

    private val genericTitles = setOf(
        "react artifact",
        "document",
        "untitled",
        "index",
        "quiz",
        "english grammar quiz",
        "grammar practice quiz"
    )

    fun processHtml(
        rawHtml: String,
        fileName: String,
        sourceUrl: String,
        sourceOrigin: SourceOrigin,
        explicitId: String? = null
    ): GrammarItem {
        val cleanedRaw = rawHtml.trim()
        val extractedTitle = extractTitle(cleanedRaw, fileName)
        val plainText = stripHtmlToText(cleanedRaw)
        val fileAndTitle = "$fileName $extractedTitle".lowercase(Locale.US)
        val combinedSearchBlob = "$fileName $extractedTitle ${plainText.take(2500)}".lowercase(Locale.US)

        val topic = classifyTopic(fileAndTitle, combinedSearchBlob)
        val category = classifyCategory(cleanedRaw, fileAndTitle, combinedSearchBlob)
        val hasExplicitBg = bgRegex.containsMatchIn(cleanedRaw)
        val revisedHtml = buildRevisedPlayableHtml(cleanedRaw, extractedTitle, hasExplicitBg)
        val keyPoints = extractKeyPoints(cleanedRaw, plainText, fileName, topic, category)
        val summary = buildRevisedSummary(plainText, extractedTitle, topic, category)

        val id = explicitId ?: "${sourceOrigin.name.lowercase(Locale.US)}_${sha1Short(sourceUrl)}"

        return GrammarItem(
            id = id,
            title = extractedTitle,
            grammarTopic = topic,
            category = category,
            sourceOrigin = sourceOrigin,
            sourceUrl = sourceUrl,
            rawHtml = cleanedRaw,
            revisedHtml = revisedHtml,
            revisedSummary = summary,
            keyPoints = keyPoints,
            hasExplicitBackground = hasExplicitBg,
            isCompleted = false,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun buildTopicDeckHtml(topic: String, items: List<GrammarItem>): String {
        val sections = items.mapIndexed { index, item ->
            val escapedSrcDoc = item.revisedHtml
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
            """
            <section class="topic-card">
              <div class="card-header">
                <span class="badge">${index + 1} / ${items.size} • ${escapeHtml(item.category.shortLabel)}</span>
                <h2>${escapeHtml(item.title)}</h2>
                <p class="summary">${escapeHtml(item.revisedSummary)}</p>
              </div>
              <div class="frame-wrap">
                <iframe sandbox="allow-scripts allow-same-origin" srcdoc="$escapedSrcDoc" loading="lazy"></iframe>
              </div>
            </section>
            """.trimIndent()
        }.joinToString("\n")

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>${escapeHtml(topic)} — Study Deck</title>
          <style>
            * { box-sizing: border-box; }
            body {
              margin: 0;
              padding: 12px;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
              background: linear-gradient(160deg, #0A6B5C 0%, #0D4E8B 100%);
              color: #0A2347;
            }
            .deck-banner {
              background: #0B2956;
              color: #FFFBE6;
              padding: 14px 16px;
              border-radius: 16px;
              margin-bottom: 14px;
              border: 1px solid rgba(255, 251, 230, 0.25);
            }
            .deck-banner h1 { margin: 0 0 4px 0; font-size: 19px; }
            .deck-banner p { margin: 0; font-size: 12.5px; opacity: 0.92; }
            .topic-card {
              background: #FFFBE6;
              border-radius: 18px;
              padding: 12px;
              margin-bottom: 16px;
              border: 1.5px solid #E0CC7A;
              box-shadow: 0 6px 18px rgba(8, 25, 52, 0.25);
            }
            .card-header { margin-bottom: 10px; }
            .badge {
              display: inline-block;
              background: #0B2956;
              color: #FFFBE6;
              padding: 4px 10px;
              border-radius: 999px;
              font-size: 11px;
              font-weight: 700;
              margin-bottom: 6px;
            }
            .card-header h2 {
              margin: 4px 0;
              font-size: 16px;
              color: #0A2347;
            }
            .summary {
              margin: 0;
              font-size: 13px;
              color: #1A3A68;
              line-height: 1.45;
            }
            .frame-wrap {
              border-radius: 12px;
              overflow: hidden;
              border: 1px solid #D6C372;
            }
            iframe {
              width: 100%;
              min-height: 460px;
              border: none;
              display: block;
            }
          </style>
        </head>
        <body>
          <div class="deck-banner">
            <h1>${escapeHtml(topic)}</h1>
            <p>Grouped topic pack (${items.size} HTML modules) • Original HTML backgrounds preserved inside each card</p>
          </div>
          $sections
        </body>
        </html>
        """.trimIndent()
    }

    private fun extractTitle(html: String, fileName: String): String {
        val niceFromFile = humanizeFileName(fileName)

        val fromTitle = titleRegex.find(html)?.groupValues?.getOrNull(1)
            ?.let { decodeEntities(tagRegex.replace(it, "")).replace(whitespaceRegex, " ").trim() }
        if (!fromTitle.isNullOrBlank() &&
            fromTitle.length in 3..68 &&
            fromTitle.lowercase(Locale.US) !in genericTitles
        ) {
            return fromTitle
        }

        val fromHeading = h1Regex.find(html)?.groupValues?.getOrNull(1)
            ?.let { decodeEntities(tagRegex.replace(it, "")).replace(whitespaceRegex, " ").trim() }
        if (!fromHeading.isNullOrBlank() &&
            fromHeading.length in 4..68 &&
            fromHeading.lowercase(Locale.US) !in genericTitles
        ) {
            return fromHeading
        }

        return niceFromFile
    }

    private fun humanizeFileName(fileName: String): String {
        val base = fileName
            .substringAfterLast('/')
            .substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .replace(whitespaceRegex, " ")
            .trim()
        if (base.equals("index", ignoreCase = true)) {
            return "Preposition Quiz (6 Sets)"
        }
        val fixedTypos = base
            .replace("Qiz", "Quiz", ignoreCase = true)
            .replace("qiuz", "Quiz", ignoreCase = true)
            .replace("prepositon", "Preposition", ignoreCase = true)
            .replace("IFcondition", "If-Condition", ignoreCase = true)
            .replace("IFcondtion", "If-Condition", ignoreCase = true)
            .replace("SpeechIndirect", "Speech Indirect", ignoreCase = false)
            .replace("speechBoth", "Speech Both", ignoreCase = false)
            .replace("PassiveToActiveQuiz", "Passive to Active Quiz ", ignoreCase = false)
            .replace("Cdc", "CDC", ignoreCase = true)
            .replace("Mcqs", "MCQs", ignoreCase = true)

        return fixedTypos.split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                if (word.uppercase(Locale.US) in setOf("SEE", "CDC", "MCQS", "G5", "G9", "G10", "Q10", "NP")) {
                    word.uppercase(Locale.US)
                } else {
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
                }
            }.ifBlank { "Grammar Lesson" }
    }

    private fun classifyTopic(fileAndTitle: String, fullBlob: String): String {
        // Priority classification from file name & explicit title
        return when {
            fileAndTitle.contains("essay") || fileAndTitle.contains("composition") ->
                GrammarTopics.COMPOSITIONS_ESSAYS
            fileAndTitle.contains("article") ->
                GrammarTopics.ARTICLES
            fileAndTitle.contains("preposition") || fileAndTitle.contains("prepositon") ->
                GrammarTopics.PREPOSITIONS
            fileAndTitle.contains("subject") && fileAndTitle.contains("verb") ||
                fileAndTitle.contains("sub-verb") || fileAndTitle.contains("sub verb") ->
                GrammarTopics.SUBJECT_VERB
            fileAndTitle.contains("condition") || fileAndTitle.contains("ifcondition") ||
                fileAndTitle.contains("ifcondtion") || fileAndTitle.contains("if-condition") ->
                GrammarTopics.CONDITIONALS
            fileAndTitle.contains("reported") || fileAndTitle.contains("direct") && fileAndTitle.contains("indirect") ->
                GrammarTopics.REPORTED_SPEECH
            fileAndTitle.contains("passive") || fileAndTitle.contains("active") ->
                GrammarTopics.ACTIVE_PASSIVE
            fileAndTitle.contains("tense") ->
                GrammarTopics.VERB_TENSES
            fileAndTitle.contains("conjunction") || fileAndTitle.contains("negation") || fileAndTitle.contains("vibrant") ->
                GrammarTopics.CONJUNCTIONS_NEGATION
            fileAndTitle.contains("see") || fileAndTitle.contains("g5") || fileAndTitle.contains("g9") ||
                fileAndTitle.contains("g10") || fileAndTitle.contains("sets") || fileAndTitle.contains("q10") ->
                GrammarTopics.SEE_MIXED_PRACTICE
            else -> classifyByBodyKeywords(fullBlob)
        }
    }

    private fun classifyByBodyKeywords(blob: String): String {
        val scores = linkedMapOf(
            GrammarTopics.PREPOSITIONS to countMatches(blob, listOf("preposition", "at ", "on ", "in ", "under", "between")),
            GrammarTopics.ARTICLES to countMatches(blob, listOf("article", "a/an/the", "definite article", "indefinite")),
            GrammarTopics.SUBJECT_VERB to countMatches(blob, listOf("subject-verb", "subject verb", "singular verb", "plural verb", "agreement")),
            GrammarTopics.CONDITIONALS to countMatches(blob, listOf("conditional", "if-clause", "would have", "type 1", "type 2")),
            GrammarTopics.REPORTED_SPEECH to countMatches(blob, listOf("reported speech", "indirect speech", "direct speech", "narration")),
            GrammarTopics.ACTIVE_PASSIVE to countMatches(blob, listOf("passive voice", "active voice", "by the", "past participle")),
            GrammarTopics.VERB_TENSES to countMatches(blob, listOf("tense", "present simple", "past perfect", "future", "continuous")),
            GrammarTopics.CONJUNCTIONS_NEGATION to countMatches(blob, listOf("conjunction", "negation", "coordinating", "subordinating", "neither", "although")),
            GrammarTopics.COMPOSITIONS_ESSAYS to countMatches(blob, listOf("essay", "composition", "paragraph", "empowerment"))
        )
        val best = scores.maxByOrNull { it.value }
        return if (best != null && best.value > 0) best.key else GrammarTopics.GENERAL
    }

    private fun classifyCategory(rawHtml: String, fileAndTitle: String, blob: String): GrammarCategory {
        if (fileAndTitle.contains("essay") || fileAndTitle.contains("composition")) {
            return GrammarCategory.COMPOSITION
        }
        if (fileAndTitle.contains("rule") || fileAndTitle.contains("study guide")) {
            return if (
                rawHtml.contains(".slide", ignoreCase = true) ||
                rawHtml.contains("class=\"slide", ignoreCase = true) ||
                rawHtml.contains("showSlide", ignoreCase = true)
            ) {
                GrammarCategory.WATCH_LESSON
            } else {
                GrammarCategory.READING_RULE
            }
        }
        val lowerHtml = rawHtml.lowercase(Locale.US)
        val hasQuizControls = fileAndTitle.contains("quiz") ||
            fileAndTitle.contains("mcq") ||
            fileAndTitle.contains("practice") ||
            fileAndTitle.contains("fill-in") ||
            fileAndTitle.contains("fill in") ||
            fileAndTitle.contains("set") ||
            lowerHtml.contains("type=\"radio\"") ||
            lowerHtml.contains("checkanswer") ||
            lowerHtml.contains("score")

        if (hasQuizControls) return GrammarCategory.QUIZ

        if (blob.contains("watch") || blob.contains("slide") || lowerHtml.contains("@keyframes")) {
            return GrammarCategory.WATCH_LESSON
        }
        return GrammarCategory.READING_RULE
    }

    /**
     * Makes desktop/smartboard HTML files cleanly mobile-friendly in Android WebView
     * while preserving each HTML file's original background format and scripts.
     */
    private fun buildRevisedPlayableHtml(
        rawHtml: String,
        title: String,
        hasExplicitBg: Boolean
    ): String {
        val mobileCssAndScript = buildString {
            append(
                """
                <style id="grammar-craft-mobile-adapter">
                  html, body {
                    max-width: 100% !important;
                    overflow-x: hidden !important;
                    overflow-y: auto !important;
                    height: auto !important;
                    min-height: 100vh;
                    -webkit-text-size-adjust: 100%;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                  }
                  * { box-sizing: border-box; }
                  img, video, svg, canvas {
                    max-width: 100% !important;
                    height: auto !important;
                  }
                  table {
                    max-width: 100% !important;
                    display: block;
                    overflow-x: auto;
                    -webkit-overflow-scrolling: touch;
                  }
                  button, input, select, label {
                    touch-action: manipulation;
                  }
                  @media (max-width: 768px) {
                    body {
                      padding: 10px !important;
                    }
                    .container, .main-container, .quiz-container, .slide-wrapper {
                      width: 100% !important;
                      max-width: 100% !important;
                      padding: 12px !important;
                      border-radius: 16px !important;
                    }
                    h1 {
                      font-size: clamp(1.25rem, 5.5vw, 1.85rem) !important;
                      line-height: 1.25 !important;
                    }
                    h2 {
                      font-size: clamp(1.1rem, 4.5vw, 1.45rem) !important;
                      line-height: 1.3 !important;
                    }
                    /* Fix locked viewport slide decks (e.g. Rules-Preposition-1 / 2) on mobile */
                    .deck, .slide-container {
                      width: 100% !important;
                      height: auto !important;
                      min-height: calc(100vh - 24px) !important;
                      padding: 8px 4px 80px 4px !important;
                      display: block !important;
                    }
                    .slide {
                      position: relative !important;
                      display: none !important;
                      width: 100% !important;
                      max-width: 100% !important;
                      height: auto !important;
                      min-height: auto !important;
                      padding: 14px 12px !important;
                      margin: 0 auto !important;
                      transform: none !important;
                    }
                    .slide.active {
                      display: flex !important;
                      flex-direction: column !important;
                      align-items: stretch !important;
                      opacity: 1 !important;
                    }
                  }
                </style>
                """.trimIndent()
            )
            if (!hasExplicitBg) {
                append(
                    """
                    <style id="grammar-craft-fallback-bg">
                      body { background: #FFFBE6; color: #0A2347; padding: 14px; font-family: sans-serif; line-height: 1.6; }
                    </style>
                    """.trimIndent()
                )
            }
            append(
                """
                <script id="grammar-craft-touch-helper">
                  (function() {
                    var startX = 0, startY = 0;
                    document.addEventListener('touchstart', function(e) {
                      if (e.touches && e.touches.length === 1) {
                        startX = e.touches[0].clientX;
                        startY = e.touches[0].clientY;
                      }
                    }, { passive: true });
                    document.addEventListener('touchend', function(e) {
                      if (!e.changedTouches || e.changedTouches.length !== 1) return;
                      var dx = e.changedTouches[0].clientX - startX;
                      var dy = e.changedTouches[0].clientY - startY;
                      if (Math.abs(dx) > 70 && Math.abs(dx) > Math.abs(dy) * 1.6) {
                        var hasSlides = document.querySelector('.slide');
                        if (!hasSlides) return;
                        if (dx < 0) {
                          var nextBtn = document.getElementById('nextBtn');
                          if (nextBtn && !nextBtn.disabled) nextBtn.click();
                          else if (typeof move === 'function') move(1);
                          else if (typeof nextSlide === 'function') nextSlide();
                        } else {
                          var prevBtn = document.getElementById('prevBtn');
                          if (prevBtn && !prevBtn.disabled) prevBtn.click();
                          else if (typeof move === 'function') move(-1);
                          else if (typeof prevSlide === 'function') prevSlide();
                        }
                      }
                    }, { passive: true });
                  })();
                </script>
                """.trimIndent()
            )
        }

        val viewportMeta = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=3.0, user-scalable=yes\" />\n"
        // Replace existing viewport safely using match range
        val existingViewportMatch = viewportTagRegex.find(rawHtml)
        val normalizedHtml = if (existingViewportMatch != null) {
            rawHtml.replaceRange(existingViewportMatch.range, viewportMeta.trim())
        } else {
            rawHtml
        }

        val hasHtmlTag = normalizedHtml.contains("<html", ignoreCase = true)
        val headMatch = Regex("(?i)<head[^>]*>").find(normalizedHtml)
        val injectedHeaderContent = "\n$viewportMeta$mobileCssAndScript\n"
        val cssOnlyContent = "\n$mobileCssAndScript\n"

        return when {
            hasHtmlTag && headMatch != null -> {
                val insertPos = headMatch.range.last + 1
                val payload = if (existingViewportMatch != null) cssOnlyContent else injectedHeaderContent
                StringBuilder(normalizedHtml).insert(insertPos, payload).toString()
            }
            hasHtmlTag -> {
                val htmlMatch = Regex("(?i)<html[^>]*>").find(normalizedHtml)
                if (htmlMatch != null) {
                    val insertPos = htmlMatch.range.last + 1
                    StringBuilder(normalizedHtml)
                        .insert(insertPos, "\n<head><meta charset=\"UTF-8\" />$viewportMeta$mobileCssAndScript</head>")
                        .toString()
                } else {
                    normalizedHtml
                }
            }
            else -> {
                """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  $viewportMeta
                  <title>${escapeHtml(title)}</title>
                  $mobileCssAndScript
                </head>
                <body>
                  $normalizedHtml
                </body>
                </html>
                """.trimIndent()
            }
        }
    }

    private fun extractKeyPoints(
        rawHtml: String,
        plainText: String,
        fileName: String,
        topic: String,
        category: GrammarCategory
    ): List<String> {
        val noScripts = scriptStyleRegex.replace(rawHtml, " ")
        val points = headingOrListRegex.findAll(noScripts)
            .map { decodeEntities(tagRegex.replace(it.groupValues[2], "")).replace(whitespaceRegex, " ").trim() }
            .filter { it.length in 10..110 }
            .filterNot { it.contains("Loading", ignoreCase = true) || it.contains("Click an option", ignoreCase = true) }
            .distinct()
            .take(3)
            .toList()

        if (points.isNotEmpty()) return points

        val sentences = plainText.split('.', '\n')
            .map { it.trim() }
            .filter { it.length in 18..110 }
            .distinct()
            .take(3)
        if (sentences.isNotEmpty()) return sentences

        // Rich fallback for compiled React-artifact HTMLs where DOM is rendered at runtime
        val lowerName = fileName.lowercase(Locale.US)
        return when {
            lowerName.contains("essay") -> listOf(
                "SEE-pattern model essays on Women Empowerment in Nepal (200–312 words).",
                "Includes persuasive, descriptive, and structured paragraph blueprints.",
                "Mobile-scrollable comparison layout with original styling preserved."
            )
            lowerName.contains("negation") -> listOf(
                "5 interactive quiz sets covering tricky negation & affirmative transformations.",
                "Instant feedback and score tracking built into the HTML quiz.",
                "Formatted for single-column mobile touch interaction."
            )
            lowerName.contains("vibrant") -> listOf(
                "5 vibrant CDC-style grammar practice sets with interactive MCQs.",
                "Covers transformation, sentence agreement, and contextual usage.",
                "Original vibrant color formatting preserved inside Play / Watch."
            )
            lowerName.contains("q10") -> listOf(
                "SEE English Question-10 fill-in-the-blanks grammar practice.",
                "Tap-to-select answers with instant explanation highlights.",
                "Covers articles, prepositions, tags, voice, and tenses together."
            )
            else -> listOf(
                "Interactive ${category.label.lowercase(Locale.US)} module grouped under $topic.",
                "Adapted with responsive mobile viewport and touch-friendly controls."
            )
        }
    }

    private fun buildRevisedSummary(
        plainText: String,
        title: String,
        topic: String,
        category: GrammarCategory
    ): String {
        val cleaned = plainText.replace(whitespaceRegex, " ").trim()
        if (cleaned.length < 24 || cleaned.contains("Loading questions", ignoreCase = true)) {
            return "$title — ${category.label} module grouped under $topic, adapted for mobile touch & scroll."
        }
        val sentences = cleaned.split(Regex("(?<=[.!?])\\s+"))
            .filter { it.length > 20 }
            .take(2)
            .joinToString(" ")
        val candidate = if (sentences.isNotBlank()) sentences else cleaned
        return if (candidate.length > 165) candidate.take(162).trimEnd() + "..." else candidate
    }

    private fun stripHtmlToText(html: String): String {
        val withoutScripts = scriptStyleRegex.replace(html, " ")
        val withoutTags = tagRegex.replace(withoutScripts, " ")
        return decodeEntities(withoutTags).replace(whitespaceRegex, " ").trim()
    }

    private fun decodeEntities(text: String): String {
        return text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&rarr;", "->")
            .replace("&bull;", "•")
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    private fun countMatches(text: String, keywords: List<String>): Int {
        var score = 0
        for (kw in keywords) {
            if (text.contains(kw)) score++
        }
        return score
    }

    private fun sha1Short(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
        return digest.take(6).joinToString("") { "%02x".format(it) }
    }
}
