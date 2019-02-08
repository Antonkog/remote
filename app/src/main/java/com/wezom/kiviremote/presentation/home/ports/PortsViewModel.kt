package com.wezom.kiviremote.presentation.home.ports

import android.arch.lifecycle.MutableLiveData
import com.wezom.kiviremote.bus.GotAspectEvent
import com.wezom.kiviremote.bus.NewAspectEvent
import com.wezom.kiviremote.bus.RequestAspectEvent
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import timber.log.Timber


class PortsViewModel(private val router: Router) : BaseViewModel() {

    val aspectEvent = MutableLiveData<GotAspectEvent>()

    init {
        disposables += RxBus.listen(GotAspectEvent::class.java).observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            aspectEvent.postValue(it)
                        }, onError = Timber::e
                )
    }

    fun sendAspectSingleChangeEvent(valueType: AspectMessage.ASPECT_VALUE, value: Int) {
        val builder = AspectMessage.AspectMsgBuilder()
        builder.addValue(valueType, value)
        RxBus.publish(NewAspectEvent(builder.buildAspect()))
    }

    fun requestAspect() = RxBus.publish(RequestAspectEvent())

}