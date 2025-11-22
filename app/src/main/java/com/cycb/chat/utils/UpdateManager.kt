package com.cycb.chat.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.cycb.chat.data.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : DownloadState()
    data class Downloaded(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object UpdateManager {
    private const val PREFS_NAME = "update_prefs"
    private const val KEY_LAST_CHECK = "last_check_time"
    private const val CHECK_INTERVAL = 24 * 60 * 60 * 1000L

    var cachedUpdateInfo: AppUpdateInfo? = null

    suspend fun checkForUpdates(context: Context): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {

            val response = com.cycb.chat.data.api.RetrofitClient.apiService.getLatestAppVersion()

            android.util.Log.d("UpdateManager", "API Response: success=${response.success}, versionCode=${response.versionCode}")

            if (!response.success) {
                android.util.Log.e("UpdateManager", "Update check failed")
                return@withContext null
            }

            val currentVersionCode = com.cycb.chat.BuildConfig.VERSION_CODE
            val isUpdateAvailable = response.versionCode > currentVersionCode

            android.util.Log.d("UpdateManager", "Current: $currentVersionCode, Remote: ${response.versionCode}, UpdateAvailable: $isUpdateAvailable")

            val updateInfo = AppUpdateInfo(
                version = response.version,
                versionCode = response.versionCode,
                releaseNotes = response.releaseNotes,
                downloadUrl = response.downloadUrl,
                fileSize = response.fileSize,
                publishedDate = response.publishedDate,
                releaseName = response.releaseName,
                isUpdateAvailable = isUpdateAvailable
            )

            if (isUpdateAvailable) {
                cachedUpdateInfo = updateInfo
            }

            updateInfo
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Failed to check for updates", e)
            return@withContext null
        }
    }

    fun shouldCheckForUpdates(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0)
        val now = System.currentTimeMillis()
        return (now - lastCheck) > CHECK_INTERVAL
    }

    fun markUpdateChecked(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
    }

    fun downloadUpdateFlow(context: Context, updateInfo: AppUpdateInfo): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading(0f, 0, 0))

        try {
            var url = URL(updateInfo.downloadUrl)
            var connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.connect()

            var responseCode = connection.responseCode
            var redirectCount = 0
            while ((responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                    responseCode == 307 || responseCode == 308) && redirectCount < 5) {
                val location = connection.getHeaderField("Location")
                if (location != null) {
                    url = URL(location)
                    connection = url.openConnection() as HttpURLConnection
                    connection.instanceFollowRedirects = true
                    connection.connect()
                    responseCode = connection.responseCode
                    redirectCount++
                    android.util.Log.d("UpdateManager", "Following redirect to: $location (attempt $redirectCount)")
                } else {
                    break
                }
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                emit(DownloadState.Error("Server returned HTTP $responseCode"))
                return@flow
            }

            var fileLength = connection.contentLength.toLong()

            if (fileLength <= 0) {
                val contentRange = connection.getHeaderField("Content-Range")
                if (contentRange != null) {

                    val parts = contentRange.split("/")
                    if (parts.size == 2) {
                        fileLength = parts[1].toLongOrNull() ?: 0L
                    }
                }
            }

            android.util.Log.d("UpdateManager", "Download starting - fileLength: $fileLength, contentLength: ${connection.contentLength}")

            val input = connection.inputStream
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "cycb-${updateInfo.version}.apk")

            file.parentFile?.mkdirs()

            val output = FileOutputStream(file)

            val data = ByteArray(8192)
            var total: Long = 0
            var count: Int
            var lastProgressUpdate = 0L

            try {
                while (input.read(data).also { count = it } != -1) {
                    total += count
                    output.write(data, 0, count)

                    val now = System.currentTimeMillis()
                    if (now - lastProgressUpdate > 100 || fileLength > 0 && total >= fileLength) {
                        if (fileLength > 0) {
                            val progress = total.toFloat() / fileLength
                            emit(DownloadState.Downloading(progress, total, fileLength))
                            android.util.Log.d("UpdateManager", "Download progress: ${(progress * 100).toInt()}% ($total/$fileLength bytes)")
                        } else {

                            emit(DownloadState.Downloading(0f, total, 0L))
                            android.util.Log.d("UpdateManager", "Downloaded: $total bytes (size unknown)")
                        }
                        lastProgressUpdate = now
                    }
                }
            } finally {
                output.flush()
                output.close()
                input.close()
            }

            android.util.Log.d("UpdateManager", "Download completed: ${file.absolutePath} (${file.length()} bytes)")
            emit(DownloadState.Downloaded(file))

        } catch (e: Exception) {
            emit(DownloadState.Error(e.message ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)

    fun downloadUpdate(context: Context, updateInfo: AppUpdateInfo): Long {
        try {
            val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl))
                .setTitle("CYCB Update")
                .setDescription("Downloading version ${updateInfo.version}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "cycb-${updateInfo.version}.apk"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setMimeType("application/vnd.android.package-archive")

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            android.util.Log.d("UpdateManager", "Download started with ID: $downloadId")
            android.util.Log.d("UpdateManager", "Download URL: ${updateInfo.downloadUrl}")

            return downloadId
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Failed to start download", e)
            throw e
        }
    }

    fun installUpdate(context: Context, apkFile: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                ),
                "application/vnd.android.package-archive"
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }
}
