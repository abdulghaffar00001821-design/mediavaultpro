package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.MediaDatabase
import com.example.data.model.MediaItem
import com.example.data.model.Platform
import com.example.data.repository.MediaRepository
import com.example.ui.navigation.NavGraph
import com.example.ui.navigation.Screen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.DownloadViewModel
import com.example.ui.viewmodel.MediaViewModel
import com.example.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var database: MediaDatabase
    private lateinit var repository: MediaRepository
    private lateinit var mediaViewModel: MediaViewModel
    private lateinit var downloadViewModel: DownloadViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Local Database and Repository architecture setup
        database = MediaDatabase.getDatabase(applicationContext)
        repository = MediaRepository(database.mediaItemDao(), database.downloadTaskDao())
        
        // MVVM ViewModels setup
        mediaViewModel = MediaViewModel(repository)
        downloadViewModel = DownloadViewModel(repository)
        settingsViewModel = SettingsViewModel()

        // Seed sample data for testing on first install
        lifecycleScope.launch {
            seedSampleDataIfEmpty()
        }

        setContent {
            val isDark by settingsViewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Hide bottom navigation on Splash Screen
                val showBottomBar = currentRoute != Screen.Splash.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 0.dp
                            ) {
                                val tabs = listOf(
                                    Screen.Dashboard,
                                    Screen.Downloads,
                                    Screen.History,
                                    Screen.Favorites,
                                    Screen.Settings
                                )

                                tabs.forEach { screen ->
                                    val isSelected = currentRoute == screen.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ),
                                        icon = {
                                            Icon(
                                                imageVector = when(screen) {
                                                    Screen.Dashboard -> Icons.Default.Home
                                                    Screen.Downloads -> Icons.Default.Download
                                                    Screen.History -> Icons.Default.List
                                                    Screen.Favorites -> Icons.Default.Star
                                                    else -> Icons.Default.Settings
                                                },
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = { Text(screen.title) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        mediaViewModel = mediaViewModel,
                        downloadViewModel = downloadViewModel,
                        settingsViewModel = settingsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private suspend fun seedSampleDataIfEmpty() {
        try {
            val count = database.mediaItemDao().getMediaCount()
            if (count == 0) {
                // Pre-populate with high quality mock metadata for evaluation testing
                val sampleYoutube = MediaItem(
                    id = "sample_video_1",
                    title = "Jetpack Compose Course: Crash Course",
                    description = "@channel_creator \u2022 The full architectural guide of building scalable component state, custom animators, and side-effects handling.",
                    url = "https://www.youtube.com/watch?v=youtube_item_ref_1",
                    thumbnailUrl = "https://images.unsplash.com/photo-1607799279861-4dd421887fb3?q=80&w=400",
                    duration = "14:24",
                    uploadDate = "8 Jun 2026",
                    platform = Platform.YOUTUBE.name,
                    fileSizeBytes = 45_000_000L
                )

                val sampleTiktok = MediaItem(
                    id = "sample_video_2",
                    title = "WorkManager Scheduling Patterns",
                    description = "@tiktok_influencer \u2022 Unlocking asynchronous thread pools, periodic constraint policies, and exponential retry strategies.",
                    url = "https://www.tiktok.com/@tiktok_dev_1/video/1",
                    thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80&w=400",
                    duration = "00:59",
                    uploadDate = "10 May 2026",
                    platform = Platform.TIKTOK.name,
                    fileSizeBytes = 12_500_000L,
                    isFavorite = true
                )

                val sampleInsta = MediaItem(
                    id = "sample_video_3",
                    title = "Minimal Workspace Aesthetics",
                    description = "insta_user \u2022 Crafting visual spacing, negative tracking properties, and clean Material 3 grids.",
                    url = "https://www.instagram.com/p/insta_ref_1",
                    thumbnailUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?q=80&w=400",
                    duration = "01:15",
                    uploadDate = "1 Apr 2026",
                    platform = Platform.INSTAGRAM.name,
                    fileSizeBytes = 28_000_000L,
                    isFavorite = true
                )

                database.mediaItemDao().insertMediaItem(sampleYoutube)
                database.mediaItemDao().insertMediaItem(sampleTiktok)
                database.mediaItemDao().insertMediaItem(sampleInsta)
            }
        } catch (e: Exception) {
            // Gracefully ignore or log
        }
    }
}
