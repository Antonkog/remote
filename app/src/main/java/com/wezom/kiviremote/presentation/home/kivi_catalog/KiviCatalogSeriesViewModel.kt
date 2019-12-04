package com.wezom.kiviremote.presentation.home.kivi_catalog

import android.content.Context
import com.wezom.kiviremote.kivi_catalog.IviService
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.presentation.home.kivi_catalog.adapters.MovieData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.PublishProcessor
import ru.terrakok.cicerone.Router

class KiviCatalogSeriesViewModel(val database: AppDatabase, val router: Router) : BaseViewModel() {

    private val pagination: PublishProcessor<Int> = PublishProcessor.create()

    var catalogRequestOnWay = false
    private var isPaginationCreated = false

    var seasonId: Int = 0
    var seasonNumber = 1

    fun paginationFetchEpisodesData(context: Context, from: Int, onResult: (data: List<MovieData>) -> Unit) {
        if (!isPaginationCreated) {
            isPaginationCreated = true
            disposables += pagination.onBackpressureDrop()
                    .doOnNext { catalogRequestOnWay = true }
                    .concatMap {
                        IviService().getService(context).getEpisodes(seasonId, seasonNumber, it,it + 15)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        //Show data in recyclerview
                        catalogRequestOnWay = false
                        val items = it.result.filter { !it.title.isNullOrEmpty() && !it.thumbs.isNullOrEmpty() }
                                .map {
                                    val posterUrl = it.thumbs[0]?.url ?: ""
                                    MovieData(it.id, it.title, "${it.season} сезон, ${it.episode} серия", posterUrl)
                                }

                        onResult(items)
                    }
                    .doOnError {}
                    .subscribe()
        }

        pagination.onNext(from)
    }

}