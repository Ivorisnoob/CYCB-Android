package com.cycb.chat.data.model

data class ImageUploadResponse(
    val success: Boolean,
    val url: String,
    val message: String? = null
)

data class ProfilePictureResponse(
    val success: Boolean,
    val url: String,
    val user: User
)

data class AudioUploadResponse(
    val success: Boolean,
    val url: String,
    val message: String? = null,
    val duration: Int? = null
)
