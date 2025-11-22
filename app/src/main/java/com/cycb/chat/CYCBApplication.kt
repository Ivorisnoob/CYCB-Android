package com.cycb.chat

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CYCBApplication : Application(), ImageLoaderFactory {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        android.util.Log.d("CYCBApplication", "CYCB Chat Application Starting...")

        initializeApiConfig()

        setupGlobalExceptionHandler()

        android.util.Log.d("CYCBApplication", "Application initialized successfully")
    }

    override fun newImageLoader(): ImageLoader {
        android.util.Log.d("CYCBApplication", "🎨 Configuring Coil ImageLoader with GIF support")

        return ImageLoader.Builder(this)
            .components {

                if (android.os.Build.VERSION.SDK_INT >= 28) {

                    add(ImageDecoderDecoder.Factory())
                } else {

                    add(GifDecoder.Factory())
                }
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024)
                    .build()
            }

            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }

    private fun initializeApiConfig() {
        applicationScope.launch {
            try {
                android.util.Log.d("CYCBApplication", "🌐 Checking API server availability...")
                com.cycb.chat.data.api.ApiConfig.initialize()
                android.util.Log.d("CYCBApplication", "✅ API Config initialized: ${com.cycb.chat.data.api.ApiConfig.BASE_URL}")
            } catch (e: Exception) {
                android.util.Log.e("CYCBApplication", "❌ Failed to initialize API config", e)
            }
        }
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("CYCBApplication", "💥 Uncaught exception in thread ${thread.name}", throwable)

            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        android.util.Log.d("CYCBApplication", "Application terminating")
    }
}
