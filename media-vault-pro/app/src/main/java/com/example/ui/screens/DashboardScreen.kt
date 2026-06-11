package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Platform
import com.example.ui.viewmodel.DownloadViewModel
import com.example.ui.viewmodel.MediaViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    mediaViewModel: MediaViewModel,
    downloadViewModel: DownloadViewModel,
    onNavigateToPlatform: (Platform) -> Unit,
    onNavigateToDownloads: () -> Unit
) {
    val context = LocalContext.current
    var globalUrlInput by remember { mutableStateOf("") }
    val allItems by mediaViewModel.allMediaItems.collectAsState()
    val activeTasks by downloadViewModel.activeDownloadTasks.collectAsState()
    val storageStats by downloadViewModel.storageStats.collectAsState()

    val countMap = remember(allItems) {
        allItems.groupBy { it.platform }.mapValues { it.value.size }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Media Vault Pro", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToDownloads) {
                        BadgedBox(
                            badge = {
                                if (activeTasks.isNotEmpty()) {
                                    Badge { Text(activeTasks.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Active Downloads", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // General Storage Statistics Box
            Spacer(modifier = Modifier.height(16.dp))
            StorageStatisticsView(stats = storageStats)

            // Direct URL Import Bar
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = globalUrlInput,
                    onValueChange = { globalUrlInput = it },
                    placeholder = { Text("Paste any Social URL to direct grab") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (globalUrlInput.isNotBlank()) {
                            val platform = mediaViewModel.addMediaItem(
                                url = globalUrlInput,
                                onSuccess = { item ->
                                    globalUrlInput = ""
                                    Toast.makeText(context, "Parsed ${item.platform} metadata!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = "Import URL Link")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Secure Resource Vaults",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Resource Folder Grid
            val platforms = Platform.values()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(platforms) { platform ->
                    val count = countMap[platform.name] ?: 0
                    PlatformGridCard(
                        platform = platform,
                        count = count,
                        onClick = { onNavigateToPlatform(platform) }
                    )
                }
            }
        }
    }
}

@Composable
fun StorageStatisticsView(stats: com.example.ui.viewmodel.StorageStats) {
    val df = remember { DecimalFormat("0.00") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SYSTEM FOOTPRINT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${df.format(stats.vaultGb)} GB in Vault",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Free Space: ${df.format(stats.freeGb)} GB",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment progress bar representing system, other apps, vaults, and free
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(stats.vaultPercentage.coerceAtLeast(0.01f))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(stats.systemPercentage.coerceAtLeast(0.01f))
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(stats.otherAppsPercentage.coerceAtLeast(0.01f))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(stats.freePercentage.coerceAtLeast(0.01f))
                            .background(Color.Transparent)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = MaterialTheme.colorScheme.primary, text = "Vault")
                LegendItem(color = MaterialTheme.colorScheme.secondary, text = "System")
                LegendItem(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), text = "Other Apps")
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PlatformGridCard(
    platform: Platform,
    count: Int,
    onClick: () -> Unit
) {
    val platformColor = when (platform) {
        Platform.YOUTUBE -> com.example.ui.theme.AccentRed
        Platform.TIKTOK -> com.example.ui.theme.AccentCyan
        Platform.INSTAGRAM -> com.example.ui.theme.AccentPink
        Platform.FACEBOOK -> com.example.ui.theme.AccentBlue
        Platform.PINTEREST -> com.example.ui.theme.AccentOrange
        Platform.X_TWITTER -> com.example.ui.theme.AccentIndigo
        else -> com.example.ui.theme.AccentGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, platformColor.copy(alpha = 0.25f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(platformColor.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(platform) {
                            Platform.YOUTUBE -> Icons.Default.PlayArrow
                            Platform.TIKTOK -> Icons.Default.PlayArrow
                            Platform.INSTAGRAM -> Icons.Default.Star
                            Platform.FACEBOOK -> Icons.Default.PlayArrow
                            Platform.PINTEREST -> Icons.Default.Star
                            else -> Icons.Default.Settings
                        },
                        contentDescription = platform.displayName,
                        tint = platformColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Go",
                    tint = platformColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = platform.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (count == 1) "1 item saved" else "$count items saved",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
