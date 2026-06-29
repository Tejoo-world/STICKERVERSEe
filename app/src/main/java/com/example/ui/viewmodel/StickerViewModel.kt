package com.example.ui.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.StickerDatabase
import com.example.data.local.StickerEntity
import com.example.data.model.GiphySticker
import com.example.data.network.GiphyApiClient
import com.example.data.repository.StickerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

enum class TrendingCategory(val displayName: String, val searchQuery: String?) {
    TRENDING_TODAY("Trending Today", null),
    VIRAL_REACTIONS("Viral Reactions", "viral reaction"),
    POPULAR("Popular", "popular"),
    MEME_STICKERS("Meme Stickers", "meme"),
    CUTE_STICKERS("Cute Stickers", "cute")
}

class StickerViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // Fallback to user provided key if BuildConfig placeholder is not overwritten
    private val apiKey: String = if (
        BuildConfig.GIPHY_API_KEY.isNotEmpty() && 
        BuildConfig.GIPHY_API_KEY != "MY_GIPHY_API_KEY"
    ) {
        BuildConfig.GIPHY_API_KEY
    } else {
        "0ybpE2X2SBk8mrLzgCogwS4AJuP8mNG7"
    }

    private val repository: StickerRepository

    init {
        val database = StickerDatabase.getDatabase(context)
        repository = StickerRepository(
            apiService = GiphyApiClient.service,
            stickerDao = database.stickerDao(),
            apiKey = apiKey
        )
    }

    // Local DB observation flows
    val favoriteStickers: StateFlow<List<StickerEntity>> = repository.favoriteStickers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedStickers: StateFlow<List<StickerEntity>> = repository.savedStickers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search state flows
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GiphySticker>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _searchLoading = MutableStateFlow(false)
    val searchLoading = _searchLoading.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError = _searchError.asStateFlow()

    private var searchOffset = 0
    private var hasMoreSearch = true
    private var searchJob: Job? = null

    // Trending state flows
    private val _selectedTrendingCategory = MutableStateFlow(TrendingCategory.TRENDING_TODAY)
    val selectedTrendingCategory = _selectedTrendingCategory.asStateFlow()

    private val _trendingResults = MutableStateFlow<List<GiphySticker>>(emptyList())
    val trendingResults = _trendingResults.asStateFlow()

    private val _trendingLoading = MutableStateFlow(false)
    val trendingLoading = _trendingLoading.asStateFlow()

    private val _trendingError = MutableStateFlow<String?>(null)
    val trendingError = _trendingError.asStateFlow()

    private var trendingOffset = 0
    private var hasMoreTrending = true
    private var trendingJob: Job? = null

    // Autocomplete states
    private val _autocompleteSuggestions = MutableStateFlow<List<String>>(emptyList())
    val autocompleteSuggestions = _autocompleteSuggestions.asStateFlow()

    // Download/Share action status
    private val _actionLoading = MutableStateFlow<String?>(null) // Contains GiphySticker ID being processed
    val actionLoading = _actionLoading.asStateFlow()

    init {
        // Load initial trending stickers
        loadTrending(refresh = true)
    }

    // 1. Live Search with Pagination and Deduplication
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.trim().length >= 2) {
            fetchAutocomplete(query)
        } else {
            _autocompleteSuggestions.value = emptyList()
        }
    }

    fun executeSearch(refresh: Boolean = true) {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) return

        if (refresh) {
            searchOffset = 0
            hasMoreSearch = true
            _searchResults.value = emptyList()
        }

        if (!hasMoreSearch || _searchLoading.value) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchLoading.value = true
            _searchError.value = null
            try {
                val limit = 30
                val stickers = repository.searchStickers(query, limit, searchOffset)
                if (stickers.isEmpty()) {
                    hasMoreSearch = false
                } else {
                    _searchResults.update { current ->
                        val combined = if (refresh) stickers else current + stickers
                        // Remove duplicates by ID
                        combined.distinctBy { it.id }
                    }
                    searchOffset += limit
                }
            } catch (e: Exception) {
                _searchError.value = e.localizedMessage ?: "Search failed. Please try again."
            } finally {
                _searchLoading.value = false
            }
        }
    }

    private fun fetchAutocomplete(query: String) {
        viewModelScope.launch {
            try {
                val suggestions = repository.getAutocompleteSuggestions(query)
                _autocompleteSuggestions.value = suggestions
            } catch (e: Exception) {
                // Fail silently for autocomplete suggestions
            }
        }
    }

    // 2. Trending category selection and dynamic loading
    fun selectTrendingCategory(category: TrendingCategory) {
        if (_selectedTrendingCategory.value == category) return
        _selectedTrendingCategory.value = category
        loadTrending(refresh = true)
    }

    fun loadTrending(refresh: Boolean = true) {
        if (refresh) {
            trendingOffset = 0
            hasMoreTrending = true
            _trendingResults.value = emptyList()
        }

        if (!hasMoreTrending || _trendingLoading.value) return

        trendingJob?.cancel()
        trendingJob = viewModelScope.launch {
            _trendingLoading.value = true
            _trendingError.value = null
            try {
                val limit = 30
                val category = _selectedTrendingCategory.value
                val stickers = if (category.searchQuery == null) {
                    repository.getTrendingStickers(limit, trendingOffset)
                } else {
                    repository.searchStickers(category.searchQuery, limit, trendingOffset)
                }

                if (stickers.isEmpty()) {
                    hasMoreTrending = false
                } else {
                    _trendingResults.update { current ->
                        val combined = if (refresh) stickers else current + stickers
                        combined.distinctBy { it.id }
                    }
                    trendingOffset += limit
                }
            } catch (e: Exception) {
                _trendingError.value = e.localizedMessage ?: "Failed to load stickers."
            } finally {
                _trendingLoading.value = false
            }
        }
    }

    // Local Db check helpers
    fun isFavorite(id: String): Flow<Boolean> = repository.isFavoriteFlow(id)
    fun isSaved(id: String): Flow<Boolean> = repository.isSavedFlow(id)

    // Local DB manipulation
    fun toggleFavorite(sticker: GiphySticker, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(sticker, isFav)
        }
    }

    fun toggleSaved(sticker: GiphySticker, isSave: Boolean) {
        viewModelScope.launch {
            repository.toggleSaved(sticker, isSave)
            if (isSave) {
                showToast("Sticker saved to offline collection!")
            } else {
                showToast("Sticker removed from collection.")
            }
        }
    }

    fun toggleFavoriteEntity(entity: StickerEntity, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavoriteEntity(entity, isFav)
        }
    }

    fun toggleSavedEntity(entity: StickerEntity, isSave: Boolean) {
        viewModelScope.launch {
            repository.toggleSavedEntity(entity, isSave)
        }
    }

    // 3. Download Sticker/GIF to Device's Public Downloads folder
    fun downloadSticker(sticker: GiphySticker) {
        val url = sticker.images.original?.url ?: sticker.images.fixedHeight?.url ?: return
        val filename = "${sticker.title?.replace(" ", "_") ?: "sticker"}_${sticker.id}.gif"

        viewModelScope.launch {
            _actionLoading.value = sticker.id
            val success = saveStickerToDownloads(url, filename)
            _actionLoading.value = null
            if (success) {
                showToast("Sticker successfully downloaded to Gallery/Downloads!")
            } else {
                showToast("Failed to download sticker. Please try again.")
            }
        }
    }

    fun downloadStickerEntity(entity: StickerEntity) {
        val filename = "${entity.title.replace(" ", "_")}_${entity.id}.gif"
        viewModelScope.launch {
            _actionLoading.value = entity.id
            val success = saveStickerToDownloads(entity.url, filename)
            _actionLoading.value = null
            if (success) {
                showToast("Sticker successfully downloaded to Gallery/Downloads!")
            } else {
                showToast("Failed to download sticker. Please try again.")
            }
        }
    }

    private suspend fun saveStickerToDownloads(url: String, filename: String): Boolean = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext false
            val body = response.body ?: return@withContext false

            val inputStream: InputStream = body.byteStream()
            var outputStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/gif")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri: Uri? = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = context.contentResolver.openOutputStream(uri)
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, filename)
                outputStream = FileOutputStream(file)
            }

            if (outputStream != null) {
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                return@withContext true
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 4. Share Sticker/GIF to other apps via Native Share Intent
    fun shareSticker(sticker: GiphySticker) {
        val url = sticker.images.original?.url ?: sticker.images.fixedHeight?.url ?: return
        val filename = "shared_${sticker.id}.gif"

        viewModelScope.launch {
            _actionLoading.value = sticker.id
            val uri = prepareStickerForSharing(url, filename)
            _actionLoading.value = null

            if (uri != null) {
                launchShareIntent(uri)
            } else {
                showToast("Unable to prepare sticker for sharing.")
            }
        }
    }

    fun shareStickerEntity(entity: StickerEntity) {
        val filename = "shared_${entity.id}.gif"
        viewModelScope.launch {
            _actionLoading.value = entity.id
            val uri = prepareStickerForSharing(entity.url, filename)
            _actionLoading.value = null

            if (uri != null) {
                launchShareIntent(uri)
            } else {
                showToast("Unable to prepare sticker for sharing.")
            }
        }
    }

    private suspend fun prepareStickerForSharing(url: String, filename: String): Uri? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body ?: return@withContext null

            val cachePath = File(context.cacheDir, "shared_stickers")
            if (!cachePath.exists()) cachePath.mkdirs()
            val file = File(cachePath, filename)

            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun launchShareIntent(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/gif"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Animated Sticker").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun showToast(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
