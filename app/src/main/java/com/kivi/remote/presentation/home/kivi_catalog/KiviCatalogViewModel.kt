package com.kivi.remote.presentation.home.kivi_catalog

import android.content.Context
import com.kivi.remote.Screens
import com.kivi.remote.bus.LaunchRecommendationEvent
import com.kivi.remote.bus.SendVoiceEvent
import com.kivi.remote.common.RxBus
import com.kivi.remote.kivi_catalog.Constants
import com.kivi.remote.kivi_catalog.IviService
import com.kivi.remote.kivi_catalog.model.FilterModel
import com.kivi.remote.kivi_catalog.model.SortType
import com.kivi.remote.net.model.Recommendation
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.presentation.base.BaseViewModel
import com.kivi.remote.presentation.home.kivi_catalog.adapters.CatalogFilter
import com.kivi.remote.presentation.home.kivi_catalog.adapters.MovieData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router

class KiviCatalogViewModel(val database: AppDatabase, val router: Router) : BaseViewModel() {

    private val pagination: PublishProcessor<Int> = PublishProcessor.create()


    var filterListYears = FilterModel.getYears().map { CatalogFilter(it.id, it.title, it.startYear, it.endYear) }
    var filterListCategories = FilterModel.getCategories().map { CatalogFilter(it.id, it.title, it.startYear, it.endYear) }
    var filterListGenres = FilterModel.getGenres().map { CatalogFilter(it.id, it.title, it.startYear, it.endYear) }
    var filterListCountries = FilterModel.getCountries().map { CatalogFilter(it.id, it.title, it.startYear, it.endYear) }

    var catalogRequestOnWay = false
    var catalogSortType = SortType.POP

    private var isPaginationCreated = false

    fun navigateToCatalogSeries(data: MovieData) = router.navigateTo(Screens.KIVI_CATALOG_SERIES_FRAGMENT, data)

    fun paginationFetchCatalogData(context: Context, from: Int, onResult: (data: List<MovieData>) -> Unit) {
        if (!isPaginationCreated) {
            isPaginationCreated = true
            disposables += pagination.onBackpressureDrop()
                    .doOnNext { catalogRequestOnWay = true }
                    .concatMap {
                        val startYearsIds = filterListYears.filter { it.isChecked }.map { it.startYear }
                        val endYearsIds = filterListYears.filter { it.isChecked }.map { it.endYear }
                        val categoriesIds = filterListCategories.filter { it.isChecked }.map { it.id }
                        val genresIds = filterListGenres.filter { it.isChecked }.map { it.id }
                        val countriesIds = filterListCountries.filter { it.isChecked }.map { it.id }
                        IviService().getService(context).getContent(it,it + 16, startYearsIds, endYearsIds, categoriesIds, genresIds, countriesIds, catalogSortType)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        //Show data in recyclerview
                        catalogRequestOnWay = false
                        val items = it.result.filter { !it.title.isNullOrEmpty() && !it.posters.isNullOrEmpty() }
                                .map {
                                    val genre = Constants.getGenresList(it.genres.toIntArray())?.firstOrNull() ?: "Драмы"
                                    val posterUrl = it.posters[0].url
                                    val year = if (it.year != 0) it.year else "${it.years.firstOrNull() ?: "2015"} - ${it.years.lastOrNull() ?: "2015"}"
                                    MovieData(it.id, it.title, "$year\n$genre", posterUrl, if (it.isSeries()) it.seasons else null, it.isSeries())
                                }

                        onResult(items)
                    }
                    .doOnError {}
                    .subscribe()
        }

        pagination.onNext(from)
    }

    fun fetchAutocompleteData(context: Context, query: String, onResult: (data: List<MovieData>) -> Unit) {
        if (query.isEmpty()) {
            return
        }

        disposables += IviService().getService(context).getSearchSuggestion(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it ->
                    val items = it.result.filter { !it.title.isNullOrEmpty() && !it.posters.isNullOrEmpty() }
                            .map {
                                val genre = Constants.getGenresList(it.genres.toIntArray())?.firstOrNull() ?: "Драмы"
                                val posterUrl = it.posters[0]?.url ?: ""
                                val year = if (it.year != 0) it.year else it.years.firstOrNull() ?: "2015"
                                MovieData(it.id, it.title, "$year , $genre", posterUrl, if (it.isSeries) it.seasons else null, it.isSeries)
                            }

                    onResult(items)
                }
    }

    fun showContentOnTv(data: MovieData) {
        RxBus.publish(LaunchRecommendationEvent(Recommendation().setFromMovie(data)))
    }


    fun sendVoiceAsText(text: String) {
        RxBus.publish(SendVoiceEvent(text))
    }



}