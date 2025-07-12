// GeminiApi.kt
package com.example.skillswaps.ImageBackend

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {
    @POST("v1beta/models/gemini-pro-vision:generateContent")
    suspend fun detectViolence(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
