package com.example.data.model

import androidx.room.*

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

@Entity(tableName = "download_tasks")
data class DownloadTask(
    @PrimaryKey
    val id: String,
    val mediaItemId: String,
    val title: String,
    val url: String,
    val thumbnailUrl: String?,
    val platform: String,
    val progress: Float = 0.0f,
    val speedKbps: Long = 0L,
    val status: String = DownloadStatus.PENDING.name,
    val errorMessage: String? = null,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val addedTimestamp: Long = System.currentTimeMillis()
)
