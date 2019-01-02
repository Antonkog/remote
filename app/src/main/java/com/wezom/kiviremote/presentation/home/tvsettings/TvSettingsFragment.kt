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
import com.wezom.kiviremote.net.model.AspectAvailable
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.base.TvKeysFragment
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
import com.wezom.kiviremote.presentation.home.tvsettings.TvSettingsViewModel
import com.wezom.kiviremote.presentation.home.tvsettings.driver_set.HDRValues
import com.wezom.kiviremote.presentation.home.tvsettings.driver_set.PictureMode
import com.wezom.kiviremote.presentation.home.tvsettings.driver_set.Ratio
import com.wezom.kiviremote.presentation.home.tvsettings.driver_set.TemperatureValues
import com.wezom.kiviremote.views.AspectHeaderView
import com.wezom.kiviremote.views.HorizontalSwitchView
import timber.log.Timber
import javax.inject.Inject


class TvSettingsFragment : TvKeysFragment(), SeekBar.OnSeekBarChangeListener, HorizontalSwitchView.OnSwitchListener, AspectHeaderView.OnSwitchListener {


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
        binding.backlight.seekBar.tag = AspectMessage.ASPECT_VALUE.BACKLIGHT
        binding.saturation.seekBar.setOnSeekBarChangeListener(this)
        binding.saturation.seekBar.tag = AspectMessage.ASPECT_VALUE.SATURATION
        binding.sharpness.seekBar.setOnSeekBarChangeListener(this)
        binding.sharpness.seekBar.tag = AspectMessage.ASPECT_VALUE.SHARPNESS
        binding.contrast.seekBar.setOnSeekBarChangeListener(this)
        binding.contrast.seekBar.tag = AspectMessage.ASPECT_VALUE.CONTRAST
        binding.brightness.seekBar.setOnSeekBarChangeListener(this)
        binding.brightness.seekBar.tag = AspectMessage.ASPECT_VALUE.BRIGHTNESS

        binding.hdr.setOnSwitchListener(this)
        binding.temperature.setOnSwitchListener(this)
        binding.ratio.setOnSwitchListener(this)
        binding.aspectHeader.setOnSwitchListener(this)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TvSettingsViewModel::class.java)
        setupConstraintMagic()
        setTvButtons(viewModel, binding.menu, binding.back, binding.home)
        binding.input.setOnClickListener { _ -> viewModel.goToInputSettings() }
        (activity as HomeActivity).hideSlidingPanel()
    }

    fun onBackPressed() {
        viewModel.goBack()
    }

    override fun onResume() {
        syncPicSettings(AspectHolder.message, AspectHolder.availableSettings)
        super.onResume()
    }

    private fun syncPicSettings(message: AspectMessage?, available: AspectAvailable?) {
        if (available != null)
            for (x in available?.settings) {
                when (x.key) {

                    AspectAvailable.VALUE_TYPE.HDR.name -> {
                        binding.hdr.setVariants(AspectMessage.ASPECT_VALUE.HDR, HDRValues.getResList(x.value))
                    }

                    AspectAvailable.VALUE_TYPE.PICTUREMODE.name -> {
                        binding.aspectHeader.setVariants(AspectMessage.ASPECT_VALUE.PICTUREMODE, PictureMode.getResList(x.value))
                    }

                    AspectAvailable.VALUE_TYPE.RATIO.name -> {
                        binding.ratio.setVariants(AspectMessage.ASPECT_VALUE.VIDEOARCTYPE, Ratio.getResList(x.value))

                    }

                    AspectAvailable.VALUE_TYPE.TEMPERATUREVALUES.name -> {
                        binding.temperature.setVariants(AspectMessage.ASPECT_VALUE.TEMPERATURE, TemperatureValues.getResList(x.value))
                    }
                }

            }


        Timber.i("got new aspect, sync: " + message?.toString())

        if (message?.settings != null) {
            for ((key, value) in message?.settings) {
                println("$key = $value")
                when (key) {
                    AspectMessage.ASPECT_VALUE.BRIGHTNESS.name -> binding.brightness.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.CONTRAST.name -> binding.contrast.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.BACKLIGHT.name -> binding.backlight.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.SATURATION.name -> binding.saturation.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.SHARPNESS.name -> binding.sharpness.seekBar.progress = value


                    AspectMessage.ASPECT_VALUE.HDR.name -> {
                        var hdr = HDRValues.getByID(value)?.stringResourceID
                        if (hdr != null) binding.hdr.variant.text = resources.getString(hdr)
                    }

                    AspectMessage.ASPECT_VALUE.TEMPERATURE.name -> {
                        var temperature = TemperatureValues.getByID(value)?.stringResourceID
                        if (temperature != null) binding.temperature.variant.text = resources.getString(temperature)
                    }

                    AspectMessage.ASPECT_VALUE.VIDEOARCTYPE.name -> {
                        var ratio = Ratio.getByID(value)?.stringResourceID
                        if (ratio != null) binding.ratio.variant.text = resources.getString(ratio)
                    }

                    AspectMessage.ASPECT_VALUE.PICTUREMODE.name -> {
                        var pictureMode = PictureMode.getByID(value)?.stringResourceID
                        if (pictureMode != null) binding.aspectHeader.row.text = resources.getString(pictureMode)
                    }
                }
            }
        }
    }

    override fun onSwitch(mode: AspectMessage.ASPECT_VALUE?, resId: Int) {
        viewModel?.let {
            var progress = -1;

            if (resId != null && mode!= null) {
                when (mode) {
                    AspectMessage.ASPECT_VALUE.HDR -> progress = HDRValues.getIdByResID(resId)
                    AspectMessage.ASPECT_VALUE.TEMPERATURE -> progress = TemperatureValues.getIdByResID(resId)
                    AspectMessage.ASPECT_VALUE.VIDEOARCTYPE -> progress = Ratio.getIdByResID(resId)
                    AspectMessage.ASPECT_VALUE.PICTUREMODE -> progress = PictureMode.getIdByResID(resId)
                    else -> Timber.e(" AspectMessage.ASPECT_VALUE not set")
                }
                if (progress != -1) {
                    it.sendAspectSingleChangeEvent(mode, progress);
                } else {
                    Timber.e(" error in aspect view resId == null or mode == null" )
                }
            } else {
                Timber.e(" error in aspect view implementation or value not set 2")
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
                if (seekBar != null && seekBar.tag != null) {
                    try {
                         it.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.valueOf(seekBar.tag.toString()), seekBar?.progress)
                    } catch (e: IllegalArgumentException) {
                        Timber.e("Error in aspect values parsing : onStopTrackingTouch", e)
                    }
                } else {
                    Timber.e("error in seekbar implementation")
                }
            }
        }
    }


    private fun setupConstraintMagic() {
        mainConstraintSet.clone(binding.tvSettingsContainer)
        mainEditConstraintSet.clone(activity, R.layout.tv_settings_fragment)
    }
}