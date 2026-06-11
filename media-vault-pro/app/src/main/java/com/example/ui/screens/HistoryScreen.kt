package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.data.model.Platform
import com.example.ui.viewmodel.DownloadViewModel
import com.example.ui.viewmodel.MediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    mediaViewModel: MediaViewModel,
    downloadViewModel: DownloadViewModel
) {
    val context = LocalContext.current
    val allItems by mediaViewModel.allMediaItems.collectAsState()
    val searchQuery by mediaViewModel.searchQuery.collectAsState()

    var selectedPlatformFilter by remember { mutableStateOf<String?> (null) }

    val filteredItems = remember(allItems, selectedPlatformFilter, searchQuery) {
        allItems.filter { item ->
            val matchPlatform = if (selectedPlatformFilter == null) true else item.platform == selectedPlatformFilter
            matchPlatform
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Master Logs", fontWeight = FontWeight.Bold) },
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
        ) {
            // Search Input Block
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { mediaViewModel.setSearchQuery(it) },
                placeholder = { Text("Search title, creator, descriptions...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") }
            )

            // Horizontal scrolling platform chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedPlatformFilter == null,
                    onClick = { selectedPlatformFilter = null },
                    label = { Text("All Vaults") }
                )

                Platform.values().forEach { platform ->
                    FilterChip(
                        selected = selectedPlatformFilter == platform.name,
                        onClick = { selectedPlatformFilter = platform.name },
                        label = { Text(platform.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Empty History",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No history log found",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Try clearing search filter or import links",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        MediaItemCard(
                            item = item,
                            isSelected = false,
                            isBatchActive = false,
                            onSelectToggle = {},
                            onFavorite = { mediaViewModel.toggleFavorite(item) },
                            onDownload = {
                                downloadViewModel.startDownload(item)
                                Toast.makeText(context, "Queued download sequence", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}
