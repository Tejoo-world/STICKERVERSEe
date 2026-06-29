package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StickerDao {
    @Query("SELECT * FROM stickers WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteStickers(): Flow<List<StickerEntity>>

    @Query("SELECT * FROM stickers WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedStickers(): Flow<List<StickerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSticker(sticker: StickerEntity)

    @Query("DELETE FROM stickers WHERE id = :id")
    suspend fun deleteStickerById(id: String)

    @Query("SELECT * FROM stickers WHERE id = :id LIMIT 1")
    suspend fun getStickerById(id: String): StickerEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM stickers WHERE id = :id AND isFavorite = 1)")
    fun isFavoriteFlow(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM stickers WHERE id = :id AND isSaved = 1)")
    fun isSavedFlow(id: String): Flow<Boolean>
}
