package com.example.data.repository

import android.content.Context
import com.example.data.local.DownloadTaskDao
import com.example.data.local.MediaItemDao
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTask
import com.example.data.model.MediaItem
import com.example.data.model.Platform
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.random.Random

class MediaRepository(
    private val mediaItemDao: MediaItemDao,
    private val downloadTaskDao: DownloadTaskDao
) {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val activeJobMap = java.util.concurrent.ConcurrentHashMap<String, Job>()

    // Exposed Flows from DAO
    val allMediaItems: Flow<List<MediaItem>> = mediaItemDao.getAllMediaItems()
    val favoriteMediaItems: Flow<List<MediaItem>> = mediaItemDao.getFavoriteMediaItems()
    val downloadedMediaItems: Flow<List<MediaItem>> = mediaItemDao.getDownloadedMediaItems()
    val allDownloadTasks: Flow<List<DownloadTask>> = downloadTaskDao.getAllTasks()
    val activeDownloadTasks: Flow<List<DownloadTask>> = downloadTaskDao.getActiveTasks()

    fun getItemsByPlatform(platform: String): Flow<List<MediaItem>> {
        return mediaItemDao.getMediaItemsByPlatform(platform)
    }

    fun searchMediaItems(query: String): Flow<List<MediaItem>> {
        return mediaItemDao.searchMediaItems(query)
    }

    suspend fun insertMediaItem(item: MediaItem) {
        mediaItemDao.insertMediaItem(item)
    }

    suspend fun deleteMediaItem(item: MediaItem) {
        // Cancel active downloads if any
        cancelDownload(item.id)
        mediaItemDao.deleteMediaItem(item)
    }

    suspend fun deleteMultipleMediaItems(items: List<MediaItem>) {
        items.forEach { cancelDownload(it.id) }
        mediaItemDao.deleteMultiple(items)
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        mediaItemDao.updateFavoriteStatus(id, isFavorite)
    }

    // Parse URL and auto-fill metadata based on platform
    fun parseMediaUrl(url: String): MediaItem {
        val canonicalUrl = url.trim()
        val platform = determinePlatform(canonicalUrl)
        val id = UUID.randomUUID().toString()
        val r = Random(System.currentTimeMillis())

        val title: String
        val description: String
        val duration: String
        val thumbnailUrl: String
        val author: String = when (platform) {
            Platform.YOUTUBE -> "@channel_creator"
            Platform.TIKTOK -> "@tiktok_influencer"
            Platform.INSTAGRAM -> "insta_user"
            Platform.FACEBOOK -> "FB Public Creator"
            Platform.PINTEREST -> "PinsArt"
            Platform.X_TWITTER -> "@x_handle"
            else -> "Web Publisher"
        }

        when (platform) {
            Platform.YOUTUBE -> {
                title = "Learn Jetpack Compose in 10 Minutes - Full Guide"
                description = "Learn how to build modern Android UIs using Jetpack Compose, Material 3, and state management. In this video, we cover Scaffold, Row, Column, Modifier, and beautiful typography. Subscribing helps make more guides!"
                duration = "10:15"
                thumbnailUrl = "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?q=80&w=400"
            }
            Platform.TIKTOK -> {
                title = "Amazing life hack for productivity in 2026!"
                description = "You won't believe how easy it is to schedule tasks using Jetpack WorkManager! #coding #android #lifehack #dev"
                duration = "00:45"
                thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=400"
            }
            Platform.INSTAGRAM -> {
                title = "Exploring the beauty of minimalist design & workspace"
                description = "Focus on the visual quality and precision of execution. Simple off-white backgrounds & deep charcoal grays make this UI stand out. #ux #inspiration #minimal"
                duration = "01:20"
                thumbnailUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?q=80&w=400"
            }
            Platform.FACEBOOK -> {
                title = "Media Vault Pro Release - Best Companion Vault Utility"
                description = "Complying with copyright policies, Media Vault Pro allows users to import and keep metadata lists from YouTube, TikTok, Insta, Facebook and more. Built with love using Jetpack Compose."
                duration = "05:00"
                thumbnailUrl = "https://images.unsplash.com/photo-1531297484001-80022131f5a1?q=80&w=400"
            }
            Platform.PINTEREST -> {
                title = "Aesthetics & Architecture Ideas Board"
                description = "Clean layouts, perfect padding, and intentional margin offsets. A collection of great material elements for Material Design 3 lovers."
                duration = "N/A"
                thumbnailUrl = "https://images.unsplash.com/photo-1506157786151-b8491531f063?q=80&w=400"
            }
            Platform.X_TWITTER -> {
                title = "Important Technical Thread on Threading and Concurrency"
                description = "Coroutines vs RxJava in Android 13+. Why supervisor scopes are crucial to prevent crashes during asynchronous downloads when database triggers write operations."
                duration = "N/A"
                thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=400"
            }
            else -> {
                title = "Imported Web Media Resource"
                description = "Successfully imported metadata from external authorized link: $url"
                duration = "N/A"
                thumbnailUrl = "https://images.unsplash.com/photo-1488590528505-98d2b5aba04b?q=80&w=400"
            }
        }

        val years = listOf("2024", "2025", "2026")
        val months = listOf("Jan", "Mar", "Apr", "Jun", "Sep", "Oct")
        val day = r.nextInt(1, 28)
        val mockDate = "${day} ${months.random()} ${years.random()}"

        // Estimate typical video size (e.g. 5 MB to 85 MB)
        val mockSize = r.nextLong(5_000_000, 95_000_000)

        return MediaItem(
            id = id,
            title = title,
            description = "$author \u2022 $description",
            url = canonicalUrl,
            thumbnailUrl = thumbnailUrl,
            duration = duration,
            uploadDate = mockDate,
            platform = platform.name,
            fileSizeBytes = mockSize
        )
    }

    fun determinePlatform(url: String): Platform {
        val lower = url.lowercase()
        return when {
            lower.contains("youtube.com") || lower.contains("youtu.be") -> Platform.YOUTUBE
            lower.contains("tiktok.com") -> Platform.TIKTOK
            lower.contains("instagram.com") -> Platform.INSTAGRAM
            lower.contains("facebook.com") || lower.contains("fb.watch") -> Platform.FACEBOOK
            lower.contains("pinterest.com") || lower.contains("pin.it") -> Platform.PINTEREST
            lower.contains("twitter.com") || lower.contains("x.com") -> Platform.X_TWITTER
            else -> Platform.OTHER
        }
    }

    // --- DOWNLOAD MANAGER IMPLEMENTATION ---

    suspend fun startDownload(mediaItem: MediaItem) {
        // First insert or update the task in DB as Downloading
        val taskId = mediaItem.id // We use mediaItem string ID as task ID
        val existingTask = downloadTaskDao.getTaskById(taskId)

        val totalBytes = mediaItem.fileSizeBytes
        val initialDownloaded = existingTask?.downloadedBytes ?: 0L
        val initialProgress = if (totalBytes > 0) initialDownloaded.toFloat() / totalBytes else 0.0f

        val task = DownloadTask(
            id = taskId,
            mediaItemId = mediaItem.id,
            title = mediaItem.title,
            url = mediaItem.url,
            thumbnailUrl = mediaItem.thumbnailUrl,
            platform = mediaItem.platform,
            progress = initialProgress,
            status = DownloadStatus.DOWNLOADING.name,
            totalBytes = totalBytes,
            downloadedBytes = initialDownloaded
        )

        downloadTaskDao.insertTask(task)

        // Launch simulated downloader
        activeJobMap[taskId]?.cancel() // Cancel any ongoing job for this item
        val job = repositoryScope.launch {
            simulateDownloadProgress(task)
        }
        activeJobMap[taskId] = job
    }

    private suspend fun simulateDownloadProgress(task: DownloadTask) {
        var downloaded = task.downloadedBytes
        val total = task.totalBytes
        val taskId = task.id
        val r = Random(taskId.hashCode())

        try {
            while (downloaded < total) {
                delay(300) // Sleep 300ms between database progress ticks

                // Ensure job wasn't cancelled or status hasn't changed from DB
                val currentTask = downloadTaskDao.getTaskById(taskId)
                if (currentTask == null || currentTask.status != DownloadStatus.DOWNLOADING.name) {
                    break
                }

                // Random chunk transfer size (e.g. 500 KB to 2.5 MB)
                val chunk = r.nextLong(500_000, 2_500_000)
                downloaded += chunk
                if (downloaded > total) downloaded = total

                val progress = downloaded.toFloat() / total
                // Calculate fluctuating download speed (e.g. 4500 KB/s to 9800 KB/s)
                val speed = r.nextLong(3200, 15000)

                downloadTaskDao.updateTaskProgress(
                    id = taskId,
                    progress = progress,
                    speedKbps = speed,
                    downloadedBytes = downloaded,
                    status = DownloadStatus.DOWNLOADING.name
                )
            }

            // Completed!
            if (downloaded >= total) {
                downloadTaskDao.updateTaskStatus(taskId, DownloadStatus.COMPLETED.name, null)
                // Mark MediaItem as downloaded and attach path
                val mockPath = "/storage/emulated/0/MediaVaultPro/downloads/${task.platform.lowercase()}_${taskId.take(6)}.mp4"
                mediaItemDao.updateDownloadStatus(task.mediaItemId, true, mockPath, total)
            }
        } catch (e: CancellationException) {
            // Task cancelled (paused)
            downloadTaskDao.updateTaskStatus(taskId, DownloadStatus.PAUSED.name, null)
        } catch (e: Exception) {
            downloadTaskDao.updateTaskStatus(taskId, DownloadStatus.FAILED.name, e.message ?: "Unknown download error")
        } finally {
            activeJobMap.remove(taskId)
        }
    }

    suspend fun pauseDownload(id: String) {
        activeJobMap[id]?.cancel()
        downloadTaskDao.updateTaskStatus(id, DownloadStatus.PAUSED.name, null)
    }

    suspend fun resumeDownload(id: String) {
        val task = downloadTaskDao.getTaskById(id) ?: return
        val item = mediaItemDao.getMediaItemById(task.mediaItemId) ?: return
        startDownload(item)
    }

    suspend fun retryDownload(id: String) {
        resumeDownload(id)
    }

    suspend fun cancelDownload(id: String) {
        activeJobMap[id]?.cancel()
        downloadTaskDao.deleteTask(DownloadTask(
            id = id, mediaItemId = "", title = "", url = "", thumbnailUrl = null, platform = ""
        ))
    }

    suspend fun clearDownloadsHistory() {
        downloadTaskDao.clearCompleted()
    }
}
