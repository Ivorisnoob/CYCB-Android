package com.cycb.chat.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException

class VoicePlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var isPrepared = false
    private var currentUrl: String? = null

    companion object {
        private const val TAG = "VoicePlayer"
    }

    suspend fun prepare(url: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (currentUrl == url && isPrepared) {
                return@withContext Result.success(mediaPlayer?.duration ?: 0)
            }

            release()

            val player = MediaPlayer()
            player.setDataSource(url)
            player.setOnCompletionListener {
                isPlaying = false
            }
            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                isPlaying = false
                true
            }
            player.prepare()

            mediaPlayer = player
            isPrepared = true
            currentUrl = url
            val duration = mediaPlayer?.duration ?: 0

            Result.success(duration)
        } catch (e: IOException) {
            Log.e(TAG, "Error preparing audio", e)
            Result.failure(e)
        }
    }

    fun play() {
        try {
            if (isPrepared && !isPlaying) {
                mediaPlayer?.start()
                isPlaying = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting playback", e)
        }
    }

    fun pause() {
        try {
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing playback", e)
        }
    }

    fun stop() {
        try {
            if (isPrepared) {
                mediaPlayer?.stop()
                mediaPlayer?.prepare()
                isPlaying = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback", e)
        }
    }

    fun seekTo(position: Int) {
        try {
            if (isPrepared) {
                mediaPlayer?.seekTo(position)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking", e)
        }
    }

    fun getCurrentPosition(): Int {
        return try {
            if (isPrepared) mediaPlayer?.currentPosition ?: 0 else 0
        } catch (e: Exception) {
            0
        }
    }

    fun getDuration(): Int {
        return try {
            if (isPrepared) mediaPlayer?.duration ?: 0 else 0
        } catch (e: Exception) {
            0
        }
    }

    fun isPlaying(): Boolean = isPlaying

    fun getProgressFlow(): Flow<Float> = flow {
        while (isPlaying) {
            try {
                val current = getCurrentPosition()
                val duration = getDuration()
                val progress = if (duration > 0) current.toFloat() / duration else 0f
                emit(progress)
                delay(50)
            } catch (e: Exception) {
                emit(0f)
            }
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            isPrepared = false
            currentUrl = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing player", e)
        }
    }

    fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
