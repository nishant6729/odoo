// RetrofitClient.kt
package com.example.skillswaps.ImageBackend

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val instance: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.GEMINI_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
}
