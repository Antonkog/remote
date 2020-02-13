package com.kivi.remote.presentation.home.recommendations.deep

import androidx.lifecycle.MutableLiveData
import com.kivi.remote.bus.LaunchChannelEvent
import com.kivi.remote.common.RxBus
import com.kivi.remote.net.model.Channel
import com.kivi.remote.persistence.AppDatabase
import com.kivi.remote.presentation.base.BaseViewModel
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import timber.log.Timber

class ChannelsDeepViewModel(val router: Router, val database: AppDatabase) : BaseViewModel() {

    fun launchChannel(channel: Channel) {
        RxBus.publish(LaunchChannelEvent(channel))
    }

    val channels = MutableLiveData<List<Channel>>()


    fun populateChannels() {
        disposables += database.chennelsDao()
                .all
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onNext = { dbChannels ->
                            val channels = ArrayList<Channel>()
                            dbChannels.forEach {
                                channels.add(Channel()
                                        .addId(it.serverId)
                                        .addActive(it.is_active)
                                        .addIconUrl(it.imageUrl)
                                        .addSort(it.sort)
                                        .addEdited(it.edited_at)
                                        .addName(it.name)
                                )
                            }
                            this.channels.postValue(channels)
                        }, onError = {
                    Timber.e(it.message)
                }
                )
    }


}