package com.wezom.kiviremote.common

import android.graphics.Bitmap
import android.util.LruCache

class KiviCache(size: Int) : LruCache<String, Bitmap>(size) {
    override fun sizeOf(key: String, value: Bitmap) = value.byteCount / 512
}