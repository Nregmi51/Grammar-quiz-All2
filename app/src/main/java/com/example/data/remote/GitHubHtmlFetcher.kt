package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

data class FetchedHtmlFile(
    val fileName: String,
    val rawUrl: String,
    val htmlContent: String
)

sealed class GitHubFetchResult {
    data class Success(val files: List<FetchedHtmlFile>, val resolvedRepoLabel: String) : GitHubFetchResult()
    data class Error(val message: String) : GitHubFetchResult()
}

@JsonClass(generateAdapter = true)
data class GitHubContentEntry(
    val name: String,
    val path: String,
    val type: String,
    @Json(name = "download_url") val downloadUrl: String? = null,
    val url: String? = null
)

class GitHubHtmlFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, GitHubContentEntry::class.java)
    private val listAdapter = moshi.adapter<List<GitHubContentEntry>>(listType)
    private val singleAdapter = moshi.adapter(GitHubContentEntry::class.java)

    suspend fun fetchHtmlFiles(inputUrl: String): GitHubFetchResult = withContext(Dispatchers.IO) {
        val trimmed = inputUrl.trim()
        if (trimmed.isBlank()) {
            return@withContext GitHubFetchResult.Error("Please enter a GitHub repository, folder, or HTML file link.")
        }

        // Direct raw HTML file URL
        if (trimmed.startsWith("https://raw.githubusercontent.com/") ||
            (trimmed.startsWith("http") && (trimmed.endsWith(".html", true) || trimmed.endsWith(".htm", true)) && !trimmed.contains("/blob/"))
        ) {
            val content = downloadText(trimmed)
                ?: return@withContext GitHubFetchResult.Error("Could not download HTML from the provided URL.")
            val fileName = decodeFileName(trimmed.substringAfterLast('/')).ifBlank { "lesson.html" }
            return@withContext GitHubFetchResult.Success(
                files = listOf(FetchedHtmlFile(fileName, trimmed, content)),
                resolvedRepoLabel = fileName
            )
        }

        val parsed = parseGitHubTarget(trimmed)
            ?: return@withContext GitHubFetchResult.Error(
                "Unrecognized GitHub link. Use e.g. https://github.com/user/repo or user/repo."
            )

        // Single blob file URL -> convert directly to raw.githubusercontent.com
        if (parsed.isBlobFile && parsed.branch != null) {
            val rawUrl = "https://raw.githubusercontent.com/${parsed.owner}/${parsed.repo}/${parsed.branch}/${parsed.path}"
            val content = downloadText(rawUrl)
                ?: return@withContext GitHubFetchResult.Error("Failed to download HTML file from GitHub.")
            val fileName = decodeFileName(parsed.path.substringAfterLast('/')).ifBlank { "lesson.html" }
            return@withContext GitHubFetchResult.Success(
                files = listOf(FetchedHtmlFile(fileName, rawUrl, content)),
                resolvedRepoLabel = "${parsed.owner}/${parsed.repo}"
            )
        }

        val collected = mutableListOf<FetchedHtmlFile>()
        val errorMessage = collectHtmlRecursive(
            owner = parsed.owner,
            repo = parsed.repo,
            path = parsed.path,
            branch = parsed.branch,
            depth = 0,
            collected = collected
        )

        if (errorMessage != null && collected.isEmpty()) {
            return@withContext GitHubFetchResult.Error(errorMessage)
        }
        if (collected.isEmpty()) {
            return@withContext GitHubFetchResult.Error(
                "No .html or .htm files were found in ${parsed.owner}/${parsed.repo}."
            )
        }

        GitHubFetchResult.Success(
            files = collected,
            resolvedRepoLabel = "${parsed.owner}/${parsed.repo}"
        )
    }

    private fun collectHtmlRecursive(
        owner: String,
        repo: String,
        path: String,
        branch: String?,
        depth: Int,
        collected: MutableList<FetchedHtmlFile>
    ): String? {
        if (depth > 3 || collected.size >= 100) return null

        val cleanPath = path.trim('/')
        val apiUrl = buildString {
            append("https://api.github.com/repos/$owner/$repo/contents")
            if (cleanPath.isNotEmpty()) append("/$cleanPath")
            if (!branch.isNullOrBlank()) append("?ref=$branch")
        }

        val request = Request.Builder()
            .url(apiUrl)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "GrammarCraft-Android")
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return when (response.code) {
                        404 -> "GitHub repository or path not found (404). Make sure the repo is public."
                        403 -> "GitHub API rate limit reached (403). Try again shortly or link a raw file URL."
                        else -> "GitHub returned HTTP ${response.code}."
                    }
                }
                val body = response.body?.string() ?: return "Empty response from GitHub."

                if (body.trimStart().startsWith("[")) {
                    val entries = listAdapter.fromJson(body) ?: emptyList()
                    for (entry in entries) {
                        if (collected.size >= 100) break
                        if (entry.type == "file" && (entry.name.endsWith(".html", true) || entry.name.endsWith(".htm", true))) {
                            val rawUrl = entry.downloadUrl ?: continue
                            val html = downloadText(rawUrl)
                            if (!html.isNullOrBlank()) {
                                collected.add(FetchedHtmlFile(entry.name, rawUrl, html))
                            }
                        } else if (entry.type == "dir") {
                            collectHtmlRecursive(owner, repo, entry.path, branch, depth + 1, collected)
                        }
                    }
                } else {
                    val single = singleAdapter.fromJson(body)
                    if (single != null && single.type == "file" && single.downloadUrl != null) {
                        val html = downloadText(single.downloadUrl)
                        if (!html.isNullOrBlank()) {
                            collected.add(FetchedHtmlFile(single.name, single.downloadUrl, html))
                        }
                    }
                }
            }
        }.onFailure { err ->
            return "Network error while connecting to GitHub: ${err.localizedMessage ?: "Check internet connection"}"
        }
        return null
    }

    private fun downloadText(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "GrammarCraft-Android")
            .build()
        return runCatching {
            client.newCall(request).execute().use { resp ->
                if (resp.isSuccessful) resp.body?.string() else null
            }
        }.getOrNull()
    }

    private fun decodeFileName(raw: String): String {
        return runCatching { URLDecoder.decode(raw, "UTF-8") }.getOrDefault(raw)
    }

    private data class ParsedTarget(
        val owner: String,
        val repo: String,
        val branch: String?,
        val path: String,
        val isBlobFile: Boolean
    )

    private fun parseGitHubTarget(input: String): ParsedTarget? {
        val sanitized = input
            .trim()
            .removeSurrounding("'")
            .removeSurrounding("\"")
            .removeSuffix(".git")
            .trimEnd('/')

        val pathPart = when {
            sanitized.startsWith("https://www.github.com/", ignoreCase = true) ->
                sanitized.substring("https://www.github.com/".length)
            sanitized.startsWith("http://www.github.com/", ignoreCase = true) ->
                sanitized.substring("http://www.github.com/".length)
            sanitized.startsWith("www.github.com/", ignoreCase = true) ->
                sanitized.substring("www.github.com/".length)
            sanitized.startsWith("https://github.com/", ignoreCase = true) ->
                sanitized.substring("https://github.com/".length)
            sanitized.startsWith("http://github.com/", ignoreCase = true) ->
                sanitized.substring("http://github.com/".length)
            sanitized.startsWith("github.com/", ignoreCase = true) ->
                sanitized.substring("github.com/".length)
            !sanitized.contains("://") && sanitized.contains('/') -> sanitized
            else -> return null
        }

        val segments = pathPart.split('/').filter { it.isNotBlank() }
        if (segments.size < 2) return null

        val owner = segments[0]
        val repo = segments[1]

        return when {
            segments.size >= 4 && segments[2] == "tree" -> ParsedTarget(
                owner = owner,
                repo = repo,
                branch = segments[3],
                path = segments.drop(4).joinToString("/"),
                isBlobFile = false
            )
            segments.size >= 4 && segments[2] == "blob" -> ParsedTarget(
                owner = owner,
                repo = repo,
                branch = segments[3],
                path = segments.drop(4).joinToString("/"),
                isBlobFile = true
            )
            segments.size > 2 -> ParsedTarget(
                owner = owner,
                repo = repo,
                branch = null,
                path = segments.drop(2).joinToString("/"),
                isBlobFile = false
            )
            else -> ParsedTarget(
                owner = owner,
                repo = repo,
                branch = null,
                path = "",
                isBlobFile = false
            )
        }
    }
}
