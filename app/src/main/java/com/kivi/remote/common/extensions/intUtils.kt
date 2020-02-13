@file:JvmName("NumUtils")

package com.kivi.remote.common.extensions

import android.content.res.Resources

val density = Resources.getSystem().displayMetrics.density

val Int.toPx: Int
    get() = (this * density).toInt()

val Int.toDp: Int
    get() = (this / density).toInt()

val Float.toPx: Float
    get() = this * density

val Float.toDp: Float
    get() = this / density
