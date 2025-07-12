package com.example.skillswaps.ImageBackend

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class ImageHandler {

    companion object {
        private const val TAG = "ImageHandler"
    }


    suspend fun uploadImageToCloudinaryAndSaveToFirestore(
        uri: Uri,
        userId: String,
        contentResolver: ContentResolver,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // 1) Make sure we are using the exact Cloudinary "cloud_name" from your dashboard:
        val cloudName ="dlsw9mhhq"      // ← replace with your actual cloud name
        val uploadPreset = "SkillSwap"   // ← the name of your unsigned upload preset

        // 2) Build the Cloudinary upload URL
        val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

        // 3) Read all bytes from the Uri (using 'use { }' so the InputStream closes automatically)
        val imageBytes: ByteArray? = try {
            contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read image bytes: ${e.localizedMessage}", e)
            // Switch to Main before calling onFailure
            withContext(Dispatchers.Main) {
                onFailure("Failed to read image data: ${e.localizedMessage}")
            }
            return
        }

        if (imageBytes == null) {
            Log.e(TAG, "ImageByteArray was null after reading Uri")
            withContext(Dispatchers.Main) {
                onFailure("Could not load image data")
            }
            return
        }

        // 4) Build a multipart/form-data request body for Cloudinary
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                name = "file",
                filename = "upload.jpg",
                body = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
            )
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        // 5) Perform the HTTP request on the IO dispatcher
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    Log.e(TAG, "Cloudinary upload failed: HTTP ${response.code}, message=${response.message}")
                    withContext(Dispatchers.Main) {
                        onFailure("Cloudinary upload failed: ${response.message}")
                    }
                    return@withContext
                }

                // 6) Parse the returned JSON for "secure_url"
                val imageUrl = Regex("\"secure_url\":\"(.*?)\"")
                    .find(responseBody)
                    ?.groupValues
                    ?.get(1)

                if (imageUrl.isNullOrEmpty()) {
                    Log.e(TAG, "Could not extract secure_url from Cloudinary response: $responseBody")
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to parse image URL from Cloudinary")
                    }
                    return@withContext
                }

                Log.d(TAG, "Cloudinary upload succeeded: $imageUrl")

                // 7) Now update Firestore under the SAME collection you use elsewhere (e.g. "data/{userId}")
                try {
                    FirebaseFirestore.getInstance()
                        .collection("data")        // ← use "data" rather than "users"
                        .document(userId)
                        .update("imageUrl", imageUrl)
                        .await()                   // suspend until the Firestore write completes

                    Log.d(TAG, "Firestore update succeeded: data/$userId ← imageUrl=$imageUrl")

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Firestore update failed: ${e.localizedMessage}", e)
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to save image URL in Firestore: ${e.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network/API error during Cloudinary upload: ${e.localizedMessage}", e)
                withContext(Dispatchers.Main) {
                    onFailure("Network error: ${e.localizedMessage}")
                }
            }
        }
    }
    suspend fun uploadImageToCloudinaryAndSaveToFirestore2(
        uri: Uri,
        userId: String,
        contentResolver: ContentResolver,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // 1) Make sure we are using the exact Cloudinary "cloud_name" from your dashboard:
        val cloudName ="dlsw9mhhq"      // ← replace with your actual cloud name
        val uploadPreset = "SkillSwap"   // ← the name of your unsigned upload preset

        // 2) Build the Cloudinary upload URL
        val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"

        // 3) Read all bytes from the Uri (using 'use { }' so the InputStream closes automatically)
        val imageBytes: ByteArray? = try {
            contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read image bytes: ${e.localizedMessage}", e)
            // Switch to Main before calling onFailure
            withContext(Dispatchers.Main) {
                onFailure("Failed to read image data: ${e.localizedMessage}")
            }
            return
        }

        if (imageBytes == null) {
            Log.e(TAG, "ImageByteArray was null after reading Uri")
            withContext(Dispatchers.Main) {
                onFailure("Could not load image data")
            }
            return
        }

        // 4) Build a multipart/form-data request body for Cloudinary
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                name = "file",
                filename = "upload.jpg",
                body = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
            )
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        // 5) Perform the HTTP request on the IO dispatcher
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    Log.e(TAG, "Cloudinary upload failed: HTTP ${response.code}, message=${response.message}")
                    withContext(Dispatchers.Main) {
                        onFailure("Cloudinary upload failed: ${response.message}")
                    }
                    return@withContext
                }

                // 6) Parse the returned JSON for "secure_url"
                val imageUrl = Regex("\"secure_url\":\"(.*?)\"")
                    .find(responseBody)
                    ?.groupValues
                    ?.get(1)

                if (imageUrl.isNullOrEmpty()) {
                    Log.e(TAG, "Could not extract secure_url from Cloudinary response: $responseBody")
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to parse image URL from Cloudinary")
                    }
                    return@withContext
                }

                Log.d(TAG, "Cloudinary upload succeeded: $imageUrl")

                // 7) Now update Firestore under the SAME collection you use elsewhere (e.g. "data/{userId}")
                try {
                    FirebaseFirestore.getInstance()
                        .collection("communityPosts")        // ← use "data" rather than "users"
                        .document(userId)
                        .update("imageUrl", imageUrl)
                        .await()                   // suspend until the Firestore write completes

                    Log.d(TAG, "Firestore update succeeded: data/$userId ← imageUrl=$imageUrl")

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Firestore update failed: ${e.localizedMessage}", e)
                    withContext(Dispatchers.Main) {
                        onFailure("Failed to save image URL in Firestore: ${e.localizedMessage}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network/API error during Cloudinary upload: ${e.localizedMessage}", e)
                withContext(Dispatchers.Main) {
                    onFailure("Network error: ${e.localizedMessage}")
                }
            }
        }
    }

}
