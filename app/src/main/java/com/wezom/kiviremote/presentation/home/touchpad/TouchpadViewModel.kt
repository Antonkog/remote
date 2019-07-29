package com.wezom.kiviremote.presentation.home.touchpad

import android.arch.lifecycle.MutableLiveData
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.*
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.presentation.base.TvKeysViewModel
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import timber.log.Timber


class TouchpadViewModel(private val router: Router) : BaseViewModel(), TvKeysViewModel {


    lateinit var speechListener: SpeechRecognizer

    val aspectSeen = MutableLiveData<GotAspectEvent>()

    init {
        disposables += RxBus.listen(GotAspectEvent::class.java).subscribeBy(
                onNext = {
                    aspectSeen.postValue(it)
                }, onError = Timber::e
        )
    }

    fun sendMotionMessage(x: Double, y: Double) {
        RxBus.publish(SendCursorCoordinatesEvent(x, y))
    }

    fun sendClickMessage(x: Double, y: Double, buttonType: Action) {
        Timber.d("sendClickMessage %s %s %s ", x, y, buttonType)
        RxBus.publish(TouchpadButtonClickEvent(x, y, buttonType))
    }

    fun sendScrollEvent(action: Action, y: Double) {
        RxBus.publish(SendScrollEvent(action, y))
    }

    fun switchOff() = RxBus.publish(SendActionEvent(Action.SWITCH_OFF))


    fun goToInputSettings() = router.navigateTo(Screens.PORTS_FRAGMENT)

    fun sendText(text: String) {
        RxBus.publish(SendTextEvent(text))
    }

    fun setSpeachRecognizer(speechRecognizer: SpeechRecognizer) {
        speechListener = speechRecognizer


        speechListener.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(rmsdB: Float) {

            }

            override fun onBufferReceived(buffer: ByteArray) {

            }

            override fun onEndOfSpeech() {

            }

            override fun onError(error: Int) {

            }

            override fun onResults(results: Bundle) {
                val matches = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                //displaying the first match
                if (matches != null)
                    router.showSystemMessage(matches[0])

                sendText(matches[0])
            }

            override fun onPartialResults(partialResults: Bundle) {

            }

            override fun onEvent(eventType: Int, params: Bundle) {

            }
        })
    }

}