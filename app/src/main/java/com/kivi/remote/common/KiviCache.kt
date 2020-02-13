package com.kivi.remote.common

import android.graphics.Bitmap
import com.bumptech.glide.util.LruCache
import timber.log.Timber

class KiviCache(size: Long) : LruCache<String, Bitmap>(size) {
    override fun onItemEvicted(key: String, item: Bitmap?) {
        super.onItemEvicted(key, item)
        Timber.e("KiviCache onItemEvicted :  $key")
    }

    override fun put(key: String, item: Bitmap?): Bitmap? {
        Timber.e("KiviCache put :  $key")
        return super.put(key, item)
    }

    override fun get(key: String): Bitmap? {
        Timber.e("KiviCache get :  $key")
        return super.get(key)
    }

    // fun sizeOf(key: String, value: Bitmap) = value.byteCount / 512
}