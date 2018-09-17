package com.wezom.kiviremote.presentation.home.remotecontrol

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.bus.NewVolumeEvent
import com.wezom.kiviremote.bus.SendActionEvent
import com.wezom.kiviremote.bus.SendKeyEvent
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.presentation.base.BaseViewModel
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber


class RemoteControlViewModel : BaseViewModel() {
    init {
        disposables += RxBus.listen(NewVolumeEvent::class.java).subscribeBy(
                onNext = {
                    when {
                        it.volume > 0 -> muteStatus.postValue(false)
                        it.volume == 0 -> muteStatus.postValue(true)
                    }
                }, onError = Timber::e
        )
    }

    val muteStatus = MutableLiveData<Boolean?>()

    fun sendButtonClick(keyEvent: Int) = RxBus.publish(SendKeyEvent(keyEvent))

    fun switchOff() = RxBus.publish(SendActionEvent(Action.SWITCH_OFF))

    fun sendHomeDown() = RxBus.publish(SendActionEvent(Action.HOME_DOWN))

    fun sendHomeUp() = RxBus.publish(SendActionEvent(Action.HOME_UP))

    fun launchQuickApps() = RxBus.publish(SendActionEvent(Action.LAUNCH_QUICK_APPS))
}