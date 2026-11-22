package com.example.data.local

import com.example.domain.HtmlGrammarProcessor
import com.example.model.GrammarItem
import com.example.model.SourceOrigin

object BundledGrammarSeed {

    fun createInitialItems(): List<GrammarItem> {
        val rawSeeds = listOf(
            SeedSpec(
                id = "bundled_verb_tenses_rules",
                fileName = "verb_tenses_reading_rules.html",
                html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <title>Verb Tenses: Present vs. Perfect</title>
                  <style>
                    body {
                      margin: 0; padding: 18px;
                      font-family: "Segoe UI", Roboto, sans-serif;
                      background: linear-gradient(155deg, #0D5C53 0%, #0E4D8A 100%);
                      color: #FFFBE6;
                    }
                    .plate {
                      background: #FFFBE6;
                      color: #0A2347;
                      border-radius: 18px;
                      padding: 18px;
                      border: 2px solid #E5CE73;
                    }
                    .title-pill {
                      background: #0B2956;
                      color: #FFFBE6;
                      padding: 10px 14px;
                      border-radius: 12px;
                      font-size: 18px;
                      font-weight: 700;
                      margin-bottom: 12px;
                    }
                    h2 { color: #0B2956; font-size: 16px; margin-top: 14px; }
                    .example {
                      background: #E9F3FF;
                      border-left: 4px solid #0B2956;
                      padding: 10px 12px;
                      border-radius: 8px;
                      margin: 8px 0;
                    }
                  </style>
                </head>
                <body>
                  <div class="plate">
                    <div class="title-pill">Reading Rules: Present Simple vs. Present Perfect</div>
                    <p>Use <strong>Present Simple</strong> for habits, general truths, and scheduled events. Use <strong>Present Perfect</strong> (has/have + past participle) when a past action connects to the present.</p>
                    <h2>Core Grammar Rules</h2>
                    <ul>
                      <li><strong>Habitual Truth:</strong> Subject + base verb (+ s/es for third-person singular).</li>
                      <li><strong>Present Relevance:</strong> Subject + have/has + past participle (V3).</li>
                      <li><strong>Signal Words:</strong> Always/usually (Simple) vs. already/since/for/yet (Perfect).</li>
                    </ul>
                    <div class="example"><strong>Correct:</strong> She <em>has lived</em> in Pokhara since 2021. (Ongoing state up to now)</div>
                    <div class="example"><strong>Correct:</strong> The train <em>leaves</em> at 7:00 AM every morning. (Timetable)</div>
                  </div>
                </body>
                </html>
                """.trimIndent()
            ),
            SeedSpec(
                id = "bundled_verb_tenses_quiz",
                fileName = "verb_tenses_interactive_quiz.html",
                html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <title>Verb Tenses Quick Quiz</title>
                  <style>
                    body {
                      margin: 0; padding: 18px;
                      font-family: "Segoe UI", Roboto, sans-serif;
                      background: radial-gradient(circle at top right, #164E63, #0F172A);
                      color: #F8FAFC;
                    }
                    .quiz-box {
                      background: rgba(255, 251, 230, 0.96);
                      color: #0B2956;
                      border-radius: 18px;
                      padding: 18px;
                    }
                    h1 {
                      background: #0B2956;
                      color: #FFFBE6;
                      padding: 10px 14px;
                      border-radius: 12px;
                      font-size: 18px;
                      margin: 0 0 14px 0;
                    }
                    .q { margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px dashed #CBD5E1; }
                    label { display: block; margin: 6px 0; padding: 8px 10px; border-radius: 8px; background: #FFF5CC; cursor: pointer; }
                    button {
                      background: #0D6E5E; color: #FFFBE6; border: none;
                      padding: 12px 18px; border-radius: 12px; font-weight: 700; font-size: 15px; width: 100%;
                    }
                    #score { margin-top: 12px; font-weight: 700; font-size: 16px; color: #0B2956; }
                  </style>
                </head>
                <body>
                  <div class="quiz-box">
                    <h1>Quiz: Verb Tenses Mastery</h1>
                    <div class="q">
                      <p><strong>1. By the time we arrived, the lecture ________.</strong></p>
                      <label><input type="radio" name="q1" value="a"> already starts</label>
                      <label><input type="radio" name="q1" value="b"> had already started</label>
                      <label><input type="radio" name="q1" value="c"> has start</label>
                    </div>
                    <div class="q">
                      <p><strong>2. Look! It ________ right now.</strong></p>
                      <label><input type="radio" name="q2" value="a"> is raining</label>
                      <label><input type="radio" name="q2" value="b"> rains</label>
                      <label><input type="radio" name="q2" value="c"> rained</label>
                    </div>
                    <div class="q">
                      <p><strong>3. Neither of the students ________ finished the essay yet.</strong></p>
                      <label><input type="radio" name="q3" value="a"> have</label>
                      <label><input type="radio" name="q3" value="b"> has</label>
                      <label><input type="radio" name="q3" value="c"> are</label>
                    </div>
                    <button type="button" onclick="checkQuiz()">Check My Answers</button>
                    <div id="score"></div>
                  </div>
                  <script>
                    function checkQuiz() {
                      const ans = { q1: 'b', q2: 'a', q3: 'b' };
                      let correct = 0;
                      Object.keys(ans).forEach(k => {
                        const sel = document.querySelector('input[name="' + k + '"]:checked');
                        if (sel && sel.value === ans[k]) correct++;
                      });
                      document.getElementById('score').textContent =
                        'Score: ' + correct + ' / 3 (' + Math.round((correct / 3) * 100) + '%)';
                    }
                  </script>
                </body>
                </html>
                """.trimIndent()
            ),
            SeedSpec(
                id = "bundled_parts_of_speech_watch",
                fileName = "parts_of_speech_watch_slides.html",
                html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <title>Watch: Parts of Speech in Action</title>
                  <style>
                    @keyframes fadeSlide {
                      from { opacity: 0; transform: translateY(8px); }
                      to { opacity: 1; transform: translateY(0); }
                    }
                    body {
                      margin: 0; padding: 18px;
                      font-family: "Segoe UI", Roboto, sans-serif;
                      background: linear-gradient(135deg, #1E3A8A, #065F46);
                      color: #FFFBE6;
                    }
                    .stage {
                      background: #FFFBE6;
                      color: #0A2347;
                      border-radius: 18px;
                      padding: 18px;
                      animation: fadeSlide 0.5s ease-out;
                    }
                    .chip {
                      display: inline-block; background: #0B2956; color: #FFFBE6;
                      padding: 4px 10px; border-radius: 999px; font-size: 12px; font-weight: 700;
                    }
                    .card {
                      margin-top: 12px; padding: 14px; border-radius: 12px;
                      background: #FFF4C2; border: 1.5px solid #E0CC7A;
                    }
                    .controls { display: flex; gap: 10px; margin-top: 14px; }
                    button {
                      flex: 1; padding: 10px; border: none; border-radius: 10px;
                      background: #0B2956; color: #FFFBE6; font-weight: 700;
                    }
                  </style>
                </head>
                <body>
                  <div class="stage">
                    <span class="chip" id="slideTag">Slide 1 of 4 • Noun &amp; Pronoun</span>
                    <h2 id="slideTitle">Nouns Name People, Places &amp; Ideas</h2>
                    <div class="card" id="slideBody">
                      <strong>Example:</strong> The <u>curious student</u> carried a <u>notebook</u> into the <u>library</u>. Pronouns (she, they, it) replace nouns cleanly to avoid repetition.
                    </div>
                    <div class="controls">
                      <button type="button" onclick="prevSlide()">Previous</button>
                      <button type="button" onclick="nextSlide()">Next Slide</button>
                    </div>
                  </div>
                  <script>
                    const slides = [
                      { tag: 'Slide 1 of 4 • Noun & Pronoun', title: 'Nouns Name People, Places & Ideas', body: '<strong>Example:</strong> The <u>curious student</u> carried a <u>notebook</u> into the <u>library</u>. Pronouns (she, they, it) replace nouns cleanly.' },
                      { tag: 'Slide 2 of 4 • Adjectives', title: 'Adjectives Describe Nouns', body: '<strong>Order Rule:</strong> Opinion -> Size -> Age -> Color -> Material. Example: A <em>brilliant small old blue</em> book.' },
                      { tag: 'Slide 3 of 4 • Adverbs', title: 'Adverbs Modify Verbs & Adjectives', body: '<strong>Tip:</strong> Most manner adverbs end in <em>-ly</em> (quietly, carefully), while frequency adverbs (always, seldom) sit before main verbs.' },
                      { tag: 'Slide 4 of 4 • Prepositions', title: 'Prepositions Show Time & Space', body: '<strong>Rule:</strong> Use <em>at</em> for exact times (at 5 PM), <em>on</em> for days/dates (on Monday), and <em>in</em> for months/years (in June).' }
                    ];
                    let idx = 0;
                    function render() {
                      document.getElementById('slideTag').textContent = slides[idx].tag;
                      document.getElementById('slideTitle').textContent = slides[idx].title;
                      document.getElementById('slideBody').innerHTML = slides[idx].body;
                    }
                    function nextSlide() { idx = (idx + 1) % slides.length; render(); }
                    function prevSlide() { idx = (idx - 1 + slides.length) % slides.length; render(); }
                  </script>
                </body>
                </html>
                """.trimIndent()
            ),
            SeedSpec(
                id = "bundled_clauses_reading_rules",
                fileName = "clauses_and_conditionals_rules.html",
                html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <title>Clauses &amp; Conditionals Reading Guide</title>
                  <style>
                    body {
                      margin: 0; padding: 18px;
                      font-family: Georgia, serif;
                      background: #FEF6D8;
                      color: #0B2545;
                    }
                    h1 {
                      background: #0B2956; color: #FFFBE6; padding: 12px 14px;
                      border-radius: 10px; font-size: 19px; font-family: sans-serif;
                    }
                    .rule-card {
                      background: #FFFFFF; border: 1.5px solid #D9C478;
                      border-radius: 12px; padding: 14px; margin-top: 12px;
                    }
                  </style>
                </head>
                <body>
                  <h1>Sentence Structure: Clauses &amp; Conditionals</h1>
                  <div class="rule-card">
                    <h3>1. Independent vs. Dependent Clauses</h3>
                    <p>An <strong>independent clause</strong> expresses a complete thought. A <strong>dependent clause</strong> begins with a subordinator (because, although, if, when) and must attach to an independent clause.</p>
                  </div>
                  <div class="rule-card">
                    <h3>2. Conditional Types (0, 1, 2, 3)</h3>
                    <ul>
                      <li><strong>Zero:</strong> If + Present, Present (Facts: If water freezes, it expands.)</li>
                      <li><strong>First:</strong> If + Present, will + Verb (Likely future)</li>
                      <li><strong>Second:</strong> If + Past, would + Verb (Imaginary present)</li>
                    </ul>
                  </div>
                </body>
                </html>
                """.trimIndent()
            ),
            SeedSpec(
                id = "bundled_passive_voice_quiz",
                fileName = "active_passive_voice_quiz.html",
                html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <title>Active &amp; Passive Voice Exercise</title>
                  <style>
                    body {
                      margin: 0; padding: 18px;
                      font-family: "Segoe UI", sans-serif;
                      background: linear-gradient(145deg, #064E3B 0%, #1E40AF 100%);
                      color: #FFFBE6;
                    }
                    .panel {
                      background: #FFFBE6; color: #0B2545; border-radius: 16px; padding: 16px;
                    }
                    .hdr {
                      background: #0B2956; color: #FFFBE6; padding: 10px 14px;
                      border-radius: 10px; font-weight: 700; margin-bottom: 12px;
                    }
                    .item { background: #FFF3C4; padding: 12px; border-radius: 10px; margin-bottom: 10px; }
                    button {
                      background: #0B2956; color: #FFFBE6; border: none; border-radius: 8px;
                      padding: 8px 12px; margin-top: 6px; font-weight: 600;
                    }
                    .ans { display: none; margin-top: 8px; color: #0F766E; font-weight: 700; }
                  </style>
                </head>
                <body>
                  <div class="panel">
                    <div class="hdr">Active &amp; Passive Voice Transformation</div>
                    <div class="item">
                      <div><strong>Active:</strong> The committee reviewed the grammar curriculum.</div>
                      <button type="button" onclick="toggle('a1')">Reveal Passive Form</button>
                      <div id="a1" class="ans">Passive: The grammar curriculum was reviewed by the committee.</div>
                    </div>
                    <div class="item">
                      <div><strong>Active:</strong> Scientists are testing a new solar battery.</div>
                      <button type="button" onclick="toggle('a2')">Reveal Passive Form</button>
                      <div id="a2" class="ans">Passive: A new solar battery is being tested by scientists.</div>
                    </div>
                  </div>
                  <script>
                    function toggle(id) {
                      const el = document.getElementById(id);
                      el.style.display = (el.style.display === 'block') ? 'none' : 'block';
                    }
                  </script>
                </body>
                </html>
                """.trimIndent()
            ),
            SeedSpec(
                id = "bundled_design_composition",
                fileName = "design_composition_paragraph_craft.html",
                html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <title>Design Composition: Persuasive Paragraph</title>
                  <style>
                    body {
                      margin: 0; padding: 18px;
                      font-family: "Segoe UI", sans-serif;
                      background: linear-gradient(180deg, #0D6E5E 0%, #0D4E8B 100%);
                      color: #0A2347;
                    }
                    .sheet {
                      background: #FFFDF0; border-radius: 18px; padding: 18px;
                      border: 2px solid #E2CE7D;
                    }
                    .banner {
                      background: #0B2956; color: #FFFBE6; padding: 12px 14px;
                      border-radius: 12px; margin-bottom: 12px;
                    }
                    .step {
                      background: #FFF4CC; padding: 10px 12px; border-radius: 10px; margin: 8px 0;
                      border-left: 4px solid #0E7C6B;
                    }
                  </style>
                </head>
                <body>
                  <div class="sheet">
                    <div class="banner">
                      <strong>Design Composition Blueprint</strong><br/>
                      <small>Topic Sentence -> Evidence -> Grammar Polish -> Closing Link</small>
                    </div>
                    <div class="step"><strong>1. Topic Sentence:</strong> State one crisp claim in active voice.</div>
                    <div class="step"><strong>2. Supporting Detail:</strong> Connect ideas with transitions (furthermore, consequently, in contrast).</div>
                    <div class="step"><strong>3. Punctuation Check:</strong> Use commas after introductory clauses and semicolons between closely linked independent clauses.</div>
                    <div class="step"><strong>Model Paragraph:</strong> <em>"Daily reading strengthens grammatical intuition; moreover, reviewing a single rule each morning builds lasting writing confidence."</em></div>
                  </div>
                </body>
                </html>
                """.trimIndent()
            )
        )

        return rawSeeds.map { spec ->
            HtmlGrammarProcessor.processHtml(
                rawHtml = spec.html,
                fileName = spec.fileName,
                sourceUrl = "bundled://${spec.fileName}",
                sourceOrigin = SourceOrigin.BUNDLED,
                explicitId = spec.id
            )
        }
    }

    private data class SeedSpec(
        val id: String,
        val fileName: String,
        val html: String
    )
}
