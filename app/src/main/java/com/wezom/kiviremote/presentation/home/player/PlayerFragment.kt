package com.wezom.kiviremote.presentation.home.player

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.wezom.kiviremote.App
import com.wezom.kiviremote.bus.LaunchRecommendationEvent
import com.wezom.kiviremote.bus.TVPlayerEvent
import com.wezom.kiviremote.common.extensions.getIviPreviewDuration
import com.wezom.kiviremote.common.glide.GlideApp
import com.wezom.kiviremote.databinding.PlayerFragmentBinding
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PlayerFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var binding: PlayerFragmentBinding
    lateinit var viewModel: PlayerViewModel

    var nextPlay = true
    var totalMillSecTime = 0L
    var timePassedMillsec = 0L


    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PlayerFragmentBinding.inflate(inflater, container!!, false)

        binding.slidingPanel.setBackgroundColor(binding.root.resources.getColor(if (App.isDarkMode()) com.wezom.kiviremote.R.color.touch_body else com.wezom.kiviremote.R.color.colorWhite))
        binding.topContainer.setBackgroundColor(binding.root.resources.getColor(if (App.isDarkMode()) com.wezom.kiviremote.R.color.touch_header_dm else com.wezom.kiviremote.R.color.colorWhite))

        if (binding.renderProgress != null) {
            binding.renderProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {


                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {

                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    viewModel.seekTo(seekBar.progress)
                }
            })
        }

        binding.renderPlay.setOnClickListener { view ->
            run {
                nextPlay = !nextPlay
                binding.renderPlay.setImageResource(if (nextPlay) com.wezom.kiviremote.R.drawable.ic_image_play else com.wezom.kiviremote.R.drawable.ic_image_pause)
                viewModel.playOrPause(nextPlay)
            }
        }

//        binding.renderPrevious.setOnClickListener { view -> viewModel.playPrev() }
//        binding.renderNext.setOnClickListener { view -> viewModel.playNext() }

        return binding.root
    }

    private val recsObserver = Observer<LaunchRecommendationEvent> {
        GlideApp.with(this).load(it?.recommendation?.imageUrl).into(binding.renderPreview)
        binding.renderTitle.text = it?.recommendation?.name
        Timber.e(it?.recommendation.toString())
    }

    private val PLAYING = 1
    private val PAUSED = 2
    private val STOPPED = 3

    private lateinit var progressDisposable: Disposable

    private fun startPlayerTimer() {
        val period = totalMillSecTime / (1000 * 60 * 100)

        progressDisposable = Observable.interval(0, period, TimeUnit.SECONDS)
                .take(100)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (binding.renderProgress.progress < 100) {
                        binding.renderProgress.progress = binding.renderProgress.progress + 1

                        timePassedMillsec = binding.renderProgress.progress * period

                        binding.renderElapsed.text = ("" + timePassedMillsec).getIviPreviewDuration()
                        binding.renderRemaining.text = ("" + (totalMillSecTime - timePassedMillsec)).getIviPreviewDuration()
                    } else {
                        binding.renderElapsed.text = ""
                        binding.renderRemaining.text = "00:00:00"
                    }
                    Timber.e("progress now " + binding.renderProgress.progress)
                }
    }


    private val playerObserver = Observer<TVPlayerEvent> {
        when (it?.playerAction) {
            TVPlayerEvent.PlayerAction.CHANGE_STATE -> {
                when (it.progress) {
                    PLAYING -> {
                        nextPlay = true
                        binding.renderPlay.setImageResource(com.wezom.kiviremote.R.drawable.ic_image_play)
                        startPlayerTimer()
                    }
                    PAUSED -> {
                        nextPlay = false
                        binding.renderPlay.setImageResource(com.wezom.kiviremote.R.drawable.ic_image_pause)
                        progressDisposable.dispose()
                    }
                    STOPPED -> {
                        nextPlay = false
                        binding.renderProgress.progress = 0
                        binding.renderPlay.setImageResource(com.wezom.kiviremote.R.drawable.ic_image_pause)
                        progressDisposable.dispose()
                    }
                }
            }

            TVPlayerEvent.PlayerAction.LAUNCH_PLAYER -> {
                GlideApp.with(this).load(it.playerPreview.imageUrl).into(binding.renderPreview)
//              val  totalTimeCurrent = it.playerPreview?.additionalData?.get("duration").toLongOrNull() ?: 0
                binding.renderRemaining.text = it.playerPreview?.additionalData?.get("duration")?.getIviPreviewDuration()
                binding.renderTitle.text = it.playerPreview.name
                binding.renderProgress.progress = 0
                totalMillSecTime = it.playerPreview?.additionalData?.get("duration")?.toLongOrNull()
                        ?: 0

                startPlayerTimer()
            }

            TVPlayerEvent.PlayerAction.SEEK_TO -> {
                Timber.e(" from tv SEEK_TO: " + it.progress)
                val period = totalMillSecTime / (1000 * 60 * 100)
                timePassedMillsec = totalMillSecTime - period * it.progress

                binding.renderProgress.progress = (it?.progress)
                binding.renderElapsed.text = ("" + timePassedMillsec).getIviPreviewDuration()
                binding.renderRemaining.text = ("" + (totalMillSecTime - timePassedMillsec)).getIviPreviewDuration()

            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PlayerViewModel::class.java)
        viewModel.run {
            tvPlayerEvent.observe(this@PlayerFragment, playerObserver)
            launchRecommendationEvent.observe(this@PlayerFragment, recsObserver)
        }
    }
}