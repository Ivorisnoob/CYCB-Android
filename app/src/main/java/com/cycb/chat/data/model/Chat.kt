package com.cycb.chat.data.model

data class Chat(
    val id: String? = null,
    val _id: String? = null,
    val type: String,
    val name: String? = null,
    val avatar: String? = null,
    val otherUser: User? = null,
    val participants: List<User>? = null,
    val lastMessage: LastMessage? = null,
    val unreadCount: Int = 0,
    val updatedAt: String = "",
    val hasActiveCall: Boolean = false,
    val activeCallParticipantsCount: Int = 0,
    val activeCall: ActiveCallInfo? = null
) {
    fun getChatId(): String {
        return when {
            !id.isNullOrEmpty() -> id
            !_id.isNullOrEmpty() -> _id
            else -> ""
        }
    }
}

data class LastMessage(
    val content: String,
    val timestamp: String
)

data class ChatsResponse(
    val chats: List<Chat>
)

data class CreateChatRequest(
    val type: String,
    val participantId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val isPublic: Boolean = false,
    val memberIds: List<String>? = null,
    val members: List<String>
)

data class CreateChatResponse(
    val success: Boolean,
    val chat: Chat
)

data class ChatMembersResponse(
    val success: Boolean,
    val members: List<User>,
    val chat: ChatInfo
)

data class ChatInfo(
    val id: String,
    val type: String,
    val name: String?,
    val description: String?,
    val avatar: String?,
    val isPublic: Boolean,
    val createdBy: String?,
    val permissions: GroupPermissions? = null,
    val customization: ChatCustomization? = null
)

data class GroupPermissions(
    val whoCanSendMessages: String = "all",
    val whoCanAddMembers: String = "admins",
    val whoCanEditInfo: String = "admins"
)

data class PublicChat(
    val id: String,
    val name: String,
    val description: String?,
    val avatar: String?,
    val membersCount: Int,
    val isJoined: Boolean,
    val createdAt: String
)

data class PublicChatsResponse(
    val chats: List<PublicChat>
)

data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null,
    val avatar: String? = null
)

data class AddMemberRequest(
    val userId: String
)

data class AddMemberResponse(
    val success: Boolean,
    val message: String,
    val member: User
)

data class UpdateRoleRequest(
    val role: String
)

data class UpdateRoleResponse(
    val success: Boolean,
    val message: String,
    val role: String
)

data class UpdatePermissionsRequest(
    val permissions: GroupPermissions
)

data class UpdatePermissionsResponse(
    val success: Boolean,
    val message: String,
    val permissions: GroupPermissions
)

data class ChatBackground(
    val type: String = "color",
    val value: String = "#FFFFFF"
)

data class ChatCustomization(
    val background: ChatBackground? = null
)

data class UpdateBackgroundRequest(
    val type: String,
    val value: String
)

data class UpdateBackgroundResponse(
    val success: Boolean,
    val message: String,
    val background: ChatBackground
)
