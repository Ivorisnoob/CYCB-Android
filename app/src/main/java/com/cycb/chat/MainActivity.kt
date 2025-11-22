package com.cycb.chat

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cycb.chat.service.CallOverlayService
import com.cycb.chat.ui.CYCBApp
import com.cycb.chat.ui.theme.CYCBChatTheme
import com.cycb.chat.utils.NotificationPermissionHelper
import com.cycb.chat.utils.OverlayPermissionHelper
import com.cycb.chat.viewmodel.VoiceCallViewModel

class MainActivity : ComponentActivity() {
    companion object {
        var hasActiveCall = false
        var currentCallChatName = "Voice Call"
        var currentCallDuration = "00:00"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        if (!NotificationPermissionHelper.hasNotificationPermission(this)) {
            NotificationPermissionHelper.requestNotificationPermission(this)
        }

        setContent {
            val voiceCallViewModel = viewModel<VoiceCallViewModel>()
            val currentCall by voiceCallViewModel.currentCall.collectAsState()

            CYCBChatTheme {
                Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                ) {
                    CYCBApp(
                            initialRoute = intent.getStringExtra("navigateTo"),
                            chatId = intent.getStringExtra("chatId"),
                            userId = intent.getStringExtra("userId")
                    )
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        android.util.Log.d("MainActivity", "onUserLeaveHint called - hasActiveCall: $hasActiveCall")

        if (hasActiveCall) {

            if (OverlayPermissionHelper.hasOverlayPermission(this)) {
                android.util.Log.d("MainActivity", "Starting overlay service")
                CallOverlayService.startOverlay(this, currentCallChatName, currentCallDuration)
            } else {
                android.util.Log.d("MainActivity", "No overlay permission, trying PiP mode")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    android.util.Log.d("MainActivity", "Entering PiP mode")
                    val result = enterPictureInPictureMode(createPipParams())
                    android.util.Log.d("MainActivity", "PiP mode entry result: $result")
                }
            }
        } else {
            android.util.Log.d("MainActivity", "No active call")
        }
    }

    private fun createPipParams(): PictureInPictureParams {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
        } else {
            PictureInPictureParams.Builder().build()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        recreate()
    }

    override fun onResume() {
        super.onResume()

        CallOverlayService.stopOverlay(this)
    }
}
