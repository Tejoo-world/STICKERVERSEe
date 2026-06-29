package com.example.data.repository

import com.example.data.local.StickerDao
import com.example.data.local.StickerEntity
import com.example.data.model.GiphySticker
import com.example.data.network.GiphyApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class StickerRepository(
    private val apiService: GiphyApiService,
    private val stickerDao: StickerDao,
    private val apiKey: String
) {
    // Local DB flows
    val favoriteStickers: Flow<List<StickerEntity>> = stickerDao.getFavoriteStickers()
    val savedStickers: Flow<List<StickerEntity>> = stickerDao.getSavedStickers()

    // Database actions
    suspend fun toggleFavorite(sticker: GiphySticker, isFav: Boolean) {
        val id = sticker.id
        val existing = stickerDao.getStickerById(id)
        if (isFav) {
            val url = sticker.images.fixedHeight?.url 
                ?: sticker.images.original?.url 
                ?: ""
            val width = sticker.images.fixedHeight?.width?.toIntOrNull() ?: 200
            val height = sticker.images.fixedHeight?.height?.toIntOrNull() ?: 200
            
            val entity = StickerEntity(
                id = id,
                title = sticker.title ?: "Sticker",
                url = url,
                width = width,
                height = height,
                username = sticker.username ?: "giphy",
                isFavorite = true,
                isSaved = existing?.isSaved ?: false
            )
            stickerDao.insertSticker(entity)
        } else {
            if (existing != null) {
                if (existing.isSaved) {
                    stickerDao.insertSticker(existing.copy(isFavorite = false))
                } else {
                    stickerDao.deleteStickerById(id)
                }
            }
        }
    }

    suspend fun toggleSaved(sticker: GiphySticker, isSave: Boolean) {
        val id = sticker.id
        val existing = stickerDao.getStickerById(id)
        if (isSave) {
            val url = sticker.images.fixedHeight?.url 
                ?: sticker.images.original?.url 
                ?: ""
            val width = sticker.images.fixedHeight?.width?.toIntOrNull() ?: 200
            val height = sticker.images.fixedHeight?.height?.toIntOrNull() ?: 200

            val entity = StickerEntity(
                id = id,
                title = sticker.title ?: "Sticker",
                url = url,
                width = width,
                height = height,
                username = sticker.username ?: "giphy",
                isFavorite = existing?.isFavorite ?: false,
                isSaved = true
            )
            stickerDao.insertSticker(entity)
        } else {
            if (existing != null) {
                if (existing.isFavorite) {
                    stickerDao.insertSticker(existing.copy(isSaved = false))
                } else {
                    stickerDao.deleteStickerById(id)
                }
            }
        }
    }

    suspend fun toggleFavoriteEntity(entity: StickerEntity, isFav: Boolean) {
        val existing = stickerDao.getStickerById(entity.id)
        if (isFav) {
            val updated = entity.copy(isFavorite = true, isSaved = existing?.isSaved ?: entity.isSaved)
            stickerDao.insertSticker(updated)
        } else {
            if (existing != null) {
                if (existing.isSaved) {
                    stickerDao.insertSticker(existing.copy(isFavorite = false))
                } else {
                    stickerDao.deleteStickerById(entity.id)
                }
            }
        }
    }

    suspend fun toggleSavedEntity(entity: StickerEntity, isSave: Boolean) {
        val existing = stickerDao.getStickerById(entity.id)
        if (isSave) {
            val updated = entity.copy(isSaved = true, isFavorite = existing?.isFavorite ?: entity.isFavorite)
            stickerDao.insertSticker(updated)
        } else {
            if (existing != null) {
                if (existing.isFavorite) {
                    stickerDao.insertSticker(existing.copy(isSaved = false))
                } else {
                    stickerDao.deleteStickerById(entity.id)
                }
            }
        }
    }

    fun isFavoriteFlow(id: String): Flow<Boolean> = stickerDao.isFavoriteFlow(id)
    fun isSavedFlow(id: String): Flow<Boolean> = stickerDao.isSavedFlow(id)

    // Network search with support for smart query expansion and merge
    suspend fun searchStickers(query: String, limit: Int, offset: Int): List<GiphySticker> = coroutineScope {
        val expandedQueries = QueryExpansion.expandQuery(query)
        if (expandedQueries.size <= 1) {
            // Standard single query
            try {
                val response = apiService.searchStickers(
                    apiKey = apiKey,
                    query = query,
                    limit = limit,
                    offset = offset
                )
                return@coroutineScope response.data
            } catch (e: Exception) {
                e.printStackTrace()
                return@coroutineScope emptyList()
            }
        }

        // Expanded multi-queries: fetch in parallel and merge
        // We divide limit and offset among the sub-queries
        val subLimit = (limit / expandedQueries.size).coerceAtLeast(12)
        val subOffset = (offset / expandedQueries.size)

        val deferredResults = expandedQueries.map { subQuery ->
            async {
                try {
                    apiService.searchStickers(
                        apiKey = apiKey,
                        query = subQuery,
                        limit = subLimit,
                        offset = subOffset
                    ).data
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            }
        }

        val results = deferredResults.flatMap { it.await() }

        // Remove duplicate items by id
        val seenIds = mutableSetOf<String>()
        val uniqueResults = mutableListOf<GiphySticker>()
        for (sticker in results) {
            if (sticker.id !in seenIds) {
                seenIds.add(sticker.id)
                uniqueResults.add(sticker)
            }
        }

        // Shuffle/mix elements slightly so expanded terms feel completely integrated and combined
        uniqueResults.shuffled()
    }

    // Network trending stickers
    suspend fun getTrendingStickers(limit: Int, offset: Int): List<GiphySticker> {
        return try {
            apiService.getTrendingStickers(
                apiKey = apiKey,
                limit = limit,
                offset = offset
            ).data
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Network autocomplete suggestions
    suspend fun getAutocompleteSuggestions(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        return try {
            val response = apiService.getAutocompleteTags(
                apiKey = apiKey,
                query = query
            )
            response.data?.mapNotNull { it.name } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
