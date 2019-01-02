package com.wezom.kiviremote.presentation.home.tvsettings

import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.NewAspectEvent
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.presentation.base.TvKeysViewModel
import ru.terrakok.cicerone.Router
import timber.log.Timber


class TvSettingsViewModel(private val router: Router) : BaseViewModel(), TvKeysViewModel {

    fun sendAspectSingleChangeEvent(valueType: AspectMessage.ASPECT_VALUE, value: Int) {
        val builder = AspectMessage.AspectMsgBuilder()
        builder.addValue(valueType, value)
        RxBus.publish(NewAspectEvent(builder.buildAspect()))
        Timber.e("sending aspect 3: " +builder.buildAspect().settings.toString())

    }

    fun goToInputSettings() = router.navigateTo(Screens.PORTS_FRAGMENT)

    fun goBack() = router.exit()

}