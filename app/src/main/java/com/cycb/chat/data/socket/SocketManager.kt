package com.cycb.chat.data.socket

import android.util.Log
import com.cycb.chat.data.api.ApiConfig
import com.cycb.chat.data.model.Message
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.net.URISyntaxException

data class TypingUser(
    val userId: String,
    val username: String,
    val displayName: String,
    val profilePicture: String?
)

class SocketManager private constructor() {
    private var socket: Socket? = null
    private val gson = Gson()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _newMessage = MutableStateFlow<Message?>(null)
    val newMessage: StateFlow<Message?> = _newMessage.asStateFlow()

    private val _typingUsers = MutableStateFlow<Map<String, Set<TypingUser>>>(emptyMap())
    val typingUsers: StateFlow<Map<String, Set<TypingUser>>> = _typingUsers.asStateFlow()

    private val _incomingCall = MutableStateFlow<Map<String, Any>?>(null)
    val incomingCall: StateFlow<Map<String, Any>?> = _incomingCall.asStateFlow()

    private val _callAccepted = MutableStateFlow<Map<String, Any>?>(null)
    val callAccepted: StateFlow<Map<String, Any>?> = _callAccepted.asStateFlow()

    private val _callRejected = MutableStateFlow<Map<String, Any>?>(null)
    val callRejected: StateFlow<Map<String, Any>?> = _callRejected.asStateFlow()

    private val _callEnded = MutableStateFlow<Map<String, Any>?>(null)
    val callEnded: StateFlow<Map<String, Any>?> = _callEnded.asStateFlow()

    private val _userJoinedCall = MutableStateFlow<Map<String, Any>?>(null)
    val userJoinedCall: StateFlow<Map<String, Any>?> = _userJoinedCall.asStateFlow()

    private val _userLeftCall = MutableStateFlow<Map<String, Any>?>(null)
    val userLeftCall: StateFlow<Map<String, Any>?> = _userLeftCall.asStateFlow()

    private val _userSwitchedToSpeaker = MutableStateFlow<Map<String, Any>?>(null)
    val userSwitchedToSpeaker: StateFlow<Map<String, Any>?> = _userSwitchedToSpeaker.asStateFlow()

    private val _userSwitchedToListener = MutableStateFlow<Map<String, Any>?>(null)
    val userSwitchedToListener: StateFlow<Map<String, Any>?> = _userSwitchedToListener.asStateFlow()

    private val _messageReactionAdded = MutableStateFlow<Map<String, Any>?>(null)
    val messageReactionAdded: StateFlow<Map<String, Any>?> = _messageReactionAdded.asStateFlow()

    private val _messageReactionRemoved = MutableStateFlow<Map<String, Any>?>(null)
    val messageReactionRemoved: StateFlow<Map<String, Any>?> = _messageReactionRemoved.asStateFlow()

    private val _messageDeleted = MutableStateFlow<Map<String, Any>?>(null)
    val messageDeleted: StateFlow<Map<String, Any>?> = _messageDeleted.asStateFlow()

    private val _chatBackgroundUpdated = MutableStateFlow<Map<String, Any>?>(null)
    val chatBackgroundUpdated: StateFlow<Map<String, Any>?> = _chatBackgroundUpdated.asStateFlow()

    companion object {
        private const val TAG = "SocketManager"

        @Volatile
        private var instance: SocketManager? = null

        fun getInstance(): SocketManager {
            return instance ?: synchronized(this) {
                instance ?: SocketManager().also { instance = it }
            }
        }
    }

    fun connect(token: String) {
        if (socket?.connected() == true) {
            Log.d(TAG, "Socket already connected")
            return
        }

        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                reconnectionAttempts = Int.MAX_VALUE
            }

            socket = IO.socket(ApiConfig.SOCKET_URL, options)

            setupSocketListeners()
            socket?.connect()

            Log.d(TAG, "Connecting to ${ApiConfig.SOCKET_URL}")
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Socket connection error", e)
            _connectionState.value = ConnectionState.ERROR
        }
    }

    private fun setupSocketListeners() {
        socket?.apply {
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected")
                _connectionState.value = ConnectionState.CONNECTED
            }

            on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket disconnected")
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Socket connection error: ${args.firstOrNull()}")
                _connectionState.value = ConnectionState.ERROR
            }

            on("new_message") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val message = gson.fromJson(it.toString(), Message::class.java)
                        Log.d(TAG, "New message received: ${message._id}")
                        _newMessage.value = message
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing new message", e)
                }
            }

            on("user_typing") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val chatId = it.getString("chatId")
                        val userId = it.getString("userId")
                        val username = it.getString("username")
                        val displayName = it.optString("displayName", username)
                        val profilePictureStr = it.optString("profilePicture", "")
                        val profilePicture = if (profilePictureStr.isEmpty()) null else profilePictureStr

                        val typingUser = TypingUser(
                            userId = userId,
                            username = username,
                            displayName = displayName,
                            profilePicture = profilePicture
                        )

                        val currentTyping = _typingUsers.value.toMutableMap()
                        val chatTypers = currentTyping[chatId]?.toMutableSet() ?: mutableSetOf()

                        chatTypers.removeIf { it.userId == userId }
                        chatTypers.add(typingUser)
                        currentTyping[chatId] = chatTypers
                        _typingUsers.value = currentTyping

                        Log.d(TAG, "$displayName is typing in $chatId (${chatTypers.size} users typing)")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing typing event", e)
                }
            }

            on("user_stopped_typing") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val chatId = it.getString("chatId")
                        val userId = it.getString("userId")

                        val currentTyping = _typingUsers.value.toMutableMap()
                        val chatTypers = currentTyping[chatId]?.toMutableSet()

                        if (chatTypers != null) {

                            chatTypers.removeIf { it.userId == userId }
                            if (chatTypers.isEmpty()) {
                                currentTyping.remove(chatId)
                            } else {
                                currentTyping[chatId] = chatTypers
                            }
                            _typingUsers.value = currentTyping
                            Log.d(TAG, "User $userId stopped typing in $chatId (${chatTypers.size} users still typing)")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing stopped typing event", e)
                }
            }

            on("incoming_call") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val callData = mapOf(
                            "chatId" to it.getString("chatId"),
                            "callerId" to it.getString("callerId"),
                            "callerName" to it.getString("callerName"),
                            "channelName" to it.getString("channelName")
                        )
                        _incomingCall.value = callData
                        Log.d(TAG, "Incoming call from ${it.getString("callerName")}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing incoming call", e)
                }
            }

            on("call_accepted") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val callData = mapOf(
                            "chatId" to it.getString("chatId"),
                            "userId" to it.getString("userId"),
                            "username" to it.getString("username"),
                            "channelName" to it.getString("channelName")
                        )
                        _callAccepted.value = callData
                        Log.d(TAG, "Call accepted by ${it.getString("username")}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call accepted", e)
                }
            }

            on("call_rejected") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val callData = mapOf(
                            "chatId" to it.getString("chatId"),
                            "userId" to it.getString("userId"),
                            "username" to it.getString("username"),
                            "channelName" to it.getString("channelName")
                        )
                        _callRejected.value = callData
                        Log.d(TAG, "Call rejected by ${it.getString("username")}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call rejected", e)
                }
            }

            on("call_ended") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val callData = mapOf(
                            "chatId" to it.getString("chatId"),
                            "userId" to it.getString("userId"),
                            "username" to it.getString("username"),
                            "channelName" to it.getString("channelName")
                        )
                        _callEnded.value = callData
                        Log.d(TAG, "Call ended by ${it.getString("username")}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing call ended", e)
                }
            }

            on("user_joined_call") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val callData = mutableMapOf(
                            "userId" to it.getString("userId"),
                            "username" to it.getString("username")
                        )

                        if (it.has("displayName")) {
                            callData["displayName"] = it.getString("displayName")
                        }
                        if (it.has("profilePicture") && !it.isNull("profilePicture")) {
                            callData["profilePicture"] = it.getString("profilePicture")
                        }
                        _userJoinedCall.value = callData
                        Log.d(TAG, "${it.getString("username")} joined call")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user joined call", e)
                }
            }

            on("user_left_call") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val callData = mutableMapOf(
                            "userId" to it.getString("userId"),
                            "username" to it.getString("username")
                        )

                        if (it.has("displayName")) {
                            callData["displayName"] = it.getString("displayName")
                        }
                        if (it.has("profilePicture") && !it.isNull("profilePicture")) {
                            callData["profilePicture"] = it.getString("profilePicture")
                        }
                        _userLeftCall.value = callData
                        Log.d(TAG, "${it.getString("username")} left call")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user left call", e)
                }
            }

            on("user_switched_to_speaker") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val modeData = mapOf(
                            "userId" to it.getString("userId"),
                            "username" to it.getString("username")
                        )
                        _userSwitchedToSpeaker.value = modeData
                        Log.d(TAG, "${it.getString("username")} switched to speaker mode")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user switched to speaker", e)
                }
            }

            on("user_switched_to_listener") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val modeData = mapOf(
                            "userId" to it.getString("userId"),
                            "username" to it.getString("username")
                        )
                        _userSwitchedToListener.value = modeData
                        Log.d(TAG, "${it.getString("username")} switched to listener mode")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user switched to listener", e)
                }
            }

            on("message_reaction_added") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val reactionData = mapOf(
                            "messageId" to it.getString("messageId"),
                            "userId" to it.getString("userId"),
                            "emoji" to it.getString("emoji")
                        )
                        _messageReactionAdded.value = reactionData
                        Log.d(TAG, "Reaction added to message ${it.getString("messageId")}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message reaction added", e)
                }
            }

            on("message_reaction_removed") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val reactionData = mapOf(
                            "messageId" to it.getString("messageId"),
                            "userId" to it.getString("userId"),
                            "emoji" to it.getString("emoji")
                        )
                        _messageReactionRemoved.value = reactionData
                        Log.d(TAG, "Reaction removed from message ${it.getString("messageId")}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message reaction removed", e)
                }
            }

            on("message_deleted") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val deleteData = mapOf(
                            "messageId" to it.getString("messageId"),
                            "userId" to it.getString("userId")
                        )
                        _messageDeleted.value = deleteData
                        Log.d(TAG, "Message deleted: ${it.getString("messageId")}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message deleted", e)
                }
            }

            on("chat_background_updated") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val chatId = it.getString("chatId")
                        val backgroundObj = it.getJSONObject("background")

                        val backgroundData = mapOf(
                            "chatId" to chatId,
                            "type" to backgroundObj.getString("type"),
                            "value" to backgroundObj.getString("value"),
                            "updatedBy" to it.getString("updatedBy")
                        )
                        _chatBackgroundUpdated.value = backgroundData
                        Log.d(TAG, "Chat background updated for $chatId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing chat background updated", e)
                }
            }
        }
    }

    fun joinChat(chatId: String) {
        val data = JSONObject().apply {
            put("chatId", chatId)
        }
        socket?.emit("join_chat", data)
        Log.d(TAG, "Joined chat: $chatId")
    }

    fun leaveChat(chatId: String) {
        val data = JSONObject().apply {
            put("chatId", chatId)
        }
        socket?.emit("leave_chat", data)
        Log.d(TAG, "Left chat: $chatId")
    }

    fun sendTyping(chatId: String) {
        val data = JSONObject().apply {
            put("chatId", chatId)
        }
        socket?.emit("typing_start", data)
    }

    fun sendStopTyping(chatId: String) {
        val data = JSONObject().apply {
            put("chatId", chatId)
        }
        socket?.emit("typing_stop", data)
    }

    fun sendMessage(chatId: String, content: String, messageType: String = "text", replyTo: String? = null) {
        val data = JSONObject().apply {
            put("chatId", chatId)
            put("content", content)
            put("messageType", messageType)
            replyTo?.let { put("replyTo", it) }
        }
        socket?.emit("send_message", data)
        Log.d(TAG, "Sent message to chat: $chatId")
    }

    fun sendVoiceMessage(chatId: String, audioUrl: String, duration: Int, replyTo: String? = null) {
        val metadata = JSONObject().apply {
            put("duration", duration)
        }

        val data = JSONObject().apply {
            put("chatId", chatId)
            put("content", audioUrl)
            put("messageType", "voice")
            put("metadata", metadata)
            replyTo?.let { put("replyTo", it) }
        }
        socket?.emit("send_message", data)
        Log.d(TAG, "Sent voice message to chat: $chatId, duration: ${duration}ms")
    }

    fun sendImageMessage(chatId: String, imageUrl: String, replyTo: String? = null) {
        val data = JSONObject().apply {
            put("chatId", chatId)
            put("content", imageUrl)
            put("messageType", "image")
            replyTo?.let { put("replyTo", it) }
        }
        socket?.emit("send_message", data)
        Log.d(TAG, "Sent image message to chat: $chatId")
    }

    fun sendGifMessage(chatId: String, gifUrl: String, replyTo: String? = null) {
        val data = JSONObject().apply {
            put("chatId", chatId)
            put("content", gifUrl)
            put("messageType", "gif")
            replyTo?.let { put("replyTo", it) }
        }
        socket?.emit("send_message", data)
        Log.d(TAG, "Sent GIF message to chat: $chatId")
    }

    fun initiateCall(chatId: String, callerId: String, callerName: String, channelName: String) {
        val data = JSONObject().apply {
            put("chatId", chatId)
            put("callerId", callerId)
            put("callerName", callerName)
            put("channelName", channelName)
        }
        socket?.emit("call_initiated", data)
        Log.d(TAG, "Call initiated in chat: $chatId")
    }

    fun acceptCall(chatId: String, userId: String, username: String, channelName: String) {
        val data = JSONObject().apply {
            put("chatId", chatId)
            put("userId", userId)
            put("username", username)
            put("channelName", channelName)
        }
        socket?.emit("call_accepted", data)
        Log.d(TAG, "Call accepted in chat: $chatId")
    }

    fun rejectCall(chatId: String, userId: String, username: String, channelName: String) {
        val data = JSONObject().apply {
            put("chatId", chatId)
            put("userId", userId)
            put("username", username)
            put("channelName", channelName)
        }
        socket?.emit("call_rejected", data)
        Log.d(TAG, "Call rejected in chat: $chatId")
    }

    fun endCall(chatId: String, userId: String, username: String, channelName: String) {
        val data = JSONObject().apply {
            put("chatId", chatId)
            put("userId", userId)
            put("username", username)
            put("channelName", channelName)
        }
        socket?.emit("call_ended", data)
        Log.d(TAG, "Call ended in chat: $chatId")
    }

    fun switchToSpeaker(chatId: String, userId: String, username: String) {
        val data = JSONObject().apply {
            put("chatId", chatId)
            put("userId", userId)
            put("username", username)
        }
        socket?.emit("switch_to_speaker", data)
        Log.d(TAG, "Switched to speaker mode in chat: $chatId")
    }

    fun switchToListener(chatId: String, userId: String, username: String) {
        val data = JSONObject().apply {
            put("chatId", chatId)
            put("userId", userId)
            put("username", username)
        }
        socket?.emit("switch_to_listener", data)
        Log.d(TAG, "Switched to listener mode in chat: $chatId")
    }

    fun reactToMessage(messageId: String, emoji: String) {
        val data = JSONObject().apply {
            put("messageId", messageId)
            put("emoji", emoji)
        }
        socket?.emit("react_to_message", data)
        Log.d(TAG, "Reacted to message $messageId with $emoji")
    }

    fun deleteMessage(messageId: String) {
        val data = JSONObject().apply {
            put("messageId", messageId)
        }
        socket?.emit("delete_message", data)
        Log.d(TAG, "Deleting message: $messageId")
    }

    fun emit(event: String, data: Map<String, Any>) {
        val jsonData = JSONObject(data)
        socket?.emit(event, jsonData)
        Log.d(TAG, "Emitted event: $event with data: $data")
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        _connectionState.value = ConnectionState.DISCONNECTED
        Log.d(TAG, "Socket disconnected and cleaned up")
    }

    fun isConnected(): Boolean = socket?.connected() == true
}

enum class ConnectionState {
    CONNECTED,
    DISCONNECTED,
    ERROR
}
