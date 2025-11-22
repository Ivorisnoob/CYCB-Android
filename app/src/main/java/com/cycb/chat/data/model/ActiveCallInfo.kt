package com.cycb.chat.data.model

data class ActiveCallInfo(
    val channelName: String,
    val startedBy: User,
    val startedAt: String,
    val participants: List<CallParticipantInfo>
)

data class CallParticipantInfo(
    val userId: User,
    val joinedAt: String
)
