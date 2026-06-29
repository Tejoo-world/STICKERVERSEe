package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.StickerEntity
import com.example.data.model.GiphySticker
import com.example.ui.components.StickerCard
import com.example.ui.components.StickerDetailDialog
import com.example.ui.components.StickerSkeletonGrid
import com.example.ui.viewmodel.StickerViewModel
import com.example.ui.viewmodel.TrendingCategory

enum class AppScreenTab(val label: String) {
    TRENDING("Trending"),
    SEARCH("Search"),
    COLLECTION("Saved")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: StickerViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(AppScreenTab.TRENDING) }
    
    // States for detail modal
    var selectedStickerDetail by remember { mutableStateOf<GiphySticker?>(null) }
    var selectedEntityDetail by remember { mutableStateOf<StickerEntity?>(null) }

    // Security warning flag as required by android-secret-management skill
    var showSecurityWarning by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3EDF7),
                contentColor = Color(0xFF1C1B1F)
            ) {
                AppScreenTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val icon = when (tab) {
                        AppScreenTab.TRENDING -> Icons.Default.TrendingUp
                        AppScreenTab.SEARCH -> Icons.Default.Search
                        AppScreenTab.COLLECTION -> Icons.Default.Bookmark
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = { Icon(imageVector = icon, contentDescription = tab.label) },
                        label = { Text(text = tab.label, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F),
                            indicatorColor = Color(0xFFE8DEF8)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F2FA))
                .padding(innerPadding)
        ) {
            // App branding header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Logo",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STICKERVERSE",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF1C1B1F)
                    )
                }

                Badge(
                    containerColor = Color(0xFFE8DEF8),
                    contentColor = Color(0xFF6750A4),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "GIPHY LIVE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Secure API warning banner
            if (showSecurityWarning) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF9DEDC),
                        contentColor = Color(0xFFB3261E)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Giphy Live prototype is running. API keys are packaged in this build. Please do not share this package publicly.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showSecurityWarning = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFFB3261E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Screen content switcher
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    AppScreenTab.TRENDING -> TrendingScreen(
                        viewModel = viewModel,
                        onStickerClick = { selectedStickerDetail = it }
                    )
                    AppScreenTab.SEARCH -> SearchScreen(
                        viewModel = viewModel,
                        onStickerClick = { selectedStickerDetail = it }
                    )
                    AppScreenTab.COLLECTION -> CollectionScreen(
                        viewModel = viewModel,
                        onEntityClick = { selectedEntityDetail = it }
                    )
                }
            }
        }
    }

    // Live Sticker detail modal overlay (GiphySticker)
    selectedStickerDetail?.let { sticker ->
        val isFav by viewModel.isFavorite(sticker.id).collectAsStateWithLifecycle(initialValue = false)
        val isSave by viewModel.isSaved(sticker.id).collectAsStateWithLifecycle(initialValue = false)
        val actionLoadingId by viewModel.actionLoading.collectAsStateWithLifecycle()

        StickerDetailDialog(
            sticker = sticker,
            entity = null,
            isFavorite = isFav,
            isSaved = isSave,
            isLoadingAction = actionLoadingId == sticker.id,
            onFavoriteToggle = { viewModel.toggleFavorite(sticker, it) },
            onSavedToggle = { viewModel.toggleSaved(sticker, it) },
            onDownload = { viewModel.downloadSticker(sticker) },
            onShare = { viewModel.shareSticker(sticker) },
            onDismiss = { selectedStickerDetail = null }
        )
    }

    // Local Entity detail modal overlay (StickerEntity)
    selectedEntityDetail?.let { entity ->
        val isFav by viewModel.isFavorite(entity.id).collectAsStateWithLifecycle(initialValue = false)
        val isSave by viewModel.isSaved(entity.id).collectAsStateWithLifecycle(initialValue = false)
        val actionLoadingId by viewModel.actionLoading.collectAsStateWithLifecycle()

        StickerDetailDialog(
            sticker = null,
            entity = entity,
            isFavorite = isFav,
            isSaved = isSave,
            isLoadingAction = actionLoadingId == entity.id,
            onFavoriteToggle = { viewModel.toggleFavoriteEntity(entity, it) },
            onSavedToggle = { viewModel.toggleSavedEntity(entity, it) },
            onDownload = { viewModel.downloadStickerEntity(entity) },
            onShare = { viewModel.shareStickerEntity(entity) },
            onDismiss = { selectedEntityDetail = null }
        )
    }
}

@Composable
fun TrendingScreen(
    viewModel: StickerViewModel,
    onStickerClick: (GiphySticker) -> Unit
) {
    val selectedCategory by viewModel.selectedTrendingCategory.collectAsStateWithLifecycle()
    val trendingResults by viewModel.trendingResults.collectAsStateWithLifecycle()
    val trendingLoading by viewModel.trendingLoading.collectAsStateWithLifecycle()
    val trendingError by viewModel.trendingError.collectAsStateWithLifecycle()
    val actionLoadingId by viewModel.actionLoading.collectAsStateWithLifecycle()

    val gridState = rememberLazyStaggeredGridState()

    // Listen for scroll end to load more items (Infinite scroll)
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= (totalItemsNumber - 5).coerceAtLeast(0) && totalItemsNumber > 0
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadTrending(refresh = false)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal category filters
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TrendingCategory.values()) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectTrendingCategory(category) },
                    label = { Text(text = category.displayName, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8DEF8),
                        selectedLabelColor = Color(0xFF1D192B),
                        containerColor = Color(0xFFF3EDF7),
                        labelColor = Color(0xFF49454F)
                    )
                )
            }
        }

        if (trendingError != null && trendingResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trendingError ?: "An error occurred.",
                        color = Color(0xFFB3261E),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = { viewModel.loadTrending(refresh = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4), contentColor = Color.White)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (trendingLoading && trendingResults.isEmpty()) {
            // Loading first page -> Show gorgeous skeleton grid
            StickerSkeletonGrid(modifier = Modifier.weight(1f))
        } else {
            Box(modifier = Modifier.weight(1f)) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    items(trendingResults) { sticker ->
                        val isFav by viewModel.isFavorite(sticker.id).collectAsStateWithLifecycle(initialValue = false)
                        val isSave by viewModel.isSaved(sticker.id).collectAsStateWithLifecycle(initialValue = false)

                        StickerCard(
                            sticker = sticker,
                            isFavorite = isFav,
                            isSaved = isSave,
                            isLoadingAction = actionLoadingId == sticker.id,
                            onFavoriteToggle = { viewModel.toggleFavorite(sticker, it) },
                            onSavedToggle = { viewModel.toggleSaved(sticker, it) },
                            onDownload = { viewModel.downloadSticker(sticker) },
                            onShare = { viewModel.shareSticker(sticker) },
                            onClick = { onStickerClick(sticker) }
                        )
                    }

                    // Bottom progress indicator for pagination
                    if (trendingLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF6750A4))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: StickerViewModel,
    onStickerClick: (GiphySticker) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchLoading by viewModel.searchLoading.collectAsStateWithLifecycle()
    val searchError by viewModel.searchError.collectAsStateWithLifecycle()
    val autocompleteSuggestions by viewModel.autocompleteSuggestions.collectAsStateWithLifecycle()
    val actionLoadingId by viewModel.actionLoading.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current
    val gridState = rememberLazyStaggeredGridState()

    val quickSearchTags = listOf(
        "happy", "hug", "cute cat", "love", "sad", "anime",
        "dog", "funny", "meme", "hello", "good morning"
    )

    // Listen for scroll end to trigger search pagination (Infinite scroll)
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= (totalItemsNumber - 5).coerceAtLeast(0) && totalItemsNumber > 0
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.executeSearch(refresh = false)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Input TextField
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search stickers GIPHY...", color = Color(0xFF49454F)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6750A4),
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFECE6F0),
                    unfocusedContainerColor = Color(0xFFECE6F0),
                    focusedTextColor = Color(0xFF1D192B),
                    unfocusedTextColor = Color(0xFF49454F)
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF49454F)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF49454F)
                            )
                        }
                    }
                }
            )
        }

        // Suggestions and quick search tags overlay
        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Horizontal scrolling suggestions / trending chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickSearchTags) { tag ->
                        FilterChip(
                            selected = searchQuery.equals(tag, ignoreCase = true),
                            onClick = {
                                viewModel.onSearchQueryChanged(tag)
                                keyboardController?.hide()
                                viewModel.executeSearch(refresh = true)
                            },
                            label = { Text(text = tag, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE8DEF8),
                                selectedLabelColor = Color(0xFF1D192B),
                                containerColor = Color(0xFFF3EDF7),
                                labelColor = Color(0xFF49454F)
                            )
                        )
                    }
                }

                if (searchLoading && searchResults.isEmpty()) {
                    // Show beautiful shimmer grid for initial search load
                    StickerSkeletonGrid(modifier = Modifier.weight(1f))
                } else if (searchResults.isEmpty()) {
                    // Empty search state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF6750A4),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Search Giphy Live Stickers",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Type your query or click on the tags above to explore hundreds of amazing animations instantly.",
                                fontSize = 13.sp,
                                color = Color(0xFF49454F),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Masonry Search Results
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        items(searchResults) { sticker ->
                            val isFav by viewModel.isFavorite(sticker.id).collectAsStateWithLifecycle(initialValue = false)
                            val isSave by viewModel.isSaved(sticker.id).collectAsStateWithLifecycle(initialValue = false)

                            StickerCard(
                                sticker = sticker,
                                isFavorite = isFav,
                                isSaved = isSave,
                                isLoadingAction = actionLoadingId == sticker.id,
                                onFavoriteToggle = { viewModel.toggleFavorite(sticker, it) },
                                onSavedToggle = { viewModel.toggleSaved(sticker, it) },
                                onDownload = { viewModel.downloadSticker(sticker) },
                                onShare = { viewModel.shareSticker(sticker) },
                                onClick = { onStickerClick(sticker) }
                            )
                        }

                        if (searchLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF6750A4))
                                }
                            }
                        }
                    }
                }
            }

            // Autocomplete Overlay Dialog List
            if (autocompleteSuggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.TopCenter),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECE6F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        autocompleteSuggestions.take(6).forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onSearchQueryChanged(suggestion)
                                        keyboardController?.hide()
                                        viewModel.executeSearch(refresh = true)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Suggestion",
                                    tint = Color(0xFF49454F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = suggestion, color = Color(0xFF1D192B), fontSize = 14.sp)
                            }
                            HorizontalDivider(color = Color(0xFFCAC4D0))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionScreen(
    viewModel: StickerViewModel,
    onEntityClick: (StickerEntity) -> Unit
) {
    var selectedCollectionSubTab by remember { mutableStateOf(0) } // 0 = Saved, 1 = Favorites
    val favorites by viewModel.favoriteStickers.collectAsStateWithLifecycle()
    val saved by viewModel.savedStickers.collectAsStateWithLifecycle()
    val actionLoadingId by viewModel.actionLoading.collectAsStateWithLifecycle()

    val currentList = if (selectedCollectionSubTab == 0) saved else favorites

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Segmented Controls
        TabRow(
            selectedTabIndex = selectedCollectionSubTab,
            containerColor = Color(0xFFF7F2FA),
            contentColor = Color(0xFF6750A4),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCollectionSubTab]),
                    color = Color(0xFF6750A4)
                )
            }
        ) {
            Tab(
                selected = selectedCollectionSubTab == 0,
                onClick = { selectedCollectionSubTab = 0 },
                text = {
                    Text(
                        text = "Offline Saved (${saved.size})",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCollectionSubTab == 0) Color(0xFF6750A4) else Color(0xFF49454F)
                    )
                }
            )
            Tab(
                selected = selectedCollectionSubTab == 1,
                onClick = { selectedCollectionSubTab = 1 },
                text = {
                    Text(
                        text = "Favorites (${favorites.size})",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCollectionSubTab == 1) Color(0xFF6750A4) else Color(0xFF49454F)
                    )
                }
            )
        }

        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = if (selectedCollectionSubTab == 0) Icons.Default.BookmarkBorder else Icons.Default.FavoriteBorder,
                        contentDescription = "Empty",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedCollectionSubTab == 0) "No Saved Stickers" else "No Favorite Stickers",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedCollectionSubTab == 0) {
                            "Save stickers from the live browser to keep them stored offline for instant viewing anytime!"
                        } else {
                            "Favorite stickers to curate your own personalized quick-access live sticker lists."
                        },
                        fontSize = 13.sp,
                        color = Color(0xFF49454F),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                items(currentList, key = { it.id }) { entity ->
                    val isFav = entity.isFavorite
                    val isSave = entity.isSaved

                    StickerCard(
                        entity = entity,
                        isFavorite = isFav,
                        isSaved = isSave,
                        isLoadingAction = actionLoadingId == entity.id,
                        onFavoriteToggle = { viewModel.toggleFavoriteEntity(entity, it) },
                        onSavedToggle = { viewModel.toggleSavedEntity(entity, it) },
                        onDownload = { viewModel.downloadStickerEntity(entity) },
                        onShare = { viewModel.shareStickerEntity(entity) },
                        onClick = { onEntityClick(entity) }
                    )
                }
            }
        }
    }
}
