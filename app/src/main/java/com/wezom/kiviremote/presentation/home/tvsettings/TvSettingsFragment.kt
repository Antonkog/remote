package com.wezom.kiviremote.presentation.home.recentdevices

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import com.wezom.kiviremote.App
import com.wezom.kiviremote.R
import com.wezom.kiviremote.bus.GotAspectEvent
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.databinding.TvSettingsFragmentBinding
import com.wezom.kiviremote.net.model.AspectAvailable
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
import com.wezom.kiviremote.presentation.home.tvsettings.TvSettingsViewModel
import com.wezom.kiviremote.presentation.home.tvsettings.driver_set.*
import com.wezom.kiviremote.views.AspectHeaderView
import com.wezom.kiviremote.views.HorizontalSwitchView
import timber.log.Timber
import javax.inject.Inject


class TvSettingsFragment : BaseFragment(), SeekBar.OnSeekBarChangeListener, HorizontalSwitchView.OnSwitchListener, AspectHeaderView.OnSwitchListener {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: TvSettingsViewModel

    private lateinit var binding: TvSettingsFragmentBinding
    private var sekBarEnabled = false;
    private var manufacture = Constants.NO_VALUE;

    override fun injectDependencies() = fragmentComponent.inject(this)


    private val aspectObserver = Observer<GotAspectEvent?> {
        Timber.i("set aspect from observable")
        syncPicSettings(it)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = TvSettingsFragmentBinding.inflate(inflater, container!!, false)
        binding.backLight.seekBar.setOnSeekBarChangeListener(this)
        binding.backLight.seekBar.tag = AspectMessage.ASPECT_VALUE.BACKLIGHT
        binding.saturation.seekBar.setOnSeekBarChangeListener(this)
        binding.saturation.seekBar.tag = AspectMessage.ASPECT_VALUE.SATURATION
        binding.sharpness.seekBar.setOnSeekBarChangeListener(this)
        binding.sharpness.seekBar.tag = AspectMessage.ASPECT_VALUE.SHARPNESS
        binding.contrast.seekBar.setOnSeekBarChangeListener(this)
        binding.contrast.seekBar.tag = AspectMessage.ASPECT_VALUE.CONTRAST
        binding.brightness.seekBar.setOnSeekBarChangeListener(this)
        binding.brightness.seekBar.tag = AspectMessage.ASPECT_VALUE.BRIGHTNESS

        binding.seekBars.setOnClickListener {
            if (!sekBarEnabled) Toast.makeText(context, resources.getString(R.string.toastPic), Toast.LENGTH_SHORT).show()
        }

        binding.temperature.setOnSwitchListener(this)
        binding.ratio.setOnSwitchListener(this)
        binding.aspectHeader.setOnSwitchListener(this)
        binding.darkMode?.arrow?.setOnClickListener {
            viewModel.restartColorScheme(activity)}
        binding.darkMode?.variant?.setOnClickListener {
            viewModel.restartColorScheme(activity)}
        binding.darkMode?.variant?.text = "${ if(App.isDarkMode())  resources.getString(R.string.on) else  resources.getString(R.string.off)}"
        binding.darkMode?.name?.text = resources.getString(R.string.dark_mode)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TvSettingsViewModel::class.java)


        if (App.isDarkMode())
            binding.tvSettingsLayout.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_gradient_black, null)
        else
            binding.tvSettingsLayout.background = ResourcesCompat.getDrawable(resources, R.drawable.shape_gradient_white, null)

        viewModel.aspectChange.observe(this, aspectObserver)

        if (!AspectHolder.hasAspectSettings())
            viewModel.requestAspect()
        else {
            viewModel?.aspectChange.postValue(GotAspectEvent(AspectHolder.message, AspectHolder.availableSettings, AspectHolder.initialMsg))
        }

        (activity as HomeActivity).run {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    override fun onResume() {
        if (AspectHolder.message != null && AspectHolder.availableSettings != null) {
            syncPicSettings(GotAspectEvent(AspectHolder.message, AspectHolder.availableSettings, null))
        } else {
            Timber.i(" requesting aspect")
            viewModel.requestAspect()
        }
        super.onResume()
    }

    private fun syncPicSettings(gotAspectEvent: GotAspectEvent?) {
        manufacture = gotAspectEvent?.getManufacture() ?: Constants.NO_VALUE //workaround for server verstion <=18 todo: use driverValues
        val settings = gotAspectEvent?.available?.settings
        if (settings != null)
            for (x in settings) {
                when (x.key) {
                    AspectAvailable.VALUE_TYPE.PICTUREMODE.name -> {
                        when (manufacture) {
                            Constants.SERV_MSTAR ->
                                binding.aspectHeader.setVariants(AspectMessage.ASPECT_VALUE.PICTUREMODE, PictureMode.getResList(x.value))
                            Constants.SERV_REALTEK ->
                                binding.aspectHeader.setVariants(AspectMessage.ASPECT_VALUE.PICTUREMODE, PictureModeRealtek.getResList(x.value))
                        }
                    }
                    AspectAvailable.VALUE_TYPE.RATIO.name -> {
                        when (manufacture) {
                            Constants.SERV_MSTAR ->
                                binding.ratio.setVariants(AspectMessage.ASPECT_VALUE.VIDEOARCTYPE, Ratio.getResList(x.value))
                            Constants.SERV_REALTEK ->
                                binding.ratio.setVariants(AspectMessage.ASPECT_VALUE.VIDEOARCTYPE, RatioRealtek.getResList(x.value))
                        }
                    }
                    AspectAvailable.VALUE_TYPE.TEMPERATUREVALUES.name -> {
                        binding.temperature.setVariants(AspectMessage.ASPECT_VALUE.TEMPERATURE, TemperatureValues.getResList(x.value))
                    }
                }
            }

        val picset = gotAspectEvent?.msg?.settings
        if (picset != null) {
            for ((key, value) in picset) {
                when (key) {
                    AspectMessage.ASPECT_VALUE.BRIGHTNESS.name -> binding.brightness.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.CONTRAST.name -> binding.contrast.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.BACKLIGHT.name -> binding.backLight.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.SATURATION.name -> binding.saturation.seekBar.progress = value
                    AspectMessage.ASPECT_VALUE.SHARPNESS.name -> binding.sharpness.seekBar.progress = value
                    // no HDR !!! cant get cant set on tv not available with current hardware
                    AspectMessage.ASPECT_VALUE.TEMPERATURE.name -> {
                        var temperature = TemperatureValues.getByID(value)?.stringResourceID
                        if (temperature != null) binding.temperature.variant.text = resources.getString(temperature)
                    }

                    AspectMessage.ASPECT_VALUE.VIDEOARCTYPE.name -> {
                        when (manufacture) {
                            Constants.SERV_MSTAR -> {
                                var ratio = Ratio.getByID(value)?.stringResourceID
                                if (ratio != null) binding.ratio.variant.text = resources.getString(ratio)
                            }
                            Constants.SERV_REALTEK -> {
                                var ratio = RatioRealtek.getByID(value)?.stringResourceID
                                if (ratio != null) binding.ratio.variant.text = resources.getString(ratio)
                            }
                        }
                    }

                    AspectMessage.ASPECT_VALUE.PICTUREMODE.name -> {
                        when (manufacture) {
                            Constants.SERV_MSTAR -> {
                                var pictureMode = PictureMode.getByID(value)?.stringResourceID
                                if (pictureMode != null) {
                                    binding.aspectHeader.row.text = resources.getString(pictureMode)
                                    enableSeekBars(PictureMode.PICTURE_MODE_USER == PictureMode.getByID(value))
                                }
                            }
                            Constants.SERV_REALTEK -> {
                                var pictureMode = PictureModeRealtek.getByID(value)?.stringResourceID
                                if (pictureMode != null) {
                                    binding.aspectHeader.row.text = resources.getString(pictureMode)
                                    enableSeekBars(PictureModeRealtek.PICTURE_MODE_USER == PictureModeRealtek.getByID(value))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun enableSeekBars(enabled: Boolean) {
        sekBarEnabled = enabled
        binding.backLight.seekBar.isEnabled = enabled
        binding.saturation.seekBar.isEnabled = enabled
        binding.sharpness.seekBar.isEnabled = enabled
        binding.contrast.seekBar.isEnabled = enabled
        binding.brightness.seekBar.isEnabled = enabled
    }

    override fun onSwitch(mode: AspectMessage.ASPECT_VALUE?, resId: Int) {
        viewModel?.let {
            var progress = -1;

            if (mode != null) {
                when (manufacture) {
                    Constants.SERV_MSTAR -> {
                        when (mode) {
//                            AspectMessage.ASPECT_VALUE.HDR -> progress = HDRValues.getIdByResID(resId) // not available with current hardware
                            AspectMessage.ASPECT_VALUE.TEMPERATURE -> progress = TemperatureValues.getIdByResID(resId)
                            AspectMessage.ASPECT_VALUE.VIDEOARCTYPE -> progress = Ratio.getIdByResID(resId)
                            AspectMessage.ASPECT_VALUE.PICTUREMODE -> progress = PictureMode.getIdByResID(resId)
                            else -> Timber.e(" AspectMessage.ASPECT_VALUE not set")
                        }
                    }

                    Constants.SERV_REALTEK -> {
                        when (mode) {
//                            AspectMessage.ASPECT_VALUE.HDR -> progress = HDRValues.getIdByResID(resId) // not available with current hardware
                            AspectMessage.ASPECT_VALUE.TEMPERATURE -> progress = TemperatureValues.getIdByResID(resId)
                            AspectMessage.ASPECT_VALUE.VIDEOARCTYPE -> progress = RatioRealtek.getIdByResID(resId)
                            AspectMessage.ASPECT_VALUE.PICTUREMODE -> progress = PictureModeRealtek.getIdByResID(resId)
                            else -> Timber.e(" AspectMessage.ASPECT_VALUE not set")
                        }
                    }
                    else -> viewModel.goBack()
                }
                if (progress != Constants.NO_VALUE) {
                    it.sendAspectSingleChangeEvent(mode, progress);
                } else {
                    Timber.e(" tr mode == null")
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
                        it.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.valueOf(seekBar.tag.toString()), seekBar.progress)
                    } catch (e: IllegalArgumentException) {
                        Timber.e("Error in aspect values parsing : onStopTrackingTouch", e)
                    }
                } else {
                    Timber.e("error in seekbar implementation")
                }
            }
        }
    }
}