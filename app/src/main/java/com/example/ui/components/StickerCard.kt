package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
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
fun StickerCard(
    sticker: GiphySticker,
    isFavorite: Boolean,
    isSaved: Boolean,
    isLoadingAction: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onSavedToggle: (Boolean) -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = CoilImageLoader.get(context)

    // Calculate aspect ratio
    val width = sticker.images.fixedHeight?.width?.toFloatOrNull() ?: 200f
    val height = sticker.images.fixedHeight?.height?.toFloatOrNull() ?: 200f
    val aspectRatio = (width / height).coerceIn(0.5f, 2.0f)

    val previewUrl = sticker.images.fixedHeight?.url 
        ?: sticker.images.fixedHeightSmall?.url 
        ?: sticker.images.original?.url 
        ?: ""

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3EDF7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
        ) {
            // Pastel gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(getPastelGradient(sticker.id))
            )

            // Live Animated Sticker Preview
            AsyncImage(
                model = previewUrl,
                contentDescription = sticker.title,
                imageLoader = imageLoader,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )

            // Floating action buttons layer
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Right Action Group (Favorite & Bookmark)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { onFavoriteToggle(!isFavorite) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.75f),
                            contentColor = if (isFavorite) Color(0xFFB3261E) else Color(0xFF49454F)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSavedToggle(!isSaved) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.75f),
                            contentColor = if (isSaved) Color(0xFF6750A4) else Color(0xFF49454F)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Offline",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom Action Bar (Download & Share)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.75f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onShare,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color(0xFF1D192B)
                        ),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDownload,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color(0xFF1D192B)
                        ),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Overlay spinner if action loading
            if (isLoadingAction) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF6750A4),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StickerCard(
    entity: StickerEntity,
    isFavorite: Boolean,
    isSaved: Boolean,
    isLoadingAction: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onSavedToggle: (Boolean) -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = CoilImageLoader.get(context)

    val aspectRatio = (entity.width.toFloat() / entity.height.toFloat()).coerceIn(0.5f, 2.0f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3EDF7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
        ) {
            // Pastel gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(getPastelGradient(entity.id))
            )

            // Local cached live sticker
            AsyncImage(
                model = entity.url,
                contentDescription = entity.title,
                imageLoader = imageLoader,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                // Top Right Action Group (Favorite & Bookmark)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { onFavoriteToggle(!isFavorite) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.75f),
                            contentColor = if (isFavorite) Color(0xFFB3261E) else Color(0xFF49454F)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSavedToggle(!isSaved) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.75f),
                            contentColor = if (isSaved) Color(0xFF6750A4) else Color(0xFF49454F)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Offline",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Bottom Action Bar (Download & Share)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.75f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onShare,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color(0xFF1D192B)
                        ),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDownload,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color(0xFF1D192B)
                        ),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (isLoadingAction) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF6750A4),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
