// GeminiResponse.kt
package com.example.skillswaps.ImageBackend

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: ContentResponse?
)

data class ContentResponse(
    val parts: List<PartResponse>
)

data class PartResponse(
    val text: String
)
