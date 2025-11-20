package com.contacts.android.contacts

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.contacts.android.contacts.ads.AdMobManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ContactsApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var adMobManager: AdMobManager

    override fun onCreate() {
        super.onCreate()

        // Initialize AdMob SDK
        // This should be called as early as possible for best ad performance
        adMobManager.initialize()
    }

    // Language handling is now done via AppCompatDelegate in MainActivity
    // which follows Google's official recommendations for per-app language preferences

    /**
     * Configure Coil ImageLoader for optimal performance
     * - Memory cache: 25% of available memory
     * - Disk cache: 50MB for contact photos
     * - Aggressive caching policies for better performance
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Use 25% of available memory for image cache
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB disk cache
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED) // Enable memory caching
            .diskCachePolicy(CachePolicy.ENABLED)   // Enable disk caching
            .respectCacheHeaders(false) // Ignore cache headers for local images
            .crossfade(true) // Smooth image transitions
            .crossfade(300)  // 300ms crossfade duration
            .build()
    }
}
