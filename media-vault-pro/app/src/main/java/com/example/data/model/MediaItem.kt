package com.example.data.model

import androidx.room.*

enum class Platform(val displayName: String, val iconName: String) {
    YOUTUBE("YouTube", "youtube"),
    TIKTOK("TikTok", "tiktok"),
    INSTAGRAM("Instagram", "instagram"),
    FACEBOOK("Facebook", "facebook"),
    PINTEREST("Pinterest", "pinterest"),
    X_TWITTER("X (Twitter)", "twitter"),
    OTHER("Other", "other")
}

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val thumbnailUrl: String?,
    val duration: String?,
    val uploadDate: String?,
    val platform: String, // String representation of Platform enum
    val addedTimestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val filePath: String? = null,
    val fileSizeBytes: Long = 0L
)
