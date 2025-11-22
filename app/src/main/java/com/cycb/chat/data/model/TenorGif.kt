package com.cycb.chat.data.model

data class TenorSearchResponse(
    val results: List<TenorGif>,
    val next: String?
)

data class TenorGif(
    val id: String,
    val title: String?,
    val media_formats: Map<String, TenorMediaFormat>,
    val created: Double,
    val content_description: String?,
    val itemurl: String,
    val url: String,
    val tags: List<String>?,
    val flags: List<String>?,
    val hasaudio: Boolean?
)

data class TenorMediaFormat(
    val url: String,
    val duration: Double?,
    val preview: String?,
    val dims: List<Int>?,
    val size: Int?
)

data class TenorCategoriesResponse(
    val tags: List<TenorCategory>
)

data class TenorCategory(
    val searchterm: String,
    val path: String,
    val image: String,
    val name: String
)
