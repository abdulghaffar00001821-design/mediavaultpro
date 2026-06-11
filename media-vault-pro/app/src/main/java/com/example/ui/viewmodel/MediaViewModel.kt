package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MediaItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MediaViewModel(private val repository: MediaRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val allMediaItems: StateFlow<List<MediaItem>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allMediaItems
            } else {
                repository.searchMediaItems(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<MediaItem>> = repository.favoriteMediaItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedItems: StateFlow<List<MediaItem>> = repository.downloadedMediaItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getItemsByPlatform(platform: String): Flow<List<MediaItem>> {
        return repository.getItemsByPlatform(platform)
    }

    fun addMediaItem(url: String, onSuccess: (MediaItem) -> Unit = {}, onError: (String) -> Unit = {}) {
        if (url.isBlank()) {
            onError("URL cannot be empty")
            return
        }
        viewModelScope.launch {
            try {
                val item = repository.parseMediaUrl(url)
                repository.insertMediaItem(item)
                onSuccess(item)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to import media URL")
            }
        }
    }

    fun deleteMediaItem(item: MediaItem) {
        viewModelScope.launch {
            repository.deleteMediaItem(item)
        }
    }

    fun deleteMultipleItems(items: List<MediaItem>) {
        viewModelScope.launch {
            repository.deleteMultipleMediaItems(items)
        }
    }

    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, !item.isFavorite)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
