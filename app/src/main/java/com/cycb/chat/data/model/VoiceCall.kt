package com.cycb.chat.data.model

data class VoiceCall(
    val chatId: String,
    val channelName: String,
    val callerId: String,
    val callerName: String,
    val participants: List<CallParticipant> = emptyList(),
    val status: CallStatus = CallStatus.IDLE,
    val startTime: Long? = null
)

data class CallParticipant(
    val userId: String,
    val username: String,
    val displayName: String? = null,
    val profilePicture: String? = null,
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val isListener: Boolean = false // true = listener mode, false = speaker mode
)

enum class CallStatus {
    IDLE,
    RINGING,
    CONNECTING,
    CONNECTED,
    ENDED
}

data class AgoraToken(
    val token: String,
    val appId: String,
    val channelName: String,
    val uid: Int,
    val expiresAt: Long
)
