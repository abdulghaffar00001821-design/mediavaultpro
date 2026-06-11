package com.example.data.local

import androidx.room.*
import com.example.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getMediaCount(): Int

    @Query("SELECT * FROM media_items ORDER BY addedTimestamp DESC")
    fun getAllMediaItems(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE platform = :platform ORDER BY addedTimestamp DESC")
    fun getMediaItemsByPlatform(platform: String): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1 ORDER BY addedTimestamp DESC")
    fun getFavoriteMediaItems(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isDownloaded = 1 ORDER BY addedTimestamp DESC")
    fun getDownloadedMediaItems(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaItemById(id: String): MediaItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(item: MediaItem): Long

    @Update
    suspend fun updateMediaItem(item: MediaItem)

    @Delete
    suspend fun deleteMediaItem(item: MediaItem)

    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE media_items SET isDownloaded = :isDownloaded, filePath = :filePath, fileSizeBytes = :fileSizeBytes WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean, filePath: String?, fileSizeBytes: Long)

    @Query("SELECT * FROM media_items WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY addedTimestamp DESC")
    fun searchMediaItems(query: String): Flow<List<MediaItem>>

    @Transaction
    suspend fun deleteMultiple(items: List<MediaItem>) {
        items.forEach { deleteMediaItem(it) }
    }
}
