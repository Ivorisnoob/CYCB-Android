package com.cycb.chat.utils

import android.content.Context
import android.util.Log
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceCallManager(private val context: Context) {

    private var rtcEngine: RtcEngine? = null
    private var currentChannelName: String? = null
    private var currentUid: Int = 0

    private val _isInCall = MutableStateFlow(false)
    val isInCall: StateFlow<Boolean> = _isInCall.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _callMode = MutableStateFlow<CallMode>(CallMode.SPEAKER)
    val callMode: StateFlow<CallMode> = _callMode.asStateFlow()

    private val _participants = MutableStateFlow<Set<Int>>(emptySet())
    val participants: StateFlow<Set<Int>> = _participants.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val eventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d(TAG, "✅ Join channel SUCCESS: channel=$channel, uid=$uid, elapsed=$elapsed")
            _isInCall.value = true
            _connectionState.value = ConnectionState.CONNECTED
            currentUid = uid
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "👤 User joined: uid=$uid, elapsed=$elapsed")
            _participants.value = _participants.value + uid
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "👋 User offline: uid=$uid, reason=$reason")
            _participants.value = _participants.value - uid
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            Log.d(TAG, "📴 Leave channel")
            _isInCall.value = false
            _connectionState.value = ConnectionState.DISCONNECTED
            _participants.value = emptySet()
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            val stateStr = when (state) {
                Constants.CONNECTION_STATE_DISCONNECTED -> "DISCONNECTED"
                Constants.CONNECTION_STATE_CONNECTING -> "CONNECTING"
                Constants.CONNECTION_STATE_CONNECTED -> "CONNECTED"
                Constants.CONNECTION_STATE_RECONNECTING -> "RECONNECTING"
                Constants.CONNECTION_STATE_FAILED -> "FAILED"
                else -> "UNKNOWN($state)"
            }
            val reasonStr = when (reason) {
                Constants.CONNECTION_CHANGED_CONNECTING -> "CONNECTING"
                Constants.CONNECTION_CHANGED_JOIN_SUCCESS -> "JOIN_SUCCESS"
                Constants.CONNECTION_CHANGED_INTERRUPTED -> "INTERRUPTED"
                Constants.CONNECTION_CHANGED_BANNED_BY_SERVER -> "BANNED"
                Constants.CONNECTION_CHANGED_JOIN_FAILED -> "JOIN_FAILED"
                Constants.CONNECTION_CHANGED_LEAVE_CHANNEL -> "LEAVE_CHANNEL"
                Constants.CONNECTION_CHANGED_INVALID_APP_ID -> "INVALID_APP_ID"
                Constants.CONNECTION_CHANGED_INVALID_CHANNEL_NAME -> "INVALID_CHANNEL_NAME"
                Constants.CONNECTION_CHANGED_INVALID_TOKEN -> "INVALID_TOKEN"
                Constants.CONNECTION_CHANGED_TOKEN_EXPIRED -> "TOKEN_EXPIRED"
                Constants.CONNECTION_CHANGED_REJECTED_BY_SERVER -> "REJECTED"
                Constants.CONNECTION_CHANGED_SETTING_PROXY_SERVER -> "PROXY_SERVER"
                Constants.CONNECTION_CHANGED_RENEW_TOKEN -> "RENEW_TOKEN"
                Constants.CONNECTION_CHANGED_CLIENT_IP_ADDRESS_CHANGED -> "IP_CHANGED"
                Constants.CONNECTION_CHANGED_KEEP_ALIVE_TIMEOUT -> "KEEP_ALIVE_TIMEOUT"
                else -> "UNKNOWN($reason)"
            }
            Log.d(TAG, "🔄 Connection state: $stateStr, reason: $reasonStr")

            _connectionState.value = when (state) {
                Constants.CONNECTION_STATE_CONNECTING -> ConnectionState.CONNECTING
                Constants.CONNECTION_STATE_CONNECTED -> ConnectionState.CONNECTED
                Constants.CONNECTION_STATE_RECONNECTING -> ConnectionState.RECONNECTING
                Constants.CONNECTION_STATE_FAILED -> ConnectionState.FAILED
                else -> ConnectionState.DISCONNECTED
            }
        }

        override fun onError(err: Int) {
            val errorStr = when (err) {
                Constants.ERR_INVALID_APP_ID -> "INVALID_APP_ID"
                Constants.ERR_INVALID_CHANNEL_NAME -> "INVALID_CHANNEL_NAME"
                Constants.ERR_INVALID_TOKEN -> "INVALID_TOKEN"
                Constants.ERR_TOKEN_EXPIRED -> "TOKEN_EXPIRED"
                Constants.ERR_NO_PERMISSION -> "NO_PERMISSION"
                Constants.ERR_TIMEDOUT -> "TIMEOUT"
                Constants.ERR_REFUSED -> "REFUSED"
                Constants.ERR_NOT_INITIALIZED -> "NOT_INITIALIZED"
                Constants.ERR_NOT_READY -> "NOT_READY"
                else -> "ERROR_CODE_$err"
            }
            Log.e(TAG, "❌ Agora error: $errorStr ($err)")
            _connectionState.value = ConnectionState.FAILED
        }
    }

    fun initializeEngine(appId: String) {
        try {
            if (rtcEngine != null) {
                Log.d(TAG, "Engine already initialized, reusing existing instance")
                return
            }

            Log.d(TAG, "Creating new RTC engine with appId: ${appId.take(8)}...")
            Log.d(TAG, "Context: ${context.javaClass.simpleName}")

            try {
                Log.d(TAG, "Attempting RtcEngine.create(context, appId, eventHandler)...")
                rtcEngine = RtcEngine.create(context, appId, eventHandler)
                Log.d(TAG, "Simple create succeeded: ${rtcEngine != null}")
            } catch (e: Exception) {
                Log.e(TAG, "Simple create failed, trying with config", e)

                val config = RtcEngineConfig().apply {
                    mContext = context
                    mAppId = appId
                    mEventHandler = eventHandler
                }

                rtcEngine = RtcEngine.create(config)
                Log.d(TAG, "Config create result: ${rtcEngine != null}")
            }

            if (rtcEngine == null) {
                Log.e(TAG, "❌ Both RtcEngine.create() methods returned NULL!")
                Log.e(TAG, "This usually means:")
                Log.e(TAG, "1. Agora SDK native libraries failed to load")
                Log.e(TAG, "2. Invalid App ID")
                Log.e(TAG, "3. Device incompatibility")
                throw Exception("Failed to create RTC engine - SDK initialization failed")
            }

            Log.d(TAG, "✅ RTC engine created successfully")

            rtcEngine?.apply {

                enableAudio()
                Log.d(TAG, "Audio enabled")

                setAudioProfile(
                    Constants.AUDIO_PROFILE_DEFAULT,
                    Constants.AUDIO_SCENARIO_CHATROOM
                )
                Log.d(TAG, "Audio profile set")

                disableVideo()
                Log.d(TAG, "Video disabled")
            }

            Log.d(TAG, "✅ Agora engine fully initialized")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize Agora engine", e)
            rtcEngine = null
            throw e
        }
    }

    fun joinChannel(token: String, channelName: String, uid: Int = 0) {
        try {
            Log.d(TAG, "=== JOIN CHANNEL DEBUG ===")
            Log.d(TAG, "Channel: $channelName")
            Log.d(TAG, "UID: $uid")
            Log.d(TAG, "Token: ${token.take(20)}...")
            Log.d(TAG, "Engine initialized: ${rtcEngine != null}")
            Log.d(TAG, "Current call mode: ${_callMode.value}")

            if (rtcEngine == null) {
                Log.e(TAG, "❌ RTC Engine is NULL! Cannot join channel.")
                _connectionState.value = ConnectionState.FAILED
                return
            }

            _connectionState.value = ConnectionState.CONNECTING
            currentChannelName = channelName

            val clientRole = if (_callMode.value == CallMode.LISTENER) {
                Constants.CLIENT_ROLE_AUDIENCE
            } else {
                Constants.CLIENT_ROLE_BROADCASTER
            }

            Log.d(TAG, "Joining as: ${if (clientRole == Constants.CLIENT_ROLE_AUDIENCE) "AUDIENCE (Listener)" else "BROADCASTER (Speaker)"}")

            val options = ChannelMediaOptions().apply {
                channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                clientRoleType = clientRole
                autoSubscribeAudio = true
            }

            Log.d(TAG, "Calling rtcEngine.joinChannel()...")
            val result = rtcEngine?.joinChannel(token, channelName, uid, options)
            Log.d(TAG, "joinChannel() returned: $result")

            if (result != 0) {
                Log.e(TAG, "❌ joinChannel failed with code: $result")
                _connectionState.value = ConnectionState.FAILED
            } else {

                if (_callMode.value == CallMode.LISTENER) {
                    rtcEngine?.muteLocalAudioStream(true)
                    _isMuted.value = true
                    Log.d(TAG, "Muted audio for listener mode")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in joinChannel", e)
            _connectionState.value = ConnectionState.FAILED
            throw e
        }
    }

    fun leaveChannel() {
        try {
            rtcEngine?.leaveChannel()
            currentChannelName = null
            currentUid = 0
            _isMuted.value = false
            _isSpeakerOn.value = false
            Log.d(TAG, "Left channel")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to leave channel", e)
        }
    }

    fun toggleMute() {
        val newMuteState = !_isMuted.value
        rtcEngine?.muteLocalAudioStream(newMuteState)
        _isMuted.value = newMuteState
        Log.d(TAG, "Mute toggled: $newMuteState")
    }

    fun toggleSpeaker() {
        val newSpeakerState = !_isSpeakerOn.value
        rtcEngine?.setEnableSpeakerphone(newSpeakerState)
        _isSpeakerOn.value = newSpeakerState
        Log.d(TAG, "Speaker toggled: $newSpeakerState")
    }

    fun switchToSpeakerMode() {
        try {

            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

            rtcEngine?.muteLocalAudioStream(false)
            _callMode.value = CallMode.SPEAKER
            _isMuted.value = false
            Log.d(TAG, "Switched to SPEAKER mode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to speaker mode", e)
        }
    }

    fun switchToListenerMode() {
        try {

            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)

            rtcEngine?.muteLocalAudioStream(true)
            _callMode.value = CallMode.LISTENER
            _isMuted.value = true
            Log.d(TAG, "Switched to LISTENER mode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch to listener mode", e)
        }
    }

    fun destroy() {
        try {
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
            rtcEngine = null
            _isInCall.value = false
            _connectionState.value = ConnectionState.DISCONNECTED
            Log.d(TAG, "Agora engine destroyed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to destroy engine", e)
        }
    }

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        FAILED
    }

    enum class CallMode {
        SPEAKER,
        LISTENER
    }

    companion object {
        private const val TAG = "VoiceCallManager"

        @Volatile
        private var instance: VoiceCallManager? = null

        fun getInstance(context: Context): VoiceCallManager {
            return instance ?: synchronized(this) {
                instance ?: VoiceCallManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
