package com.example.skillswaps.ImageBackend

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

object VisionRequestHelper {

    private const val API_KEY = "YOUR_API_KEY" // Replace with your actual API key
    private const val VISION_API_URL =
        "https://vision.googleapis.com/v1/images:annotate?key=$API_KEY"

    fun buildRequest(base64Image: String): Request {
        val json = """
        {
            "requests": [
                {
                    "image": {
                        "content": "$base64Image"
                    },
                    "features": [
                        {
                            "type": "SAFE_SEARCH_DETECTION"
                        }
                    ]
                }
            ]
        }
    """.trimIndent()

        Log.d("VisionAPI", "Request JSON: $json")

        return Request.Builder()
            .url(VISION_API_URL)
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
    }

    fun parseSafeSearchJson(json: String?): String {
        return try {
            val gson = Gson()
            val visionResponse = gson.fromJson(json, VisionResponse::class.java)
            val annotation = visionResponse.responses.firstOrNull()?.safeSearchAnnotation

            Log.d("VisionAPI", "Parsed annotation: $annotation")

            // Combine labels
            "${annotation?.adult}, ${annotation?.racy}"
        } catch (e: Exception) {
            Log.e("VisionAPI", "parseSafeSearchJson error: ${e.message}")
            "unknown"
        }
    }

    data class VisionResponse(
        val responses: List<AnnotateImageResponse>
    )

    data class AnnotateImageResponse(
        @SerializedName("safeSearchAnnotation")
        val safeSearchAnnotation: SafeSearchAnnotation
    )

    data class SafeSearchAnnotation(
        val adult: String = "UNKNOWN",
        val spoof: String = "UNKNOWN",
        val medical: String = "UNKNOWN",
        val violence: String = "UNKNOWN",
        val racy: String = "UNKNOWN"
    )
}
