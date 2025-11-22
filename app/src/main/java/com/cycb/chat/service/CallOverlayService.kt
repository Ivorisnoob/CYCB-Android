package com.cycb.chat.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import com.cycb.chat.R
import com.cycb.chat.utils.VoiceCallManager

class CallOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "CallOverlayService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_OVERLAY -> {
                val chatName = intent.getStringExtra(EXTRA_CHAT_NAME) ?: "Voice Call"
                val callDuration = intent.getStringExtra(EXTRA_CALL_DURATION) ?: "00:00"
                showOverlay(chatName, callDuration)
            }
            ACTION_UPDATE_OVERLAY -> {
                val callDuration = intent.getStringExtra(EXTRA_CALL_DURATION) ?: "00:00"
                updateOverlay(callDuration)
            }
            ACTION_STOP_OVERLAY -> {
                stopOverlay()
            }
        }
        return START_STICKY
    }

    private fun showOverlay(chatName: String, callDuration: String) {
        if (overlayView != null) {
            Log.d(TAG, "Overlay already showing")
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        overlayView = LayoutInflater.from(this).inflate(
            R.layout.call_overlay_layout,
            null
        )

        overlayView?.let { view ->

            val callDurationText = view.findViewById<TextView>(R.id.callDurationText)
            val muteButton = view.findViewById<ImageButton>(R.id.muteButton)
            val endCallButton = view.findViewById<ImageButton>(R.id.endCallButton)
            val expandButton = view.findViewById<ImageButton>(R.id.expandButton)
            val dragHandle = view.findViewById<View>(R.id.dragHandle)

            callDurationText?.text = callDuration

            muteButton?.setOnClickListener {
                VoiceCallManager.getInstance(applicationContext).toggleMute()
                updateMuteButton(muteButton)
            }

            endCallButton?.setOnClickListener {
                sendBroadcast(Intent(ACTION_END_CALL))
                stopOverlay()
            }

            expandButton?.setOnClickListener {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(launchIntent)
                stopOverlay()
            }

            val topArea = view.findViewById<View>(R.id.topInfoArea)
            val dragTouchListener = object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager?.updateViewLayout(overlayView, params)
                            return true
                        }
                    }
                    return false
                }
            }

            dragHandle?.setOnTouchListener(dragTouchListener)
            topArea?.setOnTouchListener(dragTouchListener)

            windowManager?.addView(view, params)
            Log.d(TAG, "Overlay view added")
        }
    }

    private fun updateOverlay(callDuration: String) {
        overlayView?.let { view ->
            val callDurationText = view.findViewById<TextView>(R.id.callDurationText)
            callDurationText?.text = callDuration
        }
    }

    private fun updateMuteButton(muteButton: ImageButton) {
        val isMuted = VoiceCallManager.getInstance(applicationContext).isMuted.value
        muteButton.setImageResource(
            if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic
        )
    }

    private fun stopOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
        stopSelf()
        Log.d(TAG, "Overlay stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopOverlay()
    }

    companion object {
        private const val TAG = "CallOverlayService"

        const val ACTION_START_OVERLAY = "com.cycb.chat.START_OVERLAY"
        const val ACTION_UPDATE_OVERLAY = "com.cycb.chat.UPDATE_OVERLAY"
        const val ACTION_STOP_OVERLAY = "com.cycb.chat.STOP_OVERLAY"
        const val ACTION_END_CALL = "com.cycb.chat.END_CALL"

        const val EXTRA_CHAT_NAME = "chat_name"
        const val EXTRA_CALL_DURATION = "call_duration"

        fun startOverlay(context: Context, chatName: String, callDuration: String) {
            val intent = Intent(context, CallOverlayService::class.java).apply {
                action = ACTION_START_OVERLAY
                putExtra(EXTRA_CHAT_NAME, chatName)
                putExtra(EXTRA_CALL_DURATION, callDuration)
            }
            context.startService(intent)
        }

        fun updateOverlay(context: Context, callDuration: String) {
            val intent = Intent(context, CallOverlayService::class.java).apply {
                action = ACTION_UPDATE_OVERLAY
                putExtra(EXTRA_CALL_DURATION, callDuration)
            }
            context.startService(intent)
        }

        fun stopOverlay(context: Context) {
            val intent = Intent(context, CallOverlayService::class.java).apply {
                action = ACTION_STOP_OVERLAY
            }
            context.startService(intent)
        }
    }
}
