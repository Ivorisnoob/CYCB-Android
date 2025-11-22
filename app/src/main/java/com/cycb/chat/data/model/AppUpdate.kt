package com.cycb.chat.data.model

data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val body: String,
    val published_at: String,
    val assets: List<GitHubAsset>
)

data class GitHubAsset(
    val name: String,
    val size: Long,
    val browser_download_url: String
)

data class AppUpdateInfo(
    val version: String,
    val versionCode: Int,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileSize: Long,
    val publishedDate: String,
    val releaseName: String? = null,
    val isUpdateAvailable: Boolean = false
)

data class AppVersionResponse(
    val success: Boolean,
    val version: String,
    val versionCode: Int,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileSize: Long,
    val publishedDate: String,
    val releaseName: String? = null
)
