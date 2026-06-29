package com.example.ui.components

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

object CoilImageLoader {
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return instance ?: synchronized(this) {
            val existing = instance
            if (existing != null) {
                existing
            } else {
                val loader = ImageLoader.Builder(context.applicationContext)
                    .components {
                        if (Build.VERSION.SDK_INT >= 28) {
                            add(ImageDecoderDecoder.Factory())
                        } else {
                            add(GifDecoder.Factory())
                        }
                    }
                    .crossfade(true)
                    .build()
                instance = loader
                loader
            }
        }
    }
}
