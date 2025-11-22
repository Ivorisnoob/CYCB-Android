package com.cycb.chat.data.model

data class Message(
    val _id: String,
    val chatId: String,
    val senderId: MessageSender,
    val content: String,
    val messageType: String = "text",
    val createdAt: String,
    val isEdited: Boolean = false,
    val reactions: List<Reaction> = emptyList(),
    val replyTo: ReplyToMessage? = null,
    val metadata: MessageMetadata? = null,
    val systemEventType: String? = null,
    val systemEventData: SystemEventData? = null,
    val isSending: Boolean = false,
    val sendFailed: Boolean = false
)

data class SystemEventData(
    val userId: String? = null,
    val username: String? = null,
    val eventDetails: String? = null
)

data class MessageMetadata(
    val duration: Int? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null
)

data class ReplyToMessage(
    val _id: String,
    val content: String,
    val senderId: MessageSender?,
    val senderName: String? = null,
    val messageType: String? = "text"
)

data class MessageSender(
    val _id: String,
    val displayName: String,
    val username: String,
    val profilePicture: String? = null
)

data class MessagesResponse(
    val messages: List<Message>
)

data class SendMessageRequest(
    val content: String,
    val messageType: String = "text"
)

data class SendMessageResponse(
    val success: Boolean,
    val message: Message
)

data class MessagesPaginatedResponse(
    val messages: List<Message>,
    val hasMore: Boolean,
    val nextCursor: String?
)

data class Reaction(
    val userId: String,
    val emoji: String,
    val createdAt: String
)

data class ReactToMessageRequest(
    val emoji: String
)

data class ReactToMessageResponse(
    val success: Boolean,
    val action: String // "added" or "removed"
)
