package com.wezom.kiviremote.common.glide

import android.content.Context
import android.util.Log
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class MyGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, 1024 * 1024 * 75))
        builder!!.setLogLevel(Log.DEBUG)
    }
//
//    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
//        unsafeOkHttpClient().let {
//            registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(it))
//        }
//    }
//
//    open fun unsafeOkHttpClient(): OkHttpClient {
//        val unsafeTrustManager = createUnsafeTrustManager()
//        val sslContext = SSLContext.getInstance("SSL")
//        sslContext.init(null, arrayOf(unsafeTrustManager), null)
//        return OkHttpClient.Builder()
//                .sslSocketFactory(sslContext.socketFactory,  unsafeTrustManager)
//                .hostnameVerifier { hostName, sslSession -> true }
//                .build()
//    }
//
//    fun createUnsafeTrustManager(): X509TrustManager {
//        return object : X509TrustManager {
//            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
//            }
//
//            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
//            }
//
//            override fun getAcceptedIssuers(): Array<out X509Certificate>? {
//                return emptyArray()
//            }
//        }
//    }
}