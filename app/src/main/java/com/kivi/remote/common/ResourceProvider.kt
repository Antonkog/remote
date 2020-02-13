package com.kivi.remote.common

import android.content.Context


class ResourceProvider(private val context: Context) {
    fun getString(resId: Int): String = context.getString(resId)

    fun getString(resId: Int, formatValue: String): String = context.getString(resId, formatValue)
}