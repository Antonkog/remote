package com.wezom.kiviremote.presentation.home.ports

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.bus.GotAspectEvent
import com.wezom.kiviremote.bus.NewAspectEvent
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.presentation.base.TvKeysViewModel
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import timber.log.Timber


class PortsViewModel(private val router: Router) : BaseViewModel(), TvKeysViewModel {

    val ports = MutableLiveData<List<Port>>()

    init {
        disposables += RxBus.listen(GotAspectEvent::class.java).subscribeBy(
                onNext = {
                    ports.value =  InputSourceHelper.getPortsList(it?.available?.porsSettings, it?.msg?.currentPort ?: 1)
                }, onError = Timber::e
        )
    }


    fun sendAspectSingleChangeEvent(valueType: AspectMessage.ASPECT_VALUE, value: Int) {
        val builder = AspectMessage.AspectMsgBuilder()
        builder.addValue(valueType, value)
        RxBus.publish(NewAspectEvent(builder.buildAspect()))
    }
}