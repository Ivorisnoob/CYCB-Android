package com.cycb.chat.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

object ImageDownloadHelper {

    suspend fun downloadImage(
        context: Context,
        imageUrl: String,
        fileName: String? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val finalFileName = fileName ?: "CYCB_${timestamp}.jpg"

            val bitmap = downloadBitmap(imageUrl)
                ?: return@withContext Result.failure(Exception("Failed to download image"))

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageMediaStore(context, bitmap, finalFileName)
            } else {
                saveImageLegacy(context, bitmap, finalFileName)
            }

            uri?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Failed to save image"))

        } catch (e: Exception) {
            android.util.Log.e("ImageDownloadHelper", "Error downloading image", e)
            Result.failure(e)
        }
    }

    private suspend fun downloadBitmap(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            android.util.Log.e("ImageDownloadHelper", "Error downloading bitmap", e)
            null
        }
    }

    private fun saveImageMediaStore(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CYCB")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }
                it
            }
        } catch (e: Exception) {
            android.util.Log.e("ImageDownloadHelper", "Error saving with MediaStore", e)
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun saveImageLegacy(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Uri? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val cycbDir = File(downloadsDir, "CYCB")

            if (!cycbDir.exists()) {
                cycbDir.mkdirs()
            }

            val imageFile = File(cycbDir, fileName)
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            }

            val uri = Uri.fromFile(imageFile)
            val scanIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            scanIntent.data = uri
            context.sendBroadcast(scanIntent)

            uri
        } catch (e: Exception) {
            android.util.Log.e("ImageDownloadHelper", "Error saving with legacy method", e)
            null
        }
    }

    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            true
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
}
