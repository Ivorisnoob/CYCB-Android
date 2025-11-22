package com.cycb.chat.data.model

data class Note(
    val _id: String,
    val userId: UserSummary,
    val content: String,
    val createdAt: String
)

data class UserSummary(
    val _id: String,
    val username: String,
    val displayName: String?,
    val profilePicture: String?
)

data class NoteResponse(
    val success: Boolean,
    val note: Note
)

data class NotesListResponse(
    val success: Boolean,
    val notes: List<Note>
)
