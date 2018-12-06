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
import com.wezom.kiviremote.presentation.home.tvsettings.driver_set.HDRValues
import com.wezom.kiviremote.presentation.home.tvsettings.driver_set.PictureMode
import com.wezom.kiviremote.presentation.home.tvsettings.driver_set.Ratio
import com.wezom.kiviremote.presentation.home.tvsettings.driver_set.TemperatureValues
import com.wezom.kiviremote.views.AspectHeaderView
import com.wezom.kiviremote.views.HorizontalSwitchView
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class TvSettingsFragment : BaseFragment(), SeekBar.OnSeekBarChangeListener, HorizontalSwitchView.OnSwitchListener, AspectHeaderView.OnSwitchListener {


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
        binding.ratio.setOnSwitchListener(this)
        binding.aspectHeader.setOnSwitchListener(this)

        binding.hdr.setVariants(
                LinkedList(listOf(
                        resources.getString(HDRValues.HDR_OPEN_LEVEL_AUTO.stringResourceID),
                        resources.getString(HDRValues.HDR_OPEN_LEVEL_LOW.stringResourceID),
                        resources.getString(HDRValues.HDR_OPEN_LEVEL_MIDDLE.stringResourceID),
                        resources.getString(HDRValues.HDR_OPEN_LEVEL_HIGH.stringResourceID),
                        resources.getString(HDRValues.HDR_OPEN_LEVEL_OFF.stringResourceID)).distinct()))


        binding.temperature.setVariants(
                LinkedList(listOf(
                        resources.getString(TemperatureValues.COLOR_TEMP_COOL.stringResourceID),
                        resources.getString(TemperatureValues.COLOR_TEMP_COOLER.stringResourceID),
                        resources.getString(TemperatureValues.COLOR_TEMP_NATURE.stringResourceID),
                        resources.getString(TemperatureValues.COLOR_TEMP_WARM.stringResourceID),
                        resources.getString(TemperatureValues.COLOR_TEMP_WARMER.stringResourceID)).distinct())
        )

        binding.ratio.setVariants(
                LinkedList(listOf(resources.getString(Ratio.VIDEO_ARC_AUTO.string),
                        resources.getString(Ratio.VIDEO_ARC_16x9.string),
                        resources.getString(Ratio.VIDEO_ARC_4x3.string),
                        resources.getString(Ratio.VIDEO_ARC_DEFAULT.string)

                ).distinct())
        )

        binding.aspectHeader.setVariants(
                LinkedList(listOf(
                        resources.getString(R.string.auto),
                        resources.getString(R.string.user),
                        resources.getString(R.string.soft),
                        resources.getString(R.string.economy),
                        resources.getString(R.string.normal),
                        resources.getString(R.string.movie),
                        resources.getString(R.string.sport),
                        resources.getString(R.string.game),
                        resources.getString(R.string.vivid)

                ).distinct())
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


                    AspectMessage.ASPECT_VALUE.HDR.name -> {
                        var hdr = HDRValues.getByID(value)?.stringResourceID
                        if (hdr != null) binding.hdr.variant.text = resources.getString(hdr)

                    }

                    AspectMessage.ASPECT_VALUE.TEMPERATURE.name -> {
                        var temperature = TemperatureValues.getByID(value)?.stringResourceID
                        if (temperature != null) binding.temperature.name.text = resources.getString(temperature)
                    }

                    AspectMessage.ASPECT_VALUE.VIDEOARCTYPE.name -> {
                        var ratio = Ratio.getByID(value)?.string
                        if (ratio != null) binding.ratio.name.text = resources.getString(ratio)
                    }
                }
            }
        }
    }

    override fun onSwitch(s: String) {

        viewModel?.let {
            val builder = AspectMessage.AspectMsgBuilder()

            if (HDRValues.getIdByString(binding.hdr.variant.text, context) != -1) {
                builder.addValue(AspectMessage.ASPECT_VALUE.HDR, HDRValues.getIdByString(binding.hdr.variant.text, context))
            }

            if (TemperatureValues.getIdByString(binding.temperature.variant.text, context) != -1) {
                builder.addValue(AspectMessage.ASPECT_VALUE.TEMPERATURE, TemperatureValues.getIdByString(binding.temperature.variant.text, context))
            }

            if (Ratio.getIdByString(binding.ratio.variant.text, context) != -1) {
                builder.addValue(AspectMessage.ASPECT_VALUE.VIDEOARCTYPE, Ratio.getIdByString(binding.ratio.variant.text, context))
            }

            if (PictureMode.getIdByString(binding.aspectHeader.row.text, context) != -1) {
                builder.addValue(AspectMessage.ASPECT_VALUE.PICTUREMODE, PictureMode.getIdByString(binding.aspectHeader.row.text, context))
            }

            val msg = builder.buildAspect()
            it.sendAspectChangeEvent(msg)
            Timber.i(builder.buildAspect().toString())

        }

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        seekBar?.id.let {
            viewModel?.let {
                val builder = AspectMessage.AspectMsgBuilder(AspectMessage.ASPECT_VALUE.BRIGHTNESS, binding.brightness.seekBar.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.BACKLIGHT, binding.backlight.seekBar.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.CONTRAST, binding.contrast.seekBar.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.SATURATION, binding.saturation.seekBar.progress)
                        .addValue(AspectMessage.ASPECT_VALUE.SHARPNESS, binding.sharpness.seekBar.progress)

                it.sendAspectChangeEvent(builder.buildAspect())
            }
        }
    }


    private fun setupConstraintMagic() {
        mainConstraintSet.clone(binding.tvSettingsContainer)
        mainEditConstraintSet.clone(activity, R.layout.tv_settings_fragment)
    }
}