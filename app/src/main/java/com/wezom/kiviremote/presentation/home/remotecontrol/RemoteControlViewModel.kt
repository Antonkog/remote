package com.wezom.kiviremote.presentation.home.remotecontrol

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.GotAspectEvent
import com.wezom.kiviremote.bus.NewVolumeEvent
import com.wezom.kiviremote.bus.SendActionEvent
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.presentation.base.TvKeysViewModel
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
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
                    Timber.e("got aspect2: " + AspectHolder.availableSettings.toString() + AspectHolder.message.toString())
                    if (AspectHolder.availableSettings != null && AspectHolder.message != null) {
                        aspectSeen.postValue(true)
                    } else {
                        aspectSeen.postValue(false)
                    }
                }, onError = Timber::e
        )
    }

    val muteStatus = MutableLiveData<Boolean?>()
    val aspectSeen = MutableLiveData<Boolean?>()

    fun switchOff() = RxBus.publish(SendActionEvent(Action.SWITCH_OFF))

    fun goToAspect() = router.navigateTo(Screens.TV_SETTINGS_FRAGMENT)

    fun goToInputSettings( ) = router.navigateTo(Screens.PORTS_FRAGMENT)

}