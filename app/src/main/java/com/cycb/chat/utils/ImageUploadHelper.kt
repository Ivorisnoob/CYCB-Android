package com.cycb.chat.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object ImageUploadHelper {
    private const val TAG = "ImageUploadHelper"

    suspend fun uploadToBackend(
        context: Context,
        imageUri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting upload for URI: $imageUri")

            val file = uriToFile(context, imageUri)
            Log.d(TAG, "File created: ${file.name}, size: ${file.length()} bytes")

            val mimeType = when {
                file.name.endsWith(".jpg", ignoreCase = true) ||
                file.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                file.name.endsWith(".png", ignoreCase = true) -> "image/png"
                file.name.endsWith(".gif", ignoreCase = true) -> "image/gif"
                file.name.endsWith(".webp", ignoreCase = true) -> "image/webp"
                else -> "image/jpeg"
            }

            Log.d(TAG, "MIME type: $mimeType")

            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val imagePart = okhttp3.MultipartBody.Part.createFormData("image", file.name, requestFile)

            Log.d(TAG, "Uploading to backend...")

            val response = com.cycb.chat.data.api.RetrofitClient.apiService.uploadImage(imagePart)

            Log.d(TAG, "Upload response: success=${response.success}, url=${response.url}, message=${response.message}")

            file.delete()

            if (response.success && response.url.isNotEmpty()) {
                Result.success(response.url)
            } else {
                Result.failure(Exception("Upload failed: ${response.message}"))
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "HTTP ${e.code()} error: $errorBody", e)
            Result.failure(Exception("Upload failed (${e.code()}): $errorBody"))
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePicture(
        context: Context,
        imageUri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(context, imageUri)
            val requestFile = file.asRequestBody("image
    suspend fun uploadToCloudinary(
        context: Context,
        imageUri: Uri,
        cloudName: String,
        uploadPreset: String,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(context, imageUri)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody("image
    suspend fun uploadToImgBB(
        context: Context,
        imageUri: Uri,
        apiKey: String,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(context, imageUri)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.name, file.asRequestBody("image
    private fun uriToFile(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        val fileName = getFileName(context, uri)
        val tempFile = File(context.cacheDir, fileName)

        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "image_${System.currentTimeMillis()}.jpg"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    suspend fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): Uri = withContext(Dispatchers.IO) {
        try {
            val bitmap = android.graphics.BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri)
            )

            var width = bitmap.width
            var height = bitmap.height

            if (width > maxWidth || height > maxHeight) {
                val ratio = width.toFloat() / height.toFloat()
                if (ratio > 1) {
                    width = maxWidth
                    height = (maxWidth / ratio).toInt()
                } else {
                    height = maxHeight
                    width = (maxHeight * ratio).toInt()
                }
            }

            val resized = android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)

            val tempFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { out ->
                resized.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, out)
            }

            bitmap.recycle()
            resized.recycle()

            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            Log.e(TAG, "Compression error", e)
            uri
        }
    }
}
