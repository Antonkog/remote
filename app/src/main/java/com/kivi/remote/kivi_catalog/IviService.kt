package com.kivi.remote.kivi_catalog

import android.content.Context
import com.kivi.remote.common.isNetworkAvailable
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class IviService {

    fun getService(context: Context?, onlineCacheAge: Int = 60 * 60 * 24 * 7, offlineCacheAge: Int = 60 * 60 * 24 * 7, cacheSize: Long = 5 * 1024 * 1024): IviRequester {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = if (context != null) {

            val cache = Cache(context.cacheDir, cacheSize)

            OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(logging)
                    .addInterceptor { chain ->
                        var request = chain.request()
                        request = if (isNetworkAvailable(context))
                            request.newBuilder().header("Cache-Control", "public, max-age=$onlineCacheAge").build()
                        else
                            request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=$offlineCacheAge").build()
                        chain.proceed(request)
                    }
                    .build()
        } else {
            OkHttpClient.Builder().addInterceptor(logging).build()
        }

        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(IviRequester::class.java)
    }

    companion object {
        private const val BASE_URL = "https://api.ivi.ru/mobileapi/"
    }

}