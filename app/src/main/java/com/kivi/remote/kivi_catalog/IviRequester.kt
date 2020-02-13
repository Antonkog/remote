package com.kivi.remote.kivi_catalog

import com.kivi.remote.kivi_catalog.model.*
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface IviRequester {

    @GET("catalogue/v7/")
    fun getContent(@Query("from") from: Int,
                   @Query("to") to: Int,
                   @Query("year_from") yearFromIds: List<Int>,
                   @Query("year_to") yearToIds: List<Int>,
                   @Query("category") categoriesIds: List<Int>,
                   @Query("meta_genre") genresIds: List<Int>,
                   @Query("country") countriesIds: List<Int>,
                   @Query("sort") sortType: String): Flowable<IviCatalogList>

    @GET("videofromcompilation/v7/")
    fun getEpisodes(@Query("id") id: Int,
                    @Query("season") season: Int,
                    @Query("from") from: Int,
                    @Query("to") to: Int): Flowable<IviEpisodeList>

    @GET("meta_genres/v5/")
    fun getMetaGenres(@Query("app_version") app_version: Int): Single<ResultMetaGenre>

    @GET("geocheck/whoami/v6/")
    fun getActualAppVersion(@Query("app_version") app_version: Int,
                            @Query("user_agent") user_agent: String): Single<ResultAppVersion>

    @GET("autocomplete/common/v7/?limit=15")
    fun getSearchSuggestion(@Query("query") query: String, @Query("is_erotic") isErotic: Boolean = false): Single<ResultSearchSuggestion>

}