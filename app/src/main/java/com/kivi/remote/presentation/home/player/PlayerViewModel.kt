package com.kivi.remote.presentation.home.player

import androidx.lifecycle.MutableLiveData
import com.kivi.remote.bus.LaunchRecommendationEvent
import com.kivi.remote.bus.RemotePlayerEvent
import com.kivi.remote.bus.TVPlayerEvent
import com.kivi.remote.common.RxBus
import com.kivi.remote.common.extensions.getIviPreviewDuration
import com.kivi.remote.net.model.Recommendation
import com.kivi.remote.presentation.base.BaseViewModel
import com.kivi.remote.upnp.UPnPManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.concurrent.TimeUnit


class PlayerViewModel(private val router: Router, private val uPnPManager: UPnPManager) : BaseViewModel() {

    data class ProgressEvent(val condition: Int, val progress: Int, val passedTime: String, val leftTime: String)
    data class PreviewEvent(val imgUrl: String, val title: String?)

    val progressEvent = MutableLiveData<ProgressEvent>()
    val previewEvent = MutableLiveData<PreviewEvent>()
    val launchRecEvent = MutableLiveData<Recommendation>()

    var totalTimeMls = 2
    var timePassedMls = 1
    var timeLeftMls = 1


    val PLAYING = 1
    val PAUSED = 2
    val STOPPED = 3
    val ERROR = 4
    val SEEK_TO = 5

    private var localDisposables: CompositeDisposable? = null


    init {
        disposables += RxBus.listen(TVPlayerEvent::class.java).subscribeBy(
                onNext = {
                    when (it.playerAction) {
                        TVPlayerEvent.PlayerAction.CHANGE_STATE -> {
                            when (it.progress) {
                                PLAYING -> {
                                    showProgress(PLAYING)
                                    startPlayerTimer()
                                }
                                PAUSED -> {
                                    showProgress(PAUSED)
                                    disposelocalDisposables()
                                }
                                STOPPED -> {
                                    showProgress(STOPPED)
                                    disposelocalDisposables()
                                    totalTimeMls = 2
                                    timePassedMls = 1
                                    timeLeftMls = 1
                                }
                                ERROR -> {
                                    showProgress(STOPPED)
                                    disposelocalDisposables()
                                }
                            }
                        }

                        TVPlayerEvent.PlayerAction.LAUNCH_PLAYER -> {
                            totalTimeMls = it.playerPreview?.additionalData?.get("duration")?.toIntOrNull()
                                    ?: 0
                            timeLeftMls = totalTimeMls
                            timePassedMls = 0
                            startPlayerTimer()

                            if (it.playerPreview?.imageUrl != null && it.playerPreview.name != null)
                                previewEvent.postValue(PreviewEvent(it.playerPreview.imageUrl!!, it.playerPreview.name))
                        }

                        TVPlayerEvent.PlayerAction.SEEK_TO -> {
                            Timber.e(" from tv SEEK_TO: " + it.progress)

                            timePassedMls = it.progress
                            timeLeftMls = totalTimeMls - timePassedMls

                            showProgress(SEEK_TO)
                        }

                        TVPlayerEvent.PlayerAction.LAST_REQUEST_ERROR -> {
                            Timber.e(" from tv LAST_REQUEST_ERROR: " + it.progress)
                            showProgress(ERROR)
                        }
                    }

                }, onError = Timber::e
        )

        disposables += RxBus.listen(LaunchRecommendationEvent::class.java).subscribeBy( //when user call
                onNext = {
                    launchRecEvent.postValue(it.recommendation)
                }, onError = Timber::e
        )
    }

    private fun showProgress(condition: Int) {
        if (totalTimeMls != 0)
            progressEvent.postValue(ProgressEvent(condition, (timePassedMls * 100 / totalTimeMls),
                    ("" + timePassedMls).getIviPreviewDuration(),
                    ("" + timeLeftMls).getIviPreviewDuration()
            ))
        else progressEvent.postValue(ProgressEvent(condition, 0,
                "",
                ""
        ))
    }


    private fun startPlayerTimer() {
            addlocalDisposables(Observable.interval(0, 1, TimeUnit.SECONDS)
                    .take(100)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        timePassedMls += 1000
                        timeLeftMls = totalTimeMls - timePassedMls
                        showProgress(PLAYING)
                    })
    }


    fun seekTo(process: Int) {
        RxBus.publish(RemotePlayerEvent(RemotePlayerEvent.PlayerAction.SEEK_TO, listOf(0F + process)))
    }

    fun requestPreviewState() {
        RxBus.publish(RemotePlayerEvent(RemotePlayerEvent.PlayerAction.REQUEST_CONTENT, null))
    }


    fun play(){
        RxBus.publish(RemotePlayerEvent(RemotePlayerEvent.PlayerAction.PLAY, null))
    }

    fun pause(){
        RxBus.publish(RemotePlayerEvent(RemotePlayerEvent.PlayerAction.PAUSE, null))
    }

    fun closePlayer(){
        RxBus.publish(RemotePlayerEvent(RemotePlayerEvent.PlayerAction.CLOSE, null))
    }

    override fun onCleared() {
        localDisposables?.clear()
        super.onCleared()
    }

    fun addlocalDisposables(disposable: Disposable) {
        if (localDisposables == null) {
            localDisposables = CompositeDisposable()
        }
        localDisposables?.clear()
        localDisposables?.add(disposable)
    }

    fun disposelocalDisposables() {
        localDisposables?.dispose()
        localDisposables = null
    }

}