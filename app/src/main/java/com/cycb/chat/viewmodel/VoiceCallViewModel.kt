package com.cycb.chat.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.api.RetrofitClient
import com.cycb.chat.data.api.VoiceCallTokenRequest
import com.cycb.chat.data.model.AgoraToken
import com.cycb.chat.data.model.CallParticipant
import com.cycb.chat.data.model.CallStatus
import com.cycb.chat.data.model.VoiceCall
import com.cycb.chat.data.socket.SocketManager
import com.cycb.chat.utils.VoiceCallManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class VoiceCallViewModel(application: Application) : AndroidViewModel(application) {

    private val voiceCallManager = VoiceCallManager.getInstance(application)
    private val socketManager = SocketManager.getInstance()
    private val apiService = RetrofitClient.apiService

    private val _currentCall = MutableStateFlow<VoiceCall?>(null)
    val currentCall: StateFlow<VoiceCall?> = _currentCall.asStateFlow()

    private val _incomingCall = MutableStateFlow<VoiceCall?>(null)
    val incomingCall: StateFlow<VoiceCall?> = _incomingCall.asStateFlow()

    private val _callDuration = MutableStateFlow("00:00")
    val callDuration: StateFlow<String> = _callDuration.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isInPipMode = MutableStateFlow(false)
    val isInPipMode: StateFlow<Boolean> = _isInPipMode.asStateFlow()

    private var callStartTime: Long = 0
    private var durationJob: Job? = null

    val isMuted = voiceCallManager.isMuted
    val isSpeakerOn = voiceCallManager.isSpeakerOn
    val callMode = voiceCallManager.callMode
    val connectionState = voiceCallManager.connectionState
    val isInCall = voiceCallManager.isInCall

    init {
        observeSocketEvents()
    }

    private var currentUserId: String = ""
    private var currentUsername: String = ""
    private var currentOpenChatId: String? = null

    fun setCurrentUser(userId: String, username: String) {
        currentUserId = userId
        currentUsername = username
    }

    fun setCurrentOpenChat(chatId: String?) {
        currentOpenChatId = chatId
        Log.d(TAG, "Current open chat set to: $chatId")
    }

    private fun observeSocketEvents() {

        viewModelScope.launch {
            socketManager.incomingCall.collect { callData ->
                if (callData != null && _currentCall.value == null) {
                    val chatId = callData["chatId"] as? String ?: ""
                    val channelName = callData["channelName"] as? String ?: ""
                    val callerId = callData["callerId"] as? String ?: ""
                    val callerName = callData["callerName"] as? String ?: ""

                    if (chatId == currentOpenChatId) {
                        Log.d(TAG, "Auto-joining call as listener in currently open chat: $chatId")
                        autoJoinAsListener(chatId, channelName, callerId, callerName)
                    } else {
                        Log.d(TAG, "Ignoring call in chat $chatId - not currently open (current: $currentOpenChatId)")
                    }
                }
            }
        }

        viewModelScope.launch {
            socketManager.callAccepted.collect { data ->
                if (data != null) {
                    val userId = data["userId"] as? String ?: return@collect
                    val username = data["username"] as? String ?: return@collect
                    val displayName = data["displayName"] as? String
                    val profilePicture = data["profilePicture"] as? String

                    _currentCall.value?.let { call ->
                        val updatedParticipants = call.participants + CallParticipant(
                            userId = userId,
                            username = username,
                            displayName = displayName,
                            profilePicture = profilePicture
                        )
                        _currentCall.value = call.copy(participants = updatedParticipants)
                    }
                }
            }
        }

        viewModelScope.launch {
            socketManager.callEnded.collect { data ->
                if (data != null) {
                    endCall()
                }
            }
        }

        viewModelScope.launch {
            socketManager.userJoinedCall.collect { data ->
                if (data != null) {
                    val userId = data["userId"] as? String ?: return@collect
                    val username = data["username"] as? String ?: return@collect
                    val displayName = data["displayName"] as? String
                    val profilePicture = data["profilePicture"] as? String

                    _currentCall.value?.let { call ->
                        if (call.participants.none { it.userId == userId }) {

                            val isListener = if (userId == currentUserId) {
                                callMode.value == VoiceCallManager.CallMode.LISTENER
                            } else {
                                false
                            }

                            val updatedParticipants = call.participants + CallParticipant(
                                userId = userId,
                                username = username,
                                displayName = displayName,
                                profilePicture = profilePicture,
                                isListener = isListener
                            )
                            _currentCall.value = call.copy(participants = updatedParticipants)
                            Log.d(TAG, "Added participant: $username (isListener: $isListener)")
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            socketManager.userLeftCall.collect { data ->
                if (data != null) {
                    val userId = data["userId"] as? String ?: return@collect

                    _currentCall.value?.let { call ->
                        val updatedParticipants = call.participants.filter { it.userId != userId }
                        _currentCall.value = call.copy(participants = updatedParticipants)
                    }
                }
            }
        }

        viewModelScope.launch {
            socketManager.userSwitchedToSpeaker.collect { data ->
                if (data != null) {
                    val userId = data["userId"] as? String ?: return@collect

                    _currentCall.value?.let { call ->
                        val updatedParticipants = call.participants.map { participant ->
                            if (participant.userId == userId) {
                                participant.copy(isListener = false)
                            } else {
                                participant
                            }
                        }
                        _currentCall.value = call.copy(participants = updatedParticipants)
                    }
                }
            }
        }

        viewModelScope.launch {
            socketManager.userSwitchedToListener.collect { data ->
                if (data != null) {
                    val userId = data["userId"] as? String ?: return@collect

                    _currentCall.value?.let { call ->
                        val updatedParticipants = call.participants.map { participant ->
                            if (participant.userId == userId) {
                                participant.copy(isListener = true)
                            } else {
                                participant
                            }
                        }
                        _currentCall.value = call.copy(participants = updatedParticipants)
                    }
                }
            }
        }
    }

    fun initiateCall(chatId: String, chatName: String, callerId: String, callerName: String, isGroupChat: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initiating call for chat: $chatId (isGroup: $isGroupChat)")

                val context = getApplication<android.app.Application>()
                if (android.content.pm.PackageManager.PERMISSION_GRANTED !=
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.RECORD_AUDIO
                    )) {
                    Log.e(TAG, "RECORD_AUDIO permission not granted")
                    _error.value = "Microphone permission required for voice calls"
                    return@launch
                }

                val channelName = "call_${chatId}_${System.currentTimeMillis()}"

                Log.d(TAG, "Generating Agora token...")
                val tokenResponse = apiService.generateVoiceCallToken(
                    VoiceCallTokenRequest(channelName = channelName)
                )
                Log.d(TAG, "Token generated: appId=${tokenResponse.appId}, uid=${tokenResponse.uid}")

                _currentCall.value = VoiceCall(
                    chatId = chatId,
                    channelName = channelName,
                    callerId = callerId,
                    callerName = callerName,
                    participants = listOf(
                        CallParticipant(userId = callerId, username = callerName)
                    ),
                    status = CallStatus.CONNECTING,
                    startTime = System.currentTimeMillis()
                )
                Log.d(TAG, "Current call state updated: ${_currentCall.value}")

                Log.d(TAG, "Initializing Agora engine...")
                try {
                    voiceCallManager.initializeEngine(tokenResponse.appId)

                    voiceCallManager.switchToSpeakerMode()

                    Log.d(TAG, "Joining channel: $channelName as SPEAKER")
                    voiceCallManager.joinChannel(
                        token = tokenResponse.token,
                        channelName = channelName,
                        uid = tokenResponse.uid
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize/join Agora", e)
                    _error.value = "Voice call connection failed. Audio may not work."

                }

                socketManager.initiateCall(chatId, callerId, callerName, channelName)

                startDurationTimer()

                Log.d(TAG, "Call initiated successfully, isInCall=${isInCall.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initiate call", e)
                _error.value = "Failed to start call: ${e.message}"
                _currentCall.value = null
            }
        }
    }

    fun acceptCall(userId: String, username: String) {
        viewModelScope.launch {
            try {
                val incomingCall = _incomingCall.value ?: return@launch
                Log.d(TAG, "Accepting call from: ${incomingCall.callerName}")

                Log.d(TAG, "Generating Agora token...")
                val tokenResponse = apiService.generateVoiceCallToken(
                    VoiceCallTokenRequest(channelName = incomingCall.channelName)
                )
                Log.d(TAG, "Token generated: appId=${tokenResponse.appId}, uid=${tokenResponse.uid}")

                _currentCall.value = incomingCall.copy(
                    status = CallStatus.CONNECTING,
                    startTime = System.currentTimeMillis(),
                    participants = listOf(
                        CallParticipant(userId = incomingCall.callerId, username = incomingCall.callerName),
                        CallParticipant(userId = userId, username = username)
                    )
                )
                _incomingCall.value = null
                Log.d(TAG, "Current call state updated: ${_currentCall.value}")

                Log.d(TAG, "Initializing Agora engine...")
                try {
                    voiceCallManager.initializeEngine(tokenResponse.appId)

                    Log.d(TAG, "Joining channel: ${incomingCall.channelName}")
                    voiceCallManager.joinChannel(
                        token = tokenResponse.token,
                        channelName = incomingCall.channelName,
                        uid = tokenResponse.uid
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize/join Agora", e)
                    _error.value = "Voice call connection failed. Audio may not work."

                }

                socketManager.acceptCall(
                    incomingCall.chatId,
                    userId,
                    username,
                    incomingCall.channelName
                )

                startDurationTimer()

                Log.d(TAG, "Call accepted successfully, isInCall=${isInCall.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to accept call", e)
                _error.value = "Failed to join call: ${e.message}"
                _incomingCall.value = null
            }
        }
    }

    fun joinExistingCall(chatId: String, channelName: String, callerId: String, callerName: String) {

        autoJoinAsListener(chatId, channelName, callerId, callerName)
    }

    private fun autoJoinAsListener(chatId: String, channelName: String, callerId: String, callerName: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Auto-joining call as listener - chatId: $chatId, currentUser: $currentUserId ($currentUsername)")

                if (callerId == currentUserId) {
                    Log.d(TAG, "Skipping auto-join - we initiated this call")
                    return@launch
                }

                val tokenResponse = apiService.generateVoiceCallToken(
                    VoiceCallTokenRequest(channelName = channelName)
                )

                _currentCall.value = VoiceCall(
                    chatId = chatId,
                    channelName = channelName,
                    callerId = callerId,
                    callerName = callerName,
                    status = CallStatus.CONNECTING,
                    startTime = System.currentTimeMillis(),
                    participants = listOf(
                        CallParticipant(userId = callerId, username = callerName, isListener = false),
                        CallParticipant(userId = currentUserId, username = currentUsername, isListener = true)
                    )
                )

                Log.d(TAG, "Initial participants: caller=$callerName, me=$currentUsername")

                voiceCallManager.initializeEngine(tokenResponse.appId)
                voiceCallManager.switchToListenerMode()

                voiceCallManager.joinChannel(
                    token = tokenResponse.token,
                    channelName = channelName,
                    uid = tokenResponse.uid
                )

                socketManager.emit("user_joined_call", mapOf(
                    "chatId" to chatId,
                    "userId" to currentUserId,
                    "username" to currentUsername
                ))

                startDurationTimer()

                Log.d(TAG, "Auto-joined call as listener successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to auto-join call", e)
                _error.value = "Failed to join call: ${e.message}"
            }
        }
    }

    fun rejectCall(userId: String, username: String) {
        val incomingCall = _incomingCall.value ?: return

        socketManager.rejectCall(
            incomingCall.chatId,
            userId,
            username,
            incomingCall.channelName
        )

        _incomingCall.value = null
        Log.d(TAG, "Call rejected")
    }

    fun endCall() {
        val call = _currentCall.value ?: return

        Log.d(TAG, "Leaving call in chat: ${call.chatId}, participants: ${call.participants.size}")

        durationJob?.cancel()
        durationJob = null

        voiceCallManager.leaveChannel()

        val isLastPerson = call.participants.size <= 1

        if (isLastPerson) {

            Log.d(TAG, "Last person leaving - ending call for everyone")
            socketManager.endCall(
                call.chatId,
                currentUserId,
                currentUsername,
                call.channelName
            )
        } else {

            Log.d(TAG, "Leaving call - ${call.participants.size - 1} people remaining")
            socketManager.emit("user_left_call", mapOf(
                "chatId" to call.chatId,
                "userId" to currentUserId,
                "username" to currentUsername
            ))
        }

        _currentCall.value = null
        _callDuration.value = "00:00"
        callStartTime = 0

        Log.d(TAG, "Left call")
    }

    fun toggleMute() {
        voiceCallManager.toggleMute()
    }

    fun toggleSpeaker() {
        voiceCallManager.toggleSpeaker()
    }

    fun switchToSpeaker(userId: String, username: String) {
        Log.d(TAG, "switchToSpeaker called - userId: $userId, username: $username")
        voiceCallManager.switchToSpeakerMode()
        val call = _currentCall.value
        if (call == null) {
            Log.e(TAG, "Cannot switch to speaker - no active call")
            return
        }

        val updatedParticipants = call.participants.map { participant ->
            if (participant.userId == userId) {
                participant.copy(isListener = false)
            } else {
                participant
            }
        }
        _currentCall.value = call.copy(participants = updatedParticipants)

        Log.d(TAG, "Notifying socket - switching to speaker in chat: ${call.chatId}")
        socketManager.switchToSpeaker(call.chatId, userId, username)
    }

    fun switchToListener(userId: String, username: String) {
        Log.d(TAG, "switchToListener called - userId: $userId, username: $username")
        voiceCallManager.switchToListenerMode()
        val call = _currentCall.value
        if (call == null) {
            Log.e(TAG, "Cannot switch to listener - no active call")
            return
        }

        val updatedParticipants = call.participants.map { participant ->
            if (participant.userId == userId) {
                participant.copy(isListener = true)
            } else {
                participant
            }
        }
        _currentCall.value = call.copy(participants = updatedParticipants)

        Log.d(TAG, "Notifying socket - switching to listener in chat: ${call.chatId}")
        socketManager.switchToListener(call.chatId, userId, username)
    }

    private fun startDurationTimer() {
        callStartTime = System.currentTimeMillis()
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val duration = System.currentTimeMillis() - callStartTime
                _callDuration.value = formatDuration(duration)
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun clearError() {
        _error.value = null
    }

    fun setInPipMode(isInPip: Boolean) {
        _isInPipMode.value = isInPip
        Log.d(TAG, "PiP mode: $isInPip")
    }

    override fun onCleared() {
        super.onCleared()
        durationJob?.cancel()
        voiceCallManager.destroy()
    }

    companion object {
        private const val TAG = "VoiceCallViewModel"
    }
}
