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
import com.wezom.kiviremote.views.HorizontalSwitchView
import timber.log.Timber
import javax.inject.Inject


class TvSettingsFragment : BaseFragment(), SeekBar.OnSeekBarChangeListener, HorizontalSwitchView.OnSwitchListener {


    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: TvSettingsViewModel

    private lateinit var binding: TvSettingsFragmentBinding

    private val mainConstraintSet = ConstraintSet()

    private val mainEditConstraintSet = ConstraintSet()

    override fun injectDependencies() = fragmentComponent.inject(this)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = TvSettingsFragmentBinding.inflate(inflater, container!!, false)
        binding.backlight.seekBar.setOnSeekBarChangeListener(this)
        binding.saturation.seekBar.setOnSeekBarChangeListener(this)
        binding.sharpness.seekBar.setOnSeekBarChangeListener(this)
        binding.contrast.seekBar.setOnSeekBarChangeListener(this)
        binding.brightness.seekBar.setOnSeekBarChangeListener(this)

        binding.hdr.setOnSwitchListener(this)
        binding.temperature.setOnSwitchListener(this)
        binding.radio.setOnSwitchListener(this)

        binding.hdr.setVariants(hashMapOf(
                resources.getString(R.string.off) to 1,
                resources.getString(R.string.auto) to 2,
                resources.getString(R.string.low) to 3,
                resources.getString(R.string.middle) to 4,
                resources.getString(R.string.high) to 5
        )
        )
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
                    AspectMessage.ASPECT_VALUE.BRIGHTNESS.name -> binding.brightness.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.CONTRAST.name -> binding.contrast.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.BACKLIGHT.name -> binding.saturation.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.SATURATION.name -> binding.sharpness.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.SHARPNESS.name -> binding.backlight.seekBar.progress = value
                }
            }
        }
    }

    override fun onSwitch(currentEntry: Map.Entry<String, Any>?) {

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        seekBar?.id.let {
            viewModel?.let {
                val msg = AspectMessage.AspectMsgBuilder(AspectMessage.ASPECT_VALUE.BRIGHTNESS, binding.brightness.seekBar.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.BACKLIGHT, binding.backlight.seekBar.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.CONTRAST, binding.contrast.seekBar.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.SATURATION, binding.saturation.seekBar.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.SHARPNESS, binding.sharpness.seekBar.progress)
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