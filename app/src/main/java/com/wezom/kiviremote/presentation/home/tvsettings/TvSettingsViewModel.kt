package com.wezom.kiviremote.presentation.home.tvsettings

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.NewAspectEvent
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.base.BaseViewModel
import ru.terrakok.cicerone.Router


class TvSettingsViewModel(private val router: Router) : BaseViewModel() {

    fun navigateToHome() = router.backTo(Screens.DEVICE_SEARCH_FRAGMENT)

    fun navigateBack() = router.exit()

    val aspectMessage = MutableLiveData<AspectMessage>()

    fun sendAspectChangeEvent(msg : AspectMessage){
       // RxBus.publish()
        RxBus.publish(NewAspectEvent(msg))
        AspectHolder.message = msg
    }

}