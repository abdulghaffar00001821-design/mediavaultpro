package com.example.data.local

import androidx.room.*
import com.example.data.model.DownloadTask
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadTaskDao {

    @Query("SELECT * FROM download_tasks ORDER BY addedTimestamp DESC")
    fun getAllTasks(): Flow<List<DownloadTask>>

    @Query("SELECT * FROM download_tasks WHERE status IN ('PENDING', 'DOWNLOADING') ORDER BY addedTimestamp ASC")
    fun getActiveTasks(): Flow<List<DownloadTask>>

    @Query("SELECT * FROM download_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): DownloadTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DownloadTask): Long

    @Update
    suspend fun updateTask(task: DownloadTask)

    @Delete
    suspend fun deleteTask(task: DownloadTask)

    @Query("UPDATE download_tasks SET progress = :progress, speedKbps = :speedKbps, downloadedBytes = :downloadedBytes, status = :status WHERE id = :id")
    suspend fun updateTaskProgress(id: String, progress: Float, speedKbps: Long, downloadedBytes: Long, status: String)

    @Query("UPDATE download_tasks SET status = :status, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String, errorMessage: String?)

    @Query("DELETE FROM download_tasks")
    suspend fun clearAll()

    @Query("DELETE FROM download_tasks WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()
}
