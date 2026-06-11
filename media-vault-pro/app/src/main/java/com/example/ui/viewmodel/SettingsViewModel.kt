package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val _isDarkMode = MutableStateFlow(true) // Default to dark mode for premium look
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _language = MutableStateFlow("English")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _storageLocation = MutableStateFlow("/storage/emulated/0/MediaVaultPro")
    val storageLocation: StateFlow<String> = _storageLocation.asStateFlow()

    private val _autoCleanupDays = MutableStateFlow(0) // Default: never
    val autoCleanupDays: StateFlow<Int> = _autoCleanupDays.asStateFlow()

    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setTheme(dark: Boolean) {
        _isDarkMode.value = dark
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun setStorageLocation(path: String) {
        _storageLocation.value = path
    }

    fun setAutoCleanupDays(days: Int) {
        _autoCleanupDays.value = days
    }

    fun performBackup() {
        _backupStatus.value = "Creating backup archive..."
        // Simulate backup
        viewModelScope.launch {
            delay(1500)
            _backupStatus.value = "Backup successfully saved to MediaVaultPro_Backup.json"
        }
    }

    fun performRestore() {
        _backupStatus.value = "Scanning for backup files..."
        viewModelScope.launch {
            delay(1500)
            _backupStatus.value = "Database restored successfully!"
        }
    }

    fun dismissBackupStatus() {
        _backupStatus.value = null
    }
}
