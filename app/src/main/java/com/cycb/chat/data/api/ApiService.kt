package com.cycb.chat.data.api

import com.cycb.chat.data.model.*
import retrofit2.http.*

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @GET("chats")
    suspend fun getChats(): ChatsResponse

    @GET("messages/chat/{chatId}")
    suspend fun getMessages(@Path("chatId") chatId: String): MessagesResponse

    @POST("messages/chat/{chatId}")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body request: SendMessageRequest
    ): com.cycb.chat.data.model.SendMessageResponse

    @GET("users/me")
    suspend fun getCurrentUser(): User

    @GET("users/friends")
    suspend fun getFriends(): FriendsResponse

    @POST("users/{userId}/friend-request")
    suspend fun sendFriendRequest(@Path("userId") userId: String): SuccessResponse

    @POST("users/friend-request/{userId}/accept")
    suspend fun acceptFriendRequest(@Path("userId") userId: String): SuccessResponse

    @POST("users/friend-request/{userId}/reject")
    suspend fun rejectFriendRequest(@Path("userId") userId: String): SuccessResponse

    @DELETE("users/{userId}/friend")
    suspend fun removeFriend(@Path("userId") userId: String): SuccessResponse

    @POST("chats")
    suspend fun createChat(@Body request: CreateChatRequest): CreateChatResponse

    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(@Part image: okhttp3.MultipartBody.Part): ImageUploadResponse

    @Multipart
    @POST("upload/profile-picture")
    suspend fun uploadProfilePicture(@Part image: okhttp3.MultipartBody.Part): ProfilePictureResponse

    @Multipart
    @POST("upload/audio")
    suspend fun uploadAudio(@Part audio: okhttp3.MultipartBody.Part): AudioUploadResponse

    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): UserResponse

    @PUT("users/me")
    suspend fun updateProfile(@Body request: Map<String, String>): UpdateProfileResponse

    @POST("users/me/password")
    suspend fun updatePassword(@Body request: Map<String, String>): SuccessResponse

    @POST("users/{userId}/block")
    suspend fun blockUser(@Path("userId") userId: String): SuccessResponse

    @POST("users/{userId}/report")
    suspend fun reportUser(
        @Path("userId") userId: String,
        @Body request: ReportUserRequest
    ): SuccessResponse

    @GET("users/friend-requests")
    suspend fun getFriendRequests(): FriendRequestsResponse

    @GET("messages/chat/{chatId}")
    suspend fun getMessagesPaginated(
        @Path("chatId") chatId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): MessagesPaginatedResponse

    @GET("chats/{chatId}/members")
    suspend fun getChatMembers(@Path("chatId") chatId: String): ChatMembersResponse

    @DELETE("chats/{chatId}")
    suspend fun leaveChat(@Path("chatId") chatId: String): SuccessResponse

    @POST("messages/{messageId}/react")
    suspend fun reactToMessage(
        @Path("messageId") messageId: String,
        @Body request: ReactToMessageRequest
    ): ReactToMessageResponse

    @POST("notifications/register-token")
    suspend fun registerFCMToken(@Body request: Map<String, String>): retrofit2.Response<SuccessResponse>

    @HTTP(method = "DELETE", path = "notifications/unregister-token", hasBody = true)
    suspend fun unregisterFCMToken(@Body request: Map<String, String>): retrofit2.Response<SuccessResponse>

    @POST("voice-call/token")
    suspend fun generateVoiceCallToken(@Body request: VoiceCallTokenRequest): AgoraToken

    @GET("voice-call/status/{channelName}")
    suspend fun getCallStatus(@Path("channelName") channelName: String): CallStatusResponse

    @GET("chats/public")
    suspend fun getPublicChats(
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null
    ): PublicChatsResponse

    @POST("chats/{chatId}/join")
    suspend fun joinPublicChat(@Path("chatId") chatId: String): SuccessResponse

    @PUT("chats/{chatId}")
    suspend fun updateGroupInfo(
        @Path("chatId") chatId: String,
        @Body request: UpdateGroupRequest
    ): SuccessResponse

    @POST("chats/{chatId}/members")
    suspend fun addGroupMember(
        @Path("chatId") chatId: String,
        @Body request: AddMemberRequest
    ): AddMemberResponse

    @DELETE("chats/{chatId}/members/{memberId}")
    suspend fun removeGroupMember(
        @Path("chatId") chatId: String,
        @Path("memberId") memberId: String
    ): SuccessResponse

    @PUT("chats/{chatId}/members/{memberId}/role")
    suspend fun updateMemberRole(
        @Path("chatId") chatId: String,
        @Path("memberId") memberId: String,
        @Body request: UpdateRoleRequest
    ): UpdateRoleResponse

    @PUT("chats/{chatId}/permissions")
    suspend fun updateGroupPermissions(
        @Path("chatId") chatId: String,
        @Body request: UpdatePermissionsRequest
    ): UpdatePermissionsResponse

    @PUT("chats/{chatId}/background")
    suspend fun updateChatBackground(
        @Path("chatId") chatId: String,
        @Body request: UpdateChatBackgroundRequest
    ): SuccessResponse

    @DELETE("messages/{messageId}")
    suspend fun deleteMessage(@Path("messageId") messageId: String): SuccessResponse

    @POST("chats/private")
    suspend fun getOrCreatePrivateChat(@Body request: Map<String, String>): CreateChatResponse

    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): UsersSearchResponse

    @POST("chats/{chatId}/read")
    suspend fun markChatAsRead(@Path("chatId") chatId: String): SuccessResponse

    @GET("chats/{chatId}")
    suspend fun getChatById(@Path("chatId") chatId: String): ChatDetailResponse

    @GET("app/version")
    suspend fun getLatestAppVersion(): AppVersionResponse

    @GET("notes")
    suspend fun getNotes(): com.cycb.chat.data.model.NotesListResponse

    @POST("notes")
    suspend fun createNote(@Body request: Map<String, String>): com.cycb.chat.data.model.NoteResponse

    @DELETE("notes")
    suspend fun deleteNote(): Map<String, Any>
}

data class VoiceCallTokenRequest(
    val channelName: String,
    val uid: Int? = null
)

data class VideoCAllTokenRequest(
    val channelName: String,
    val uid:Int? = null
)

data class CallStatusResponse(
    val channelName: String,
    val active: Boolean
)

data class UsersSearchResponse(
    val users: List<User>
)

data class ChatDetailResponse(
    val _id: String,
    val type: String,
    val name: String?,
    val members: List<ChatMember>,
    val activeCall: ActiveCallInfo?
)

data class ChatMember(
    val userId: User,
    val role: String
)

data class UpdateChatBackgroundRequest(
    val type: String,
    val value: String
)
