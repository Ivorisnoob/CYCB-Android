package com.cycb.chat.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (downloadId != -1L) {
                Log.d("DownloadComplete", "Download completed: $downloadId")

                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(statusIndex)

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        val localUri = cursor.getString(uriIndex)

                        Log.d("DownloadComplete", "File downloaded to: $localUri")

                        installApk(context, localUri)
                    } else {
                        Log.e("DownloadComplete", "Download failed with status: $status")
                    }
                }
                cursor.close()
            }
        }
    }

    private fun installApk(context: Context, uriString: String) {
        try {
            val file = File(Uri.parse(uriString).path ?: return)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    val apkUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                } else {
                    setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
                }
            }

            context.startActivity(intent)
            Log.d("DownloadComplete", "Installation intent started")
        } catch (e: Exception) {
            Log.e("DownloadComplete", "Failed to install APK", e)
        }
    }
}
