package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stickers")
data class StickerEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val width: Int,
    val height: Int,
    val username: String,
    val isFavorite: Boolean = false,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
