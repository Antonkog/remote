package com.wezom.kiviremote.presentation.home.recommendations.deep

import androidx.lifecycle.MutableLiveData
import com.wezom.kiviremote.bus.LaunchRecommendationEvent
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.net.model.Recommendation
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.presentation.base.BaseViewModel
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import timber.log.Timber

class RecsDeepViewModel (val router: Router, val database: AppDatabase): BaseViewModel() {
    fun launchRecommendation(recommendation: Recommendation) {
        RxBus.publish(LaunchRecommendationEvent(recommendation))
    }


    val recommendations = MutableLiveData<List<Recommendation>>()

    fun populateRecommendations() {
        disposables += database.recommendationsDao()
                .all
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onNext = { dbRecs ->
                            val recommendations = ArrayList<Recommendation>()
                            dbRecs.forEach {
                                recommendations.add(Recommendation()
                                        .addContentId(it.contentID)
                                        .addImageUrl(it.imageUrl)
                                        .setImdb(it.imdb)
                                        .addKind(it.kind)
                                        .addDiscription(it.description)
                                        .addTitle(it.title)
                                        .addSubtitle(it.subTitle)
                                )
                            }
                            this.recommendations.postValue(recommendations)
                        }, onError = {
                    Timber.e(it.message)
                }
                )
    }

}