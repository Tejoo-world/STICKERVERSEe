package com.example.data.repository

object QueryExpansion {
    fun expandQuery(query: String): List<String> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return emptyList()

        // 1. Direct matching specified in user prompt
        if (trimmed == "cute cat" || trimmed == "cat" || trimmed == "cats" || trimmed.contains("cute cat")) {
            return listOf(
                "cute cat",
                "kawaii cat",
                "chibi cat",
                "funny cat",
                "cat reaction",
                "cat meme",
                "cute kitten"
            )
        }

        if (trimmed == "hug" || trimmed == "hugs" || trimmed.contains("hug")) {
            return listOf(
                "hug",
                "cute hug",
                "love hug",
                "anime hug",
                "cat hug",
                "reaction hug"
            )
        }

        // 2. Additional dynamic expansions for common sticker search queries
        if (trimmed.contains("dog") || trimmed == "puppy") {
            return listOf(
                trimmed,
                "cute dog",
                "funny dog",
                "kawaii dog",
                "dog reaction",
                "puppy"
            )
        }

        if (trimmed.contains("happy") || trimmed == "joy") {
            return listOf(
                trimmed,
                "happy sticker",
                "excited",
                "cute happy",
                "happy dance"
            )
        }

        if (trimmed.contains("love") || trimmed == "heart") {
            return listOf(
                trimmed,
                "cute love",
                "heart sticker",
                "love you",
                "chibi love"
            )
        }

        if (trimmed.contains("sad") || trimmed == "cry") {
            return listOf(
                trimmed,
                "cute sad",
                "crying sticker",
                "sad anime",
                "sad face"
            )
        }

        if (trimmed.contains("anime") || trimmed == "chibi") {
            return listOf(
                trimmed,
                "cute anime",
                "anime sticker",
                "chibi sticker",
                "kawaii anime"
            )
        }

        if (trimmed.contains("meme") || trimmed == "funny") {
            return listOf(
                trimmed,
                "funny meme",
                "cat meme",
                "reaction meme",
                "funny sticker"
            )
        }

        if (trimmed.contains("hello") || trimmed.contains("hi") || trimmed.contains("morning")) {
            return listOf(
                trimmed,
                "cute hello",
                "hello sticker",
                "good morning sticker",
                "hi sticker"
            )
        }

        return listOf(query)
    }
}
