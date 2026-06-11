package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DownloadTask
import com.example.data.model.MediaItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StorageStats(
    val totalGb: Double = 128.0,
    val systemGb: Double = 14.2,
    val vaultBytes: Long = 0L,
    val otherAppsGb: Double = 43.5
) {
    val vaultGb: Double get() = vaultBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
    val freeGb: Double get() = (totalGb - systemGb - otherAppsGb - vaultGb).coerceAtLeast(0.0)
    val vaultPercentage: Float get() = (vaultGb / totalGb).toFloat()
    val freePercentage: Float get() = (freeGb / totalGb).toFloat()
    val systemPercentage: Float get() = (systemGb / totalGb).toFloat()
    val otherAppsPercentage: Float get() = (otherAppsGb / totalGb).toFloat()
}

class DownloadViewModel(private val repository: MediaRepository) : ViewModel() {

    val allDownloadTasks: StateFlow<List<DownloadTask>> = repository.allDownloadTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDownloadTasks: StateFlow<List<DownloadTask>> = repository.activeDownloadTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live calculation of media vault storage footprint based on downloaded files
    val storageStats: StateFlow<StorageStats> = repository.downloadedMediaItems
        .map { list ->
            val bytes = list.sumOf { it.fileSizeBytes }
            StorageStats(vaultBytes = bytes)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StorageStats())

    fun startDownload(mediaItem: MediaItem) {
        viewModelScope.launch {
            repository.startDownload(mediaItem)
        }
    }

    fun pauseDownload(id: String) {
        viewModelScope.launch {
            repository.pauseDownload(id)
        }
    }

    fun resumeDownload(id: String) {
        viewModelScope.launch {
            repository.resumeDownload(id)
        }
    }

    fun retryDownload(id: String) {
        viewModelScope.launch {
            repository.retryDownload(id)
        }
    }

    fun cancelDownload(id: String) {
        viewModelScope.launch {
            repository.cancelDownload(id)
        }
    }

    fun clearCompletedHistory() {
        viewModelScope.launch {
            repository.clearDownloadsHistory()
        }
    }
}
