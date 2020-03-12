package com.kivi.remote.presentation.home.player

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kivi.remote.App
import com.kivi.remote.R
import com.kivi.remote.common.extensions.getIviPreviewDuration
import com.kivi.remote.databinding.PlayerFragmentBinding
import com.kivi.remote.net.model.Recommendation
import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.home.HomeActivity
import timber.log.Timber
import javax.inject.Inject


class PlayerFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory


    private lateinit var mDetector: GestureDetectorCompat

    private lateinit var binding: PlayerFragmentBinding
    lateinit var viewModel: PlayerViewModel

    override fun injectDependencies() = fragmentComponent.inject(this)

    private var closeDialog: AppCompatDialog? = null

    private var stopByUser = false

    val panelObserver: Observer<Int> = Observer { newState ->
        // Update the UI, in this case, a TextView.
        when (newState) {
            BottomSheetBehavior.STATE_HIDDEN -> {
                if(stopByUser) closeDialog?.show()
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                setSmallPlayer(true)
            }
            else -> {
                setSmallPlayer(false)
            }
        }
    }

    fun setSmallPlayer(visible: Boolean) {
        if (!visible) binding.topContainer.imageView.setImageResource(android.R.color.transparent)
        else binding.topContainer.imageView.setImageResource(if (App.isDarkMode()) R.drawable.ic_close_dm else R.drawable.ic_close)
        binding.topContainer.dragView.visibility = if (!visible) View.VISIBLE else View.GONE
        binding.topContainer.imgPreview.visibility = if (!visible) View.GONE else View.VISIBLE
        binding.topContainer.previewName.visibility = if (!visible) View.GONE else View.VISIBLE
        if (!visible)
            binding.topContainer.constraint.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.trans, null))
        else
            binding.topContainer.constraint.setBackgroundColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.touch_header_dm else R.color.colorWhite, null))
    }


    override fun onResume() {
        super.onResume()
        (activity as HomeActivity).run {
            playerPreviewState.observe(this, panelObserver)
        }
    }


    override fun onPause() {
        super.onPause()
        (activity as HomeActivity).run {
            playerPreviewState.removeObserver(panelObserver)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PlayerFragmentBinding.inflate(inflater, container!!, false)

        binding.slidingPanel.setBackgroundColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.touch_body else R.color.colorWhite, null))
        binding.topContainer.constraint.setBackgroundColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.touch_header_dm else R.color.colorWhite, null))

        binding.topContainer.previewName.setTextColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.colorWhite else R.color.kiviDark, null))
        binding.renderElapsed.setTextColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.colorWhite else R.color.kiviDark, null))
        binding.renderRemaining.setTextColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.colorWhite else R.color.kiviDark, null))

        if (binding.renderProgress != null) {
            binding.renderProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {


                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {

                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    if (viewModel.totalTimeMls > 500) //1/2 second - need to do some fix - when we don't have connection
                        viewModel.seekTo(seekBar.progress)
                }
            })

            binding.topContainer.constraint.setOnTouchListener { _, event ->
                mDetector.onTouchEvent(event)
            }

            mDetector = GestureDetectorCompat(context, MyGestureListener())

        }

        binding.topContainer.imageView.setOnClickListener(View.OnClickListener {
            stopByUser = true
            (activity as HomeActivity).hideSlidingPanel()
        })


        binding.renderPlay.setOnClickListener { view ->
            if (view.tag == R.drawable.ic_image_play) {
                viewModel.play()
            } else {
                viewModel.pause()
            }
        }

        closeDialog = AlertDialog.Builder(context!!, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                .setMessage(R.string.close_recommendation)
                .setNegativeButton(R.string.cancel)
                { dialog, _ ->
                    (activity as HomeActivity).showFullPreviewPanel()
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.ok)
                { dialog, _ ->
                    stopByUser = true
                    showPlayerStop()
                    viewModel.closePlayer()
                }
                .create()

        return binding.root
    }


    inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onDown(event: MotionEvent): Boolean {
            Timber.e("onDown: $event")
            return true
        }

        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
            (activity as HomeActivity).showFullPreviewPanel()
            return true
        }

        override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        stopByUser = true
                        (activity as HomeActivity).hideSlidingPanel()
                        result = true
                    }
                }
            } catch (e: Exception) {
                Timber.e(this.javaClass.simpleName, "gesture listener exception: ${e.message}")
            }

            return result
        }
    }

    private val playerObserver = Observer<PlayerViewModel.ProgressEvent> {
        if (it != null) {
            showProgress(it.condition, it.progress, it.passedTime, it.leftTime)
        }
    }

    private val previewObserver = Observer<PlayerViewModel.PreviewEvent> {
        if (it != null) {
            showPreview(it.imgUrl, it.title ?: "no title")
        }
    }

    private val recsObserver = Observer<Recommendation> {
        //means user launched from phone
        if (it != null && it.imageUrl != null && it.name != null) {
            showPreview(it.imageUrl, it.name)
        }
    }


    fun showProgress(condition: Int, progress: Int, timePassed: String, timeLeft: String) {
        when (condition) {
            viewModel.PAUSED -> {
                binding.renderPlay.setImageResource(R.drawable.ic_image_play)
                binding.renderPlay.tag = R.drawable.ic_image_play
            }
            viewModel.PLAYING -> {
                showSeekTo(timePassed, timeLeft, progress)
                binding.renderPlay.setImageResource(R.drawable.ic_image_pause)
                binding.renderPlay.tag = R.drawable.ic_image_pause
            }
            viewModel.STOPPED -> {
                stopByUser = false
                showPlayerStop()
            }
            viewModel.SEEK_TO -> {
                showSeekTo(timePassed, timeLeft, progress)
                if (binding.renderPlay.tag == R.drawable.ic_image_play) {
                    binding.renderPlay.setImageResource(R.drawable.ic_image_pause)
                    binding.renderPlay.tag = R.drawable.ic_image_pause
                }
            }
            viewModel.ERROR -> {
                stopByUser = false
                showPlayerStop()
            }
        }
    }

    private fun showSeekTo(timePassed: String, timeLeft: String, progress: Int) {
        binding.renderElapsed.text = timePassed
        binding.renderRemaining.text = timeLeft
        binding.renderProgress.progress = progress
        binding.renderSlideshowProgress.progress = progress
        binding.renderSlideshowProgressBackground.progress = progress
    }

    private fun showPlayerStop() {
        binding.renderPlay.setImageResource(R.drawable.ic_image_play)
        binding.renderPlay.tag = R.drawable.ic_image_play
        (activity as HomeActivity).hideSlidingPanel()
        binding.renderElapsed.text = "00:00:00"
        binding.renderRemaining.text = "00:00:00"
    }


    fun showPreview(url: String, title: String) {
        (activity as HomeActivity).run {
            showFullPreviewPanel()
            hideTouchPad()
        }

        Glide.with(this).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.renderPreview)

        Glide.with(this).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.topContainer.imgPreview)

        binding.topContainer.previewName.text = title
        binding.renderPlay.setImageResource(R.drawable.ic_image_pause)
        binding.renderPlay.tag = R.drawable.ic_image_pause
        showSeekTo("0".getIviPreviewDuration(),"0".getIviPreviewDuration(),0)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PlayerViewModel::class.java)
        viewModel.run {
            launchRecEvent.observe(viewLifecycleOwner, recsObserver)
            previewEvent.observe(viewLifecycleOwner, previewObserver)
            progressEvent.observe(viewLifecycleOwner, playerObserver)
        }
    }
}