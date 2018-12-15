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

        binding.hdr.setVariants(AspectMessage.ASPECT_VALUE.HDR,
                listOf(
                        HDRValues.HDR_OPEN_LEVEL_AUTO.stringResourceID,
                        HDRValues.HDR_OPEN_LEVEL_LOW.stringResourceID,
                        HDRValues.HDR_OPEN_LEVEL_MIDDLE.stringResourceID,
                        HDRValues.HDR_OPEN_LEVEL_HIGH.stringResourceID,
                        HDRValues.HDR_OPEN_LEVEL_OFF.stringResourceID))

        binding.temperature.setVariants( AspectMessage.ASPECT_VALUE.TEMPERATURE,
                listOf(TemperatureValues.COLOR_TEMP_COOL.stringResourceID,
                        TemperatureValues.COLOR_TEMP_COOLER.stringResourceID,
                        TemperatureValues.COLOR_TEMP_NATURE.stringResourceID,
                        TemperatureValues.COLOR_TEMP_WARM.stringResourceID,
                        TemperatureValues.COLOR_TEMP_WARMER.stringResourceID)
        )

        binding.ratio.setVariants(AspectMessage.ASPECT_VALUE.VIDEOARCTYPE,
                listOf(Ratio.VIDEO_ARC_AUTO.string,
                        Ratio.VIDEO_ARC_16x9.string,
                        Ratio.VIDEO_ARC_4x3.string,
                        Ratio.VIDEO_ARC_DEFAULT.string
                )
        )

        binding.aspectHeader.setVariants(AspectMessage.ASPECT_VALUE.PICTUREMODE,
                listOf(
                        R.string.auto,
                        R.string.user,
                        R.string.soft,
                        R.string.economy,
                        R.string.normal,
                        R.string.movie,
                        R.string.sport,
                        R.string.game,
                        R.string.vivid

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
        syncPicSettings(AspectHolder.message, AspectHolder.availableSettings)
        super.onResume()
    }

    private fun syncPicSettings(message: AspectMessage?, available: AspectAvailable?) {
       available.let {
           for(x in it!!.settings){
               when(x.key){
                   AspectAvailable.VALUE_TYPE.INPUT_PORT.name -> {
                       for (i in x.value){

                       }

                   }

                   AspectAvailable.VALUE_TYPE.PICTUREMODE.name-> {

                   }

                   AspectAvailable.VALUE_TYPE.RATIO.name -> {

                   }

                   AspectAvailable.VALUE_TYPE.TEMPERATUREVALUES.name -> {

                   }
               }

           }
       }
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
                        if (temperature != null) binding.temperature.variant.text = resources.getString(temperature)
                    }

                    AspectMessage.ASPECT_VALUE.VIDEOARCTYPE.name -> {
                        var ratio = Ratio.getByID(value)?.string
                        if (ratio != null) binding.ratio.variant.text = resources.getString(ratio)
                    }
                }
            }
        }
    }

    override fun onSwitch(mode: AspectMessage.ASPECT_VALUE?, resId: Int) {
        viewModel?.let {
            val builder = AspectMessage.AspectMsgBuilder()

            if (resId != null) {
                when (mode) {
                    AspectMessage.ASPECT_VALUE.HDR -> builder.addValue(mode, HDRValues.getIdByResID(resId))
                    AspectMessage.ASPECT_VALUE.TEMPERATURE -> builder.addValue(mode, TemperatureValues.getIdByResID(resId))
                    AspectMessage.ASPECT_VALUE.VIDEOARCTYPE -> builder.addValue(mode, Ratio.getIdByResID(resId))
                    AspectMessage.ASPECT_VALUE.PICTUREMODE -> builder.addValue(mode, PictureMode.getIdByResID(resId))
                    else -> Timber.e(" AspectMessage.ASPECT_VALUE not set")
                }

            } else {
                Timber.e(" error in aspect view implementation or value not set")
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
                if(seekBar!=null && seekBar.tag!=null){
                    val builder = AspectMessage.AspectMsgBuilder(AspectMessage.ASPECT_VALUE.valueOf(tag!!), seekBar!!.progress)
                    it.sendAspectChangeEvent(builder.buildAspect())
                }else{
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