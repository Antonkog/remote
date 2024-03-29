package com.wezom.kiviremote.presentation.home.remotecontrol

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.GotAspectEvent
import com.wezom.kiviremote.bus.NewVolumeEvent
import com.wezom.kiviremote.bus.RequestAspectEvent
import com.wezom.kiviremote.bus.SendActionEvent
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.Run
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.presentation.base.TvKeysViewModel
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import timber.log.Timber


class RemoteControlViewModel(private val router: Router) : BaseViewModel(), TvKeysViewModel {
    init {
        disposables += RxBus.listen(NewVolumeEvent::class.java).subscribeBy(
                onNext = {
                    when {
                        it.volume > 0 -> muteStatus.postValue(false)
                        it.volume == 0 -> muteStatus.postValue(true)
                    }
                }, onError = Timber::e
        )


        disposables += RxBus.listen(GotAspectEvent::class.java).subscribeBy(
                onNext = {
                    aspectSeen.postValue(it)
                    imputSeen.postValue(it)
                }, onError = Timber::e
        )
    }


    val muteStatus = MutableLiveData<Boolean?>()
    val aspectSeen = MutableLiveData<GotAspectEvent>()
    val imputSeen = MutableLiveData<GotAspectEvent>()

    fun switchOff() = RxBus.publish(SendActionEvent(Action.SWITCH_OFF))

    fun goToAspect() = router.navigateTo(Screens.TV_SETTINGS_FRAGMENT)

    fun requestAspect() = Run.after(1000){
        RxBus.publish(RequestAspectEvent())
    }

    fun goToInputSettings() = router.navigateTo(Screens.PORTS_FRAGMENT)

}