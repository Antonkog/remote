package com.wezom.kiviremote.kivi_catalog.model

import java.io.Serializable

data class IviEpisodeList(val result: List<IviEpisodeItem>) : Serializable

data class IviEpisodeItem(val id: Int,
                          val title: String,
                          val season: Int,
                          val episode: Int,
                          val genres: List<Int>,
                          val thumbs: List<Poster>,
                          val year: Int) : Serializable