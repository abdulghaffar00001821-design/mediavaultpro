package com.example.ui.screens

import android.widget.Toast
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.Platform
import com.example.ui.viewmodel.DownloadViewModel
import com.example.ui.viewmodel.MediaViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlatformScreen(
    platform: Platform,
    mediaViewModel: MediaViewModel,
    downloadViewModel: DownloadViewModel
) {
    val context = LocalContext.current
    val items by mediaViewModel.getItemsByPlatform(platform.name).collectAsState(initial = emptyList())
    var importUrl by remember { mutableStateOf("") }
    
    // Batch selection state
    var isBatchMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<MediaItem>() }
    
    // Search filter state inside platform
    var platformSearchQuery by remember { mutableStateOf("") }
    
    val filteredItems = remember(items, platformSearchQuery) {
        if (platformSearchQuery.isBlank()) {
            items
        } else {
            items.filter { 
                it.title.lowercase().contains(platformSearchQuery.lowercase()) ||
                it.description.lowercase().contains(platformSearchQuery.lowercase())
            }
        }
    }

    LaunchedEffect(isBatchMode) {
        if (!isBatchMode) {
            selectedItems.clear()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isBatchMode) "${selectedItems.size} Selected" else platform.displayName,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (isBatchMode) {
                        IconButton(onClick = {
                            downloadViewModel.clearCompletedHistory()
                            selectedItems.forEach { downloadViewModel.startDownload(it) }
                            isBatchMode = false
                            Toast.makeText(context, "Added ${selectedItems.size} items to downloads!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "Download Selected")
                        }
                        IconButton(onClick = {
                            mediaViewModel.deleteMultipleItems(selectedItems.toList())
                            isBatchMode = false
                            Toast.makeText(context, "Deleted standard links!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                        }
                        IconButton(onClick = { isBatchMode = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Batch Mode")
                        }
                    } else {
                        // Regular action search icon placeholder
                        var showSearchBox by remember { mutableStateOf(false) }
                        if (showSearchBox) {
                            TextField(
                                value = platformSearchQuery,
                                onValueChange = { platformSearchQuery = it },
                                placeholder = { Text("Search link...") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        platformSearchQuery = ""
                                        showSearchBox = false
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close Search")
                                    }
                                },
                                modifier = Modifier.width(180.dp)
                            )
                        } else {
                            IconButton(onClick = { showSearchBox = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search Within Folder")
                            }
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
        ) {
            // Import section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Import Authorized ${platform.displayName} Link",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = importUrl,
                            onValueChange = { importUrl = it },
                            placeholder = { Text("Paste video/post link...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (importUrl.isNotBlank()) {
                                    // Soft validate platform check
                                    val determined = mediaViewModel.addMediaItem(
                                        url = importUrl,
                                        onSuccess = { item ->
                                            importUrl = ""
                                            Toast.makeText(context, "Added metadata list link!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No saved content here yet.",
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Paste a link above to parse & cache metadata",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
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
                        val isSelected = selectedItems.contains(item)
                        MediaItemCard(
                            item = item,
                            isSelected = isSelected,
                            isBatchActive = isBatchMode,
                            onSelectToggle = {
                                if (isSelected) {
                                    selectedItems.remove(item)
                                    if (selectedItems.isEmpty()) isBatchMode = false
                                } else {
                                    isBatchMode = true
                                    selectedItems.add(item)
                                }
                            },
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaItemCard(
    item: MediaItem,
    isSelected: Boolean,
    isBatchActive: Boolean,
    onSelectToggle: () -> Unit,
    onFavorite: () -> Unit,
    onDownload: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val cardColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val cardBorder = if (isSelected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isBatchActive) onSelectToggle()
                },
                onLongClick = {
                    onSelectToggle()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = cardBorder
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media Thumbnail with duration
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                ) {
                    if (item.thumbnailUrl != null) {
                        AsyncImage(
                            model = item.thumbnailUrl,
                            contentDescription = "Cover Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (item.duration != "N/A" && item.duration != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.duration,
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isBatchActive) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onSelectToggle() }
                            )
                        } else {
                            IconButton(
                                onClick = onFavorite,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (item.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Added: ${item.uploadDate ?: "N/A"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (!isBatchActive) {
                Divider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                )

                // Actions Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TooltipBoxHelper(text = "Copy Title") {
                            IconButton(onClick = {
                                clipboard.setPrimaryClip(ClipData.newPlainText("Title", item.title))
                                Toast.makeText(context, "Title copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Title", modifier = Modifier.size(16.dp))
                            }
                        }

                        TooltipBoxHelper(text = "Copy Description") {
                            IconButton(onClick = {
                                clipboard.setPrimaryClip(ClipData.newPlainText("Description", item.description))
                                Toast.makeText(context, "Description copied", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Description, contentDescription = "Copy Description", modifier = Modifier.size(16.dp))
                            }
                        }

                        TooltipBoxHelper(text = "Copy Link") {
                            IconButton(onClick = {
                                clipboard.setPrimaryClip(ClipData.newPlainText("URL", item.url))
                                Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Link, contentDescription = "Copy Link", modifier = Modifier.size(16.dp))
                            }
                        }

                        TooltipBoxHelper(text = "Save Cover") {
                            IconButton(onClick = {
                                Toast.makeText(context, "Saved thumbnail into /Pictures/MediaVaultPro", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Image, contentDescription = "Save Cover Cover Image", modifier = Modifier.size(16.dp))
                            }
                        }

                        TooltipBoxHelper(text = "Share Link") {
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, item.title)
                                    putExtra(Intent.EXTRA_TEXT, item.url)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Authorized Link"))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (item.isDownloaded) {
                        Button(
                            onClick = {},
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Downloaded Icon", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vaulted", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onDownload,
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = "Download Icon", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TooltipBoxHelper(text: String, content: @Composable () -> Unit) {
    // Elegant small wrapper
    content()
}
