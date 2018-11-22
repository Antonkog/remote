package com.wezom.kiviremote.presentation.home.recentdevices

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.wezom.kiviremote.R
import com.wezom.kiviremote.databinding.TvSettingsFragmentBinding
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
import com.wezom.kiviremote.presentation.home.tvsettings.TvSettingsViewModel
import timber.log.Timber
import javax.inject.Inject


class TvSettingsFragment : BaseFragment(), SeekBar.OnSeekBarChangeListener {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: TvSettingsViewModel

    private lateinit var binding: TvSettingsFragmentBinding

    private val mainConstraintSet = ConstraintSet()

    private val mainEditConstraintSet = ConstraintSet()

    override fun injectDependencies() = fragmentComponent.inject(this)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = TvSettingsFragmentBinding.inflate(inflater, container!!, false)
        binding.seekBrightness.setOnSeekBarChangeListener(this)
        binding.seekBacklight.setOnSeekBarChangeListener(this)
        binding.seekSaturation.setOnSeekBarChangeListener(this)
        binding.seekContrast.setOnSeekBarChangeListener(this)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TvSettingsViewModel::class.java)
        setupConstraintMagic()
        (activity as HomeActivity).hideSlidingPanel()
    }

    override fun onResume() {
        syncPicSettings(AspectHolder.message)
        super.onResume()
    }

    private fun syncPicSettings(message: AspectMessage?) {
        Timber.i("got new aspect, sync: " + message?.toString())
        if (message?.settings != null) {
            for ((key, value) in message.settings) {
                println("$key = $value")
                when (key) {
                    AspectMessage.ASPECT_VALUE.BRIGHTNESS.name -> binding.seekBrightness.progress = value
                    AspectMessage.ASPECT_VALUE.CONTRAST.name -> binding.seekContrast.progress = value
                    AspectMessage.ASPECT_VALUE.BACKLIGHT.name -> binding.seekBacklight.progress = value
                    AspectMessage.ASPECT_VALUE.SATURATION.name -> binding.seekSaturation.progress = value
                    AspectMessage.ASPECT_VALUE.SHARPNESS.name -> binding.seekSharpness.progress = value
                }
            }
        }
    }


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        seekBar?.id.let {
            viewModel?.let {
                val msg = AspectMessage.AspectMsgBuilder(AspectMessage.ASPECT_VALUE.BRIGHTNESS, binding.seekBrightness.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.BACKLIGHT, binding.seekBacklight.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.CONTRAST, binding.seekContrast.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.SATURATION, binding.seekSaturation.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.SHARPNESS, binding.seekSharpness.progress)
                        .buildAspect()

                AspectHolder.message = msg
                it.sendAspectChangeEvent(msg);
            }
        }
    }


    private fun setupConstraintMagic() {
        mainConstraintSet.clone(binding.tvSettingsContainer)
        mainEditConstraintSet.clone(activity, R.layout.tv_settings_fragment)
    }
}