package com.cycb.chat.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class VoiceRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    private var startTime: Long = 0

    companion object {
        private const val TAG = "VoiceRecorder"
        private const val SAMPLE_RATE = 44100
        private const val BIT_RATE = 128000
        private const val MAX_DURATION_MS = 300000
    }

    suspend fun startRecording(): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (isRecording) {
                return@withContext Result.failure(Exception("Already recording"))
            }

            val fileName = "voice_${System.currentTimeMillis()}.m4a"
            outputFile = File(context.cacheDir, fileName)

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(SAMPLE_RATE)
                setAudioEncodingBitRate(BIT_RATE)
                setOutputFile(outputFile?.absolutePath)
                setMaxDuration(MAX_DURATION_MS)

                try {
                    prepare()
                    start()
                    isRecording = true
                    startTime = System.currentTimeMillis()
                    Log.d(TAG, "Recording started: ${outputFile?.absolutePath}")
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to start recording", e)
                    release()
                    throw e
                }
            }

            Result.success(outputFile!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            Result.failure(e)
        }
    }

    suspend fun stopRecording(): Result<Pair<File, Long>> = withContext(Dispatchers.IO) {
        try {
            if (!isRecording) {
                return@withContext Result.failure(Exception("Not recording"))
            }

            val duration = System.currentTimeMillis() - startTime

            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping recording", e)
                }
            }
            mediaRecorder = null
            isRecording = false

            val file = outputFile ?: return@withContext Result.failure(Exception("No output file"))

            if (!file.exists() || file.length() == 0L) {
                return@withContext Result.failure(Exception("Recording file is empty"))
            }

            Log.d(TAG, "Recording stopped: ${file.absolutePath}, duration: ${duration}ms")
            Result.success(Pair(file, duration))
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            cleanup()
            Result.failure(e)
        }
    }

    suspend fun cancelRecording() = withContext(Dispatchers.IO) {
        try {
            if (isRecording) {
                mediaRecorder?.apply {
                    try {
                        stop()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error stopping recording during cancel", e)
                    }
                    release()
                }
                mediaRecorder = null
                isRecording = false
            }

            outputFile?.delete()
            outputFile = null
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling recording", e)
        }
    }

    fun getAmplitudeFlow(): Flow<Int> = flow {
        while (isRecording) {
            try {
                val amplitude = mediaRecorder?.maxAmplitude ?: 0

                val normalized = (amplitude / 327.67).toInt().coerceIn(0, 100)
                emit(normalized)
                delay(50)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting amplitude", e)
                emit(0)
            }
        }
    }

    fun getDuration(): Long {
        return if (isRecording) {
            System.currentTimeMillis() - startTime
        } else {
            0L
        }
    }

    fun isRecording(): Boolean = isRecording

    private fun cleanup() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            outputFile?.delete()
            outputFile = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
