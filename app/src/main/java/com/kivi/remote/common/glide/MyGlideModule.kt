package com.kivi.remote.common.glide

import android.content.Context
import android.os.Build
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.MemoryCategory
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import java.io.InputStream
import java.util.concurrent.TimeUnit

@GlideModule
class MyGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, 1024 * 1024 * 75))
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024)
        builder.setMemoryCache(LruResourceCache(maxMemory / 16))
        builder.setLogLevel(Log.DEBUG)
    //    builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
    }

   override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val client = getNewHttpClient()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            glide.setMemoryCategory(MemoryCategory.NORMAL)
        }else{
            glide.setMemoryCategory(MemoryCategory.LOW)
        }

        val factory = OkHttpUrlLoader.Factory(client)

        glide.registry.replace(GlideUrl::class.java, InputStream::class.java!!, factory)
    }


    private fun getNewHttpClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .cache(null)
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)

        return Tls12SocketFactory.enableTls12OnPreLollipop(client).build()
    }

//    override fun registerComponents(context: Context?, glide: Glide?, registry: Registry?) {
//        registry!!.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory())
//    registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
//
//        val client = //UnsafeOkHttpClient.getUnsafeOkHttpClient()
//                OkHttpClient.Builder()
//                        .sslSocketFactory(TLSSocketFactory())
//                        .build()
//
//
//        val context = SSLContext.getInstance("TLS")
//        context.init(null, null, null)
//
//        var noSSLv3Factory: SSLSocketFactory? = null
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
//            noSSLv3Factory = TLSSocketFactory(sslContext.getSocketFactory())
//        } else {
//            noSSLv3Factory = sslContext.getSocketFactory()
//        }
//        connection.setSSLSocketFactory(noSSLv3Factory)
//
//
//        registry!!.replace(GlideUrl::class.java, InputStream::class.java,
//                OkHttpUrlLoader.Factory(client))
//    }
}