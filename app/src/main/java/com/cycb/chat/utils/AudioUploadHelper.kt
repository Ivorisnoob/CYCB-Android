package com.cycb.chat.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object AudioUploadHelper {
    private const val TAG = "AudioUploadHelper"

    suspend fun uploadAudio(
        context: Context,
        audioFile: File,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!audioFile.exists()) {
                return@withContext Result.failure(Exception("Audio file does not exist"))
            }

            Log.d(TAG, "Uploading audio: ${audioFile.name}, size: ${audioFile.length()} bytes")

            val requestFile = audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)

            val response = com.cycb.chat.data.api.RetrofitClient.apiService.uploadAudio(audioPart)

            if (response.success && response.url.isNotEmpty()) {
                Log.d(TAG, "Audio uploaded successfully: ${response.url}")
                Result.success(response.url)
            } else {
                Log.e(TAG, "Upload failed: ${response.message}")
                Result.failure(Exception("Upload failed: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
            Result.failure(e)
        }
    }

    fun getAudioDuration(audioFile: File): Long {
        return try {
            val mediaPlayer = android.media.MediaPlayer()
            mediaPlayer.setDataSource(audioFile.absolutePath)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration.toLong()
            mediaPlayer.release()
            duration
        } catch (e: Exception) {
            Log.e(TAG, "Error getting audio duration", e)
            0L
        }
    }

    fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }

    fun getFileSize(file: File): String {
        val bytes = file.length()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
}
