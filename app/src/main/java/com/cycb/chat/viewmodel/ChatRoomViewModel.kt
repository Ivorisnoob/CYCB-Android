package com.cycb.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.data.api.RetrofitClient
import com.cycb.chat.data.model.Message
import com.cycb.chat.data.model.MessageSender
import com.cycb.chat.data.model.MessageMetadata
import com.cycb.chat.data.model.ReplyToMessage
import com.cycb.chat.data.socket.SocketManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ChatRoomUiState {
    object Loading : ChatRoomUiState()
    data class Success(val messages: List<Message>) : ChatRoomUiState()
    data class Error(val message: String) : ChatRoomUiState()
}

sealed class SendMessageState {
    object Idle : SendMessageState()
    object Sending : SendMessageState()
    object Success : SendMessageState()
    data class Error(val message: String) : SendMessageState()
}

class ChatRoomViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ChatRoomUiState>(ChatRoomUiState.Loading)
    val uiState: StateFlow<ChatRoomUiState> = _uiState.asStateFlow()

    private val _sendMessageState = MutableStateFlow<SendMessageState>(SendMessageState.Idle)
    val sendMessageState: StateFlow<SendMessageState> = _sendMessageState.asStateFlow()

    private val _typingUsers = MutableStateFlow<Set<com.cycb.chat.data.socket.TypingUser>>(emptySet())
    val typingUsers: StateFlow<Set<com.cycb.chat.data.socket.TypingUser>> = _typingUsers.asStateFlow()

    private val _replyToMessage = MutableStateFlow<Message?>(null)
    val replyToMessage: StateFlow<Message?> = _replyToMessage.asStateFlow()

    private var currentChatId: String? = null
    private var typingJob: Job? = null
    private val socketManager = SocketManager.getInstance()

    init {

        viewModelScope.launch {
            socketManager.newMessage.collect { message ->
                message?.let {

                    if (it.chatId == currentChatId) {
                        val currentState = _uiState.value
                        if (currentState is ChatRoomUiState.Success) {

                            val messagesWithoutOptimistic = currentState.messages.filter { msg ->
                                !msg._id.startsWith("temp_") || msg.senderId._id != it.senderId._id
                            }

                            if (!messagesWithoutOptimistic.any { msg -> msg._id == it._id }) {
                                _uiState.value = ChatRoomUiState.Success(messagesWithoutOptimistic + it)
                            }
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            socketManager.typingUsers.collect { typingMap ->
                currentChatId?.let { chatId ->
                    _typingUsers.value = typingMap[chatId] ?: emptySet()
                }
            }
        }

        viewModelScope.launch {
            socketManager.messageReactionAdded.collect { reactionData ->
                reactionData?.let {
                    val messageId = it["messageId"] as? String ?: return@let
                    val userId = it["userId"] as? String ?: return@let
                    val emoji = it["emoji"] as? String ?: return@let

                    val currentState = _uiState.value
                    if (currentState is ChatRoomUiState.Success) {
                        val updatedMessages = currentState.messages.map { message ->
                            if (message._id == messageId) {
                                val newReaction = com.cycb.chat.data.model.Reaction(
                                    userId = userId,
                                    emoji = emoji,
                                    createdAt = System.currentTimeMillis().toString()
                                )
                                message.copy(reactions = message.reactions + newReaction)
                            } else {
                                message
                            }
                        }
                        _uiState.value = ChatRoomUiState.Success(updatedMessages)
                    }
                }
            }
        }

        viewModelScope.launch {
            socketManager.messageReactionRemoved.collect { reactionData ->
                reactionData?.let {
                    val messageId = it["messageId"] as? String ?: return@let
                    val userId = it["userId"] as? String ?: return@let
                    val emoji = it["emoji"] as? String ?: return@let

                    val currentState = _uiState.value
                    if (currentState is ChatRoomUiState.Success) {
                        val updatedMessages = currentState.messages.map { message ->
                            if (message._id == messageId) {
                                val filteredReactions = message.reactions.filter {
                                    !(it.userId == userId && it.emoji == emoji)
                                }
                                message.copy(reactions = filteredReactions)
                            } else {
                                message
                            }
                        }
                        _uiState.value = ChatRoomUiState.Success(updatedMessages)
                    }
                }
            }
        }

        viewModelScope.launch {
            socketManager.messageDeleted.collect { deleteData ->
                deleteData?.let {
                    val messageId = it["messageId"] as? String ?: return@let

                    val currentState = _uiState.value
                    if (currentState is ChatRoomUiState.Success) {
                        val updatedMessages = currentState.messages.filter { msg -> msg._id != messageId }
                        _uiState.value = ChatRoomUiState.Success(updatedMessages)
                    }
                }
            }
        }

        viewModelScope.launch {
            socketManager.chatBackgroundUpdated.collect { bgData ->
                bgData?.let {
                    val chatId = it["chatId"] as? String ?: return@let

                    if (chatId == currentChatId) {
                        val type = it["type"] as? String ?: "color"
                        val value = it["value"] as? String ?: "#FFFFFF"

                        _chatBackground.value = com.cycb.chat.data.model.ChatBackground(
                            type = type,
                            value = value
                        )
                        android.util.Log.d("ChatRoomVM", "Received background update via socket: $type, $value")
                    }
                }
            }
        }
    }

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private var hasMoreMessages = true
    private var nextCursor: String? = null

    private val _chatMembers = MutableStateFlow<List<com.cycb.chat.data.model.User>>(emptyList())
    val chatMembers: StateFlow<List<com.cycb.chat.data.model.User>> = _chatMembers.asStateFlow()

    private val _chatBackground = MutableStateFlow<com.cycb.chat.data.model.ChatBackground?>(null)
    val chatBackground: StateFlow<com.cycb.chat.data.model.ChatBackground?> = _chatBackground.asStateFlow()

    fun loadMessages(chatId: String) {
        currentChatId = chatId

        socketManager.joinChat(chatId)

        _typingUsers.value = emptySet()

        viewModelScope.launch {
            _uiState.value = ChatRoomUiState.Loading
            try {
                val response = RetrofitClient.apiService.getMessagesPaginated(chatId, limit = 50)
                hasMoreMessages = response.hasMore
                nextCursor = response.nextCursor
                _uiState.value = ChatRoomUiState.Success(response.messages)

                loadChatMembers(chatId)

                checkAndAutoJoinCall(chatId)
            } catch (e: Exception) {
                _uiState.value = ChatRoomUiState.Error(e.message ?: "Failed to load messages")
            }
        }
    }

    private fun checkAndAutoJoinCall(chatId: String) {
        viewModelScope.launch {
            try {
                val chatDetail = RetrofitClient.apiService.getChatById(chatId)
                val activeCall = chatDetail.activeCall

                val hasValidCall = activeCall != null &&
                    !activeCall.channelName.isNullOrEmpty() &&
                    activeCall.participants.isNotEmpty() &&
                    chatDetail.type == "group"

                if (hasValidCall) {
                    android.util.Log.d("ChatRoomVM", "Active call detected: channelName=${activeCall?.channelName}, participants=${activeCall?.participants?.size}")
                    _hasActiveCall.value = activeCall
                } else {
                    android.util.Log.d("ChatRoomVM", "No active call: channelName=${activeCall?.channelName}, participants=${activeCall?.participants?.size}, type=${chatDetail.type}")
                    _hasActiveCall.value = null
                }
            } catch (e: Exception) {

                android.util.Log.e("ChatRoomVM", "Failed to check active call", e)
                _hasActiveCall.value = null
            }
        }
    }

    private val _hasActiveCall = MutableStateFlow<com.cycb.chat.data.model.ActiveCallInfo?>(null)
    val hasActiveCall: StateFlow<com.cycb.chat.data.model.ActiveCallInfo?> = _hasActiveCall.asStateFlow()

    private fun loadChatMembers(chatId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getChatMembers(chatId)
                if (response.success) {
                    _chatMembers.value = response.members
                    val bg = response.chat.customization?.background
                    android.util.Log.d("ChatRoomVM", "Loaded chat members. Background: $bg")
                    _chatBackground.value = bg
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatRoomVM", "Failed to load chat members", e)

            }
        }
    }

    fun loadMoreMessages() {
        if (!hasMoreMessages || currentChatId == null) return

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getMessagesPaginated(
                    currentChatId!!,
                    limit = 50,
                    before = nextCursor
                )

                hasMoreMessages = response.hasMore
                nextCursor = response.nextCursor

                val currentState = _uiState.value
                if (currentState is ChatRoomUiState.Success) {

                    _uiState.value = ChatRoomUiState.Success(response.messages + currentState.messages)
                }
            } catch (e: Exception) {

            }
        }
    }

    fun leaveChat() {
        currentChatId?.let {

            socketManager.sendStopTyping(it)
        }
        currentChatId = null
        typingJob?.cancel()
    }

    fun updateMessageText(text: String) {
        _messageText.value = text

        currentChatId?.let { chatId ->
            if (text.isNotEmpty()) {
                socketManager.sendTyping(chatId)

                typingJob?.cancel()

                typingJob =
                        viewModelScope.launch {
                            delay(3000)
                            socketManager.sendStopTyping(chatId)
                        }
            } else {
                socketManager.sendStopTyping(chatId)
                typingJob?.cancel()
            }
        }
    }

    fun sendMessage() {
        val chatId = currentChatId ?: return
        val content = _messageText.value.trim()
        if (content.isEmpty()) return

        val replyToId = _replyToMessage.value?._id

        val tempId = "temp_${System.currentTimeMillis()}"
        val optimisticMessage = Message(
            _id = tempId,
            chatId = chatId,
            senderId = MessageSender(
                _id = RetrofitClient.getCurrentUserId() ?: "",
                displayName = "You",
                username = "",
                profilePicture = null
            ),
            content = content,
            messageType = "text",
            createdAt = System.currentTimeMillis().toString(),
            isSending = true,
            replyTo = _replyToMessage.value?.let { msg ->
                ReplyToMessage(msg._id, msg.content, msg.senderId, msg.senderId.displayName, msg.messageType)
            }
        )

        val currentState = _uiState.value
        android.util.Log.d("ChatRoomVM", "sendMessage - Current state: ${currentState::class.simpleName}")
        if (currentState is ChatRoomUiState.Success) {
            android.util.Log.d("ChatRoomVM", "Adding optimistic TEXT message: $tempId, isSending: ${optimisticMessage.isSending}, messages count: ${currentState.messages.size}")
            _uiState.value = ChatRoomUiState.Success(currentState.messages + optimisticMessage)
        }

        socketManager.sendMessage(chatId, content, replyTo = replyToId)

        _messageText.value = ""
        _replyToMessage.value = null
        _sendMessageState.value = SendMessageState.Success

        viewModelScope.launch {
            delay(10000)
            val state = _uiState.value
            if (state is ChatRoomUiState.Success) {
                val messages = state.messages.filter { it._id != tempId }
                _uiState.value = ChatRoomUiState.Success(messages)
            }
        }
    }

    fun sendVoiceMessage(audioUrl: String, duration: Int) {
        val chatId = currentChatId ?: return

        val replyToId = _replyToMessage.value?._id

        val tempId = "temp_${System.currentTimeMillis()}"
        val optimisticMessage = Message(
            _id = tempId,
            chatId = chatId,
            senderId = MessageSender(
                _id = RetrofitClient.getCurrentUserId() ?: "",
                displayName = "You",
                username = "",
                profilePicture = null
            ),
            content = audioUrl,
            messageType = "voice",
            createdAt = System.currentTimeMillis().toString(),
            isSending = true,
            metadata = MessageMetadata(duration = duration),
            replyTo = _replyToMessage.value?.let { msg ->
                ReplyToMessage(msg._id, msg.content, msg.senderId, msg.senderId.displayName, msg.messageType)
            }
        )

        val currentState = _uiState.value
        if (currentState is ChatRoomUiState.Success) {
            _uiState.value = ChatRoomUiState.Success(currentState.messages + optimisticMessage)
        }

        socketManager.sendVoiceMessage(chatId, audioUrl, duration, replyTo = replyToId)

        _replyToMessage.value = null
        _sendMessageState.value = SendMessageState.Success

        viewModelScope.launch {
            delay(10000)
            val state = _uiState.value
            if (state is ChatRoomUiState.Success) {
                val messages = state.messages.filter { it._id != tempId }
                _uiState.value = ChatRoomUiState.Success(messages)
            }
        }
    }

    fun sendImageMessage(imageUrl: String) {
        val chatId = currentChatId ?: return

        val replyToId = _replyToMessage.value?._id

        val tempId = "temp_${System.currentTimeMillis()}"
        val optimisticMessage = Message(
            _id = tempId,
            chatId = chatId,
            senderId = MessageSender(
                _id = RetrofitClient.getCurrentUserId() ?: "",
                displayName = "You",
                username = "",
                profilePicture = null
            ),
            content = imageUrl,
            messageType = "image",
            createdAt = System.currentTimeMillis().toString(),
            isSending = true,
            replyTo = _replyToMessage.value?.let { msg ->
                ReplyToMessage(msg._id, msg.content, msg.senderId, msg.senderId.displayName, msg.messageType)
            }
        )

        val currentState = _uiState.value
        android.util.Log.d("ChatRoomVM", "sendImageMessage - Current state: ${currentState::class.simpleName}")
        if (currentState is ChatRoomUiState.Success) {
            android.util.Log.d("ChatRoomVM", "Adding optimistic IMAGE message: $tempId, isSending: ${optimisticMessage.isSending}, messages count: ${currentState.messages.size}")
            _uiState.value = ChatRoomUiState.Success(currentState.messages + optimisticMessage)
        }

        socketManager.sendImageMessage(chatId, imageUrl, replyTo = replyToId)

        _replyToMessage.value = null
        _sendMessageState.value = SendMessageState.Success

        viewModelScope.launch {
            delay(10000)
            val state = _uiState.value
            if (state is ChatRoomUiState.Success) {
                val messages = state.messages.filter { it._id != tempId }
                _uiState.value = ChatRoomUiState.Success(messages)
            }
        }
    }

    fun sendGifMessage(gifUrl: String) {
        val chatId = currentChatId ?: return

        val replyToId = _replyToMessage.value?._id

        val tempId = "temp_${System.currentTimeMillis()}"
        val optimisticMessage = Message(
            _id = tempId,
            chatId = chatId,
            senderId = MessageSender(
                _id = RetrofitClient.getCurrentUserId() ?: "",
                displayName = "You",
                username = "",
                profilePicture = null
            ),
            content = gifUrl,
            messageType = "gif",
            createdAt = System.currentTimeMillis().toString(),
            isSending = true,
            replyTo = _replyToMessage.value?.let { msg ->
                ReplyToMessage(msg._id, msg.content, msg.senderId, msg.senderId.displayName, msg.messageType)
            }
        )

        val currentState = _uiState.value
        if (currentState is ChatRoomUiState.Success) {
            _uiState.value = ChatRoomUiState.Success(currentState.messages + optimisticMessage)
        }

        socketManager.sendGifMessage(chatId, gifUrl, replyTo = replyToId)

        _replyToMessage.value = null
        _sendMessageState.value = SendMessageState.Success

        viewModelScope.launch {
            delay(10000)
            val state = _uiState.value
            if (state is ChatRoomUiState.Success) {
                val messages = state.messages.filter { it._id != tempId }
                _uiState.value = ChatRoomUiState.Success(messages)
            }
        }
    }

    fun resetSendMessageState() {
        _sendMessageState.value = SendMessageState.Idle
    }

    fun reactToMessage(messageId: String, emoji: String) {
        viewModelScope.launch {
            try {

                socketManager.reactToMessage(messageId, emoji)
            } catch (e: Exception) {
                android.util.Log.e("ChatRoomVM", "React to message error", e)
            }
        }
    }

    fun setReplyToMessage(message: Message?) {
        _replyToMessage.value = message
    }

    fun clearReply() {
        _replyToMessage.value = null
    }

    fun deleteMessage(messageId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {

                socketManager.deleteMessage(messageId)

                RetrofitClient.apiService.deleteMessage(messageId)

                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("ChatRoomVM", "Delete message error", e)
                onError(e.message ?: "Failed to delete message")
            }
        }
    }

    fun updateChatBackground(chatId: String, type: String, value: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ChatRoomVM", "Updating chat background: chatId=$chatId, type=$type, value=$value")

                _chatBackground.value = com.cycb.chat.data.model.ChatBackground(
                    type = type,
                    value = value
                )

                RetrofitClient.apiService.updateChatBackground(
                    chatId = chatId,
                    request = com.cycb.chat.data.api.UpdateChatBackgroundRequest(
                        type = type,
                        value = value
                    )
                )

                android.util.Log.d("ChatRoomVM", "Chat background updated successfully")
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("ChatRoomVM", "Failed to update chat background. Code: ${e.code()}, Error: $errorBody", e)
            } catch (e: Exception) {
                android.util.Log.e("ChatRoomVM", "Failed to update chat background", e)
            }
        }
    }
}
