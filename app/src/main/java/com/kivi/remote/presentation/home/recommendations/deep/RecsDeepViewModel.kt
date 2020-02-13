package com.kivi.remote.presentation.home.recommendations.deep

import androidx.lifecycle.MutableLiveData
import com.kivi.remote.bus.LaunchRecommendationEvent
import com.kivi.remote.common.RxBus
import com.kivi.remote.net.model.Recommendation
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.presentation.base.BaseViewModel
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