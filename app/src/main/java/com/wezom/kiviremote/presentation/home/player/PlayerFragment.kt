package com.wezom.kiviremote.presentation.home.player

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.wezom.kiviremote.App
import com.wezom.kiviremote.common.glide.GlideApp
import com.wezom.kiviremote.databinding.PlayerFragmentBinding
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import javax.inject.Inject


class PlayerFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var binding: PlayerFragmentBinding
    lateinit var viewModel: PlayerViewModel

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
                    if(viewModel.totalTimeMls > 500) //1/2 second - need to do some fix - when we don't have connection
                    viewModel.seekTo(seekBar.progress)
                }
            })
        }


        binding.renderPlay.setOnClickListener { view ->
            run {
                viewModel.nextPlay = !viewModel.nextPlay
                viewModel.playOrPause(viewModel.nextPlay)
            }
        }
        return binding.root
    }

    private val playerObserver = Observer<PlayerViewModel.ProgressEvent> {
        if (it != null) {
            showProgress(it.condition, it.progress, it.passedTime, it.leftTime)
        }
    }

    private val previewObserver = Observer<PlayerViewModel.PreviewEvent> {
        if (it != null) {
            showPreview(it.imgUrl, it.title?:"")
        }
    }


    fun showProgress(condition: Int, progress: Int, timePassed: String, timeLeft: String) {

        viewModel.nextPlay = condition == viewModel.PLAYING

        when (condition) {
            viewModel.PAUSED -> {
                binding.renderPlay.setImageResource(com.wezom.kiviremote.R.drawable.ic_image_play)
            }
            viewModel.PLAYING -> {
                binding.renderElapsed.text = timePassed
                binding.renderRemaining.text = timeLeft
                binding.renderProgress.progress = progress
                binding.renderSlideshowProgress.progress = progress
                binding.renderSlideshowProgressBackground.progress = progress
                binding.renderPlay.setImageResource(com.wezom.kiviremote.R.drawable.ic_image_pause)

            }
            viewModel.STOPPED -> {
                binding.renderPlay.setImageResource(com.wezom.kiviremote.R.drawable.ic_image_play)
                (activity as HomeActivity).hideSlidingPanel()
                binding.renderElapsed.text = "00:00:00"
                binding.renderRemaining.text = "00:00:00"
//                binding.renderPlay.setImageResource(android.R.color.transparent);
            }
            viewModel.SEEK_TO -> {
                binding.renderElapsed.text = timePassed
                binding.renderRemaining.text = timeLeft
                binding.renderProgress.progress = progress
                binding.renderSlideshowProgress.progress = progress
                binding.renderSlideshowProgressBackground.progress = progress
            }
            viewModel.ERROR -> {
                binding.renderPlay.setImageResource(com.wezom.kiviremote.R.drawable.ic_image_play)
                (activity as HomeActivity).hideSlidingPanel()
                binding.renderElapsed.text = "00:00:00"
                binding.renderRemaining.text = "00:00:00"
//                binding.renderPlay.setImageResource(android.R.color.transparent);
            }

        }
    }

    fun showPreview(url: String?, title: String) {
        (activity as HomeActivity).expandSlidingPanel()

        GlideApp.with(this).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.renderPreview)

        binding.renderTitle.text = title
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PlayerViewModel::class.java)
        viewModel.run {
            previewEvent.observe(this@PlayerFragment, previewObserver)
            progressEvent.observe(this@PlayerFragment, playerObserver)
        }
    }
}