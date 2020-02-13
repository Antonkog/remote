package com.kivi.remote.common.extensions

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup


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

val blackMatrix =
        floatArrayOf(0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f)

val whiteMatrix =
        floatArrayOf(0f, 0f, 0f, 0f, 255f,
                0f, 0f, 0f, 0f, 255f,
                0f, 0f, 0f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f)



fun setBitmapMatrix(src: Bitmap, matrix : ColorMatrix): Bitmap {
    val colorMatrix = ColorMatrix(matrix)

    val cf: ColorFilter = ColorMatrixColorFilter(colorMatrix);
    val bitmap: Bitmap = Bitmap.createBitmap(src.width, src.height,
            Bitmap.Config.ARGB_8888)

    val canvas = Canvas(bitmap)
    val paint = Paint()
    paint.colorFilter = cf
    canvas.drawBitmap(src, 0f, 0f, paint)
    return bitmap
}

fun View.changeBackgroundRecurcevly(drawable: Drawable) {
    if (this is ViewGroup)
        for (counter in 0..childCount) {
            this.getChildAt(counter).changeBackgroundRecurcevly(drawable)
        }
    else {
        this.background = drawable
    }

}