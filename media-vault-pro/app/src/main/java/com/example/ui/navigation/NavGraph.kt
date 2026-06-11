package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.data.model.Platform
import com.example.ui.screens.*
import com.example.ui.viewmodel.DownloadViewModel
import com.example.ui.viewmodel.MediaViewModel
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    mediaViewModel: MediaViewModel,
    downloadViewModel: DownloadViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                mediaViewModel = mediaViewModel,
                downloadViewModel = downloadViewModel,
                onNavigateToPlatform = { platform ->
                    navController.navigate("folder/${platform.name}")
                },
                onNavigateToDownloads = {
                    navController.navigate(Screen.Downloads.route)
                }
            )
        }

        // Folders individual screens
        composable("folder/{platformName}") { backStackEntry ->
            val platformName = backStackEntry.arguments?.getString("platformName") ?: Platform.OTHER.name
            val platform = Platform.valueOf(platformName)
            PlatformScreen(
                platform = platform,
                mediaViewModel = mediaViewModel,
                downloadViewModel = downloadViewModel
            )
        }

        composable(Screen.Downloads.route) {
            DownloadsScreen(downloadViewModel = downloadViewModel)
        }

        composable(Screen.History.route) {
            HistoryScreen(
                mediaViewModel = mediaViewModel,
                downloadViewModel = downloadViewModel
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                mediaViewModel = mediaViewModel,
                downloadViewModel = downloadViewModel
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(settingsViewModel = settingsViewModel)
        }
    }
}
