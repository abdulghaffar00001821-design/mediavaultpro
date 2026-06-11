package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : Screen("splash", "Welcome", Icons.Default.Home)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    
    // Media Platforms (Using highly safe core icons)
    object YouTube : Screen("youtube", "YouTube", Icons.Default.PlayArrow)
    object TikTok : Screen("tiktok", "TikTok", Icons.Default.Menu)
    object Instagram : Screen("instagram", "Instagram", Icons.Default.Star)
    object Facebook : Screen("facebook", "Facebook", Icons.Default.PlayArrow)
    object Pinterest : Screen("pinterest", "Pinterest", Icons.Default.Star)
    object XTwitter : Screen("xtwitter", "X (Twitter)", Icons.Default.Settings)
    
    // Core Utility Screens
    object Downloads : Screen("downloads", "Downloads", Icons.Default.PlayArrow)
    object History : Screen("history", "History", Icons.Default.List)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Star)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
