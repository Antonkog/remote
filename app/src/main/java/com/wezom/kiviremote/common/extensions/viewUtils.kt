package com.wezom.kiviremote.common.extensions

import android.graphics.*
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.R


fun View.vanish() {
    this.visibility = View.GONE
}

val invertMatrix =
        floatArrayOf(-1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f)

val blueMatrix =
        floatArrayOf(0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 65f,
                0f, 0f, 0f, 0f, 172f,
                0f, 0f, 0f, 1f, 0f)

fun makeBlueBitmap(src: Bitmap): Bitmap {
    val colorMatrix = ColorMatrix(blueMatrix)

    val cf: ColorFilter = ColorMatrixColorFilter(colorMatrix);
    val bitmap: Bitmap = Bitmap.createBitmap(src.width, src.height,
            Bitmap.Config.ARGB_8888)

    val canvas = Canvas(bitmap)
    val paint = Paint()
    paint.colorFilter = cf
    canvas.drawBitmap(src, 0f, 0f, paint)
    return bitmap
}

fun View.changeBackgroundRecurcevly() {
    if (this is ViewGroup)
        for (counter in 0..childCount) {
            this.getChildAt(counter).changeBackgroundRecurcevly()
        }
    else {

        this.background = ContextCompat.getDrawable(context, R.drawable.shape_gradient_black)

    }

}