package com.kivi.remote.kivi_catalog.model

import java.io.Serializable

data class IviCatalogList(val result: List<IviMovieItem>) : Serializable

data class IviMovieItem(val id: Int,
                        val title: String,
                        val seasons: List<IviSeason>,
                        val genres: List<Int>,
                        val posters: List<Poster>,
                        val object_type: String,
                        val year: Int, val years: List<Int>) : Serializable {
    fun isSeries(): Boolean = object_type == "compilation"
}

data class IviSeason(val number: Int,
                     val season_id: Int,
                     val episode_count: Int,
                     val min_episode: Int,
                     val max_episode: Int) : Serializable