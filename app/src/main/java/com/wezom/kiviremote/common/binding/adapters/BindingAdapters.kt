package com.wezom.kiviremote.common.binding.adapters

import android.graphics.Bitmap
import android.widget.ImageView

fun loadImage(iv: ImageView, bitmap: Bitmap) {
    iv.setImageBitmap(bitmap)
}