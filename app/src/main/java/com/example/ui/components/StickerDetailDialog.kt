package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.StickerEntity
import com.example.data.model.GiphySticker
import kotlin.math.absoluteValue

private val pastelGradients = listOf(
    listOf(Color(0xFFD0BCFF), Color(0xFFEADDFF)),
    listOf(Color(0xFFFFD8E4), Color(0xFFF9DEDC)),
    listOf(Color(0xFFC2E7FF), Color(0xFFD3E3FD)),
    listOf(Color(0xFFE8DEF8), Color(0xFFD0BCFF))
)

private fun getPastelGradient(id: String): Brush {
    val index = id.hashCode().absoluteValue % pastelGradients.size
    val colors = pastelGradients[index]
    return Brush.linearGradient(colors)
}

@Composable
fun StickerDetailDialog(
    sticker: GiphySticker?,
    entity: StickerEntity?,
    isFavorite: Boolean,
    isSaved: Boolean,
    isLoadingAction: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onSavedToggle: (Boolean) -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val imageLoader = CoilImageLoader.get(context)

    val title = sticker?.title ?: entity?.title ?: "Sticker"
    val username = sticker?.username ?: entity?.username ?: "giphy"
    val imageUrl = sticker?.images?.original?.url ?: entity?.url ?: ""
    val id = sticker?.id ?: entity?.id ?: ""

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF7F2FA)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sticker Details",
                        color = Color(0xFF1C1B1F),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFFECE6F0),
                            contentColor = Color(0xFF1D192B)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Main Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Transparent Backdrop for the sticker with pastel gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(getPastelGradient(id))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = title,
                            imageLoader = imageLoader,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = title.ifBlank { "Untitled Sticker" },
                        color = Color(0xFF1D192B),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "by @$username",
                        color = Color(0xFF49454F),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Buttons Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Favorite
                        FilledTonalIconButton(
                            onClick = { onFavoriteToggle(!isFavorite) },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (isFavorite) Color(0xFFF9DEDC) else Color(0xFFECE6F0),
                                contentColor = if (isFavorite) Color(0xFFB3261E) else Color(0xFF49454F)
                            ),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Save Offline
                        FilledTonalIconButton(
                            onClick = { onSavedToggle(!isSaved) },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (isSaved) Color(0xFFEADDFF) else Color(0xFFECE6F0),
                                contentColor = if (isSaved) Color(0xFF6750A4) else Color(0xFF49454F)
                            ),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save Offline",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Share
                        FilledTonalIconButton(
                            onClick = onShare,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFECE6F0),
                                contentColor = Color(0xFF49454F)
                            ),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Download
                        FilledTonalIconButton(
                            onClick = onDownload,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFECE6F0),
                                contentColor = Color(0xFF49454F)
                            ),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Spinner loader overlay
                if (isLoadingAction) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF6750A4))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Processing sticker...",
                                color = Color(0xFF1D192B),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
