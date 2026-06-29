package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GiphyResponse(
    @Json(name = "data") val data: List<GiphySticker>,
    @Json(name = "pagination") val pagination: GiphyPagination?
)

@JsonClass(generateAdapter = true)
data class GiphySticker(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "username") val username: String?,
    @Json(name = "images") val images: GiphyImages
)

@JsonClass(generateAdapter = true)
data class GiphyImages(
    @Json(name = "original") val original: GiphyImageInfo?,
    @Json(name = "fixed_height") val fixedHeight: GiphyImageInfo?,
    @Json(name = "fixed_width") val fixedWidth: GiphyImageInfo?,
    @Json(name = "fixed_height_small") val fixedHeightSmall: GiphyImageInfo?,
    @Json(name = "downsized") val downsized: GiphyImageInfo?
)

@JsonClass(generateAdapter = true)
data class GiphyImageInfo(
    @Json(name = "url") val url: String?,
    @Json(name = "width") val width: String?,
    @Json(name = "height") val height: String?,
    @Json(name = "size") val size: String?
)

@JsonClass(generateAdapter = true)
data class GiphyPagination(
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "count") val count: Int,
    @Json(name = "offset") val offset: Int
)

@JsonClass(generateAdapter = true)
data class GiphyAutocompleteResponse(
    @Json(name = "data") val data: List<GiphyTag>?
)

@JsonClass(generateAdapter = true)
data class GiphyTag(
    @Json(name = "name") val name: String?
)
