package com.wezom.kiviremote.presentation.home.player

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.bus.LaunchRecommendationEvent
import com.wezom.kiviremote.bus.RemotePlayerEvent
import com.wezom.kiviremote.bus.TVPlayerEvent
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.upnp.UPnPManager
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import timber.log.Timber


class PlayerViewModel(private val router: Router, private val uPnPManager: UPnPManager) : BaseViewModel() {

    val tvPlayerEvent = MutableLiveData<TVPlayerEvent>()
    val launchRecommendationEvent = MutableLiveData<LaunchRecommendationEvent>()

    init {
        disposables += RxBus.listen(TVPlayerEvent::class.java).subscribeBy(
                onNext = {
                    tvPlayerEvent.postValue(it)
                }, onError = Timber::e
        )

        disposables += RxBus.listen(LaunchRecommendationEvent::class.java).subscribeBy(
                onNext = {
                    launchRecommendationEvent.postValue(it)
                }, onError = Timber::e
        )
    }

    fun seekTo(process: Int) {
        RxBus.publish(RemotePlayerEvent(RemotePlayerEvent.PlayerAction.SEEK_TO, listOf(0F + process)))
    }
//
//    fun playPrev() {
//        RxBus.publish(RemotePlayerEvent(RemotePlayerEvent.PlayerAction.PLAY_PREV, null))
//
//    }
//
//    fun playNext() {
//        RxBus.publish(RemotePlayerEvent(RemotePlayerEvent.PlayerAction.PLAY_NEXT, null))
//
//    }

    fun playOrPause(play: Boolean) {
        if (play)
            RxBus.publish(RemotePlayerEvent(RemotePlayerEvent.PlayerAction.PLAY, null))
        else
            RxBus.publish(RemotePlayerEvent(RemotePlayerEvent.PlayerAction.PAUSE, null))
    }

}