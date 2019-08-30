package com.wezom.kiviremote.presentation.home.recommendations.deep

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.bus.LaunchChannelEvent
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.net.model.Channel
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.presentation.base.BaseViewModel
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
                                //                                Timber.d("12345 Populate channel  " + it.name)
                                channels.add(Channel()
                                        .addId(it.serverId)
                                        .addActive(it.is_active)
                                        .addIconUrl(it.imageUrl)
                                        .addSort(it.sort)
                                        .addEdited(it.edited_at)
                                        .addName(it.name)
                                )
                            }

//                            this.inputs.postValue(newInputs.distinct()) //could n't be same values
                            this.channels.postValue(channels)
                        }, onError = {
                    Timber.e(it.message)
                }
                )
    }


}