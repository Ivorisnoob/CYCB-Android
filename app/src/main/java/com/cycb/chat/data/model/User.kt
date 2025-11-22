package com.cycb.chat.data.model

data class User(
    val id: String? = null,
    val _id: String? = null,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val profilePicture: String? = null,
    val bio: String? = null,
    val isOnline: Boolean = false,
    val friends: List<String> = emptyList(),
    val friendRequestsSent: List<String> = emptyList(),
    val friendRequestsReceived: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val role: String? = null
) {

    fun getUserId(): String = id?.takeIf { it.isNotEmpty() } ?: _id ?: ""

    fun isAdmin(): Boolean = role == "admin"
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val displayName: String,
    val email: String? = null
)

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val user: User
)

data class FriendsResponse(
    val friends: List<User>
)

data class SuccessResponse(
    val success: Boolean,
    val message: String? = null
)

data class ProfileResponse(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val profilePicture: String? = null,
    val bio: String? = null,
    val isOnline: Boolean = false,
    val friendsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFriend: Boolean = false,
    val isFollowing: Boolean = false,
    val createdAt: String
)

data class UpdateProfileResponse(
    val success: Boolean,
    val user: User
)

data class UserResponse(
    val success: Boolean,
    val user: ProfileResponse
)

data class UserProfileData(
    val id: String? = null,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val profilePicture: String? = null,
    val bio: String? = null,
    val isOnline: Boolean = false,
    val friendsCount: Int? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val isFriend: Boolean? = null,
    val isFollowing: Boolean? = null,
    val createdAt: String? = null
)

data class ReportUserRequest(
    val reason: String,
    val description: String? = null
)

data class FriendRequestsResponse(
    val requests: List<User>
)
