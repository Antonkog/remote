package com.wezom.kiviremote.common.glide

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.wezom.kiviremote.net.UnsafeOkHttpClient
import java.io.InputStream

@GlideModule
class MyGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, 1024 * 1024 * 75))
        builder!!.setLogLevel(Log.DEBUG)
    }

    override fun registerComponents(context: Context?, glide: Glide?, registry: Registry?) {
                registry!!.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory())
        val client = UnsafeOkHttpClient.getUnsafeOkHttpClient()
        registry!!.replace(GlideUrl::class.java, InputStream::class.java,
                OkHttpUrlLoader.Factory(client))
    }

}