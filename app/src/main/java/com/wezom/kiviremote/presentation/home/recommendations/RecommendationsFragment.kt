package com.wezom.kiviremote.presentation.home.recommendations

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.wezom.kiviremote.bus.SendActionEvent
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.KiviCache
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.databinding.RecommendationsFragmentBinding
import com.wezom.kiviremote.net.model.*
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
import timber.log.Timber
import javax.inject.Inject


class RecommendationsFragment : BaseFragment(), HorizontalCVContract.HorizontalCVListener {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    @Inject
    lateinit var cache: KiviCache

    private lateinit var viewModel: RecommendationsViewModel
    private lateinit var binding: RecommendationsFragmentBinding

    private lateinit var adapterPorts: RecommendationsAdapter
    private lateinit var adapterApps: RecommendationsAdapter
    private lateinit var adapterChannels: RecommendationsAdapter
    private lateinit var adapterRecommend: RecommendationsAdapter


    private val recommendationsObserver = Observer<List<Comparable<Recommendation>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterRecommend.swapData(it)
        } ?: Timber.e("TYPE_RECOMMENDATIONS empty")
    }

    private val channelsObserver = Observer<List<Comparable<Channel>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterChannels.swapData(it)
        } ?: Timber.e("TYPE_RECOMMENDATIONS empty")
    }

    private val appsObserver = Observer<List<Comparable<ServerAppInfo>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterApps.swapData(it)
        } ?: Timber.e("TYPE_RECOMMENDATIONS empty")
    }

    private val inputPortObserver = Observer<List<Comparable<Input>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterPorts.swapData(it)
        } ?: Timber.e("TYPE_INPUTS empty")
    }


    override fun onInputChosen(item: Input, position: Int) {
        Toast.makeText(context, "port chosen " + item.name, Toast.LENGTH_SHORT).show()
        onPortChecked(item.intID)
    }

    override fun onChannelChosen(item: Channel, position: Int) {
        Toast.makeText(context, "channel chosen " + item.toString(), Toast.LENGTH_SHORT).show()
        viewModel.launchChannel(item)

    }

    override fun onRecommendationChosen(item: Recommendation, position: Int) {
        Toast.makeText(context, "rec chosen " + item.name, Toast.LENGTH_SHORT).show()
        viewModel.launchRecommendation(item)
    }

    override fun appChosenNeedOpen(appModel: ServerAppInfo, positio: Int) {
        Toast.makeText(context, "app chosen " + appModel.applicationName, Toast.LENGTH_SHORT).show()
        viewModel.launchApp(appModel.packageName)
    }

    private fun setPortServerCheck(id: Int) {
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, id)
        viewModel.requestAspect()
    }


    private fun setPort(portId: Int) {
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, portId)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecommendationsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecommendationsViewModel::class.java)

        adapterApps = RecommendationsAdapter(cache, this)
        adapterPorts = RecommendationsAdapter(cache, this)
        adapterRecommend = RecommendationsAdapter(cache, this)
        adapterChannels = RecommendationsAdapter(cache, this)

        viewModel.run {

            binding.reciclerApps.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.reciclerApps.adapter = adapterApps

            binding.reciclerPorts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.reciclerPorts.adapter = adapterPorts

            binding.reciclerRecommendations.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.reciclerRecommendations.adapter = adapterRecommend

            binding.reciclerChannels.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
            binding.reciclerChannels.adapter = adapterChannels

            apps.observe(this@RecommendationsFragment, appsObserver)
            inputs.observe(this@RecommendationsFragment, inputPortObserver)
            recommendations.observe(this@RecommendationsFragment, recommendationsObserver)
            channels.observe(this@RecommendationsFragment, channelsObserver)

            populateApps()
            populatePorts()
            observePreviews()
            requestAllPreviews()
        }
    }

    override fun injectDependencies() {
        fragmentComponent.inject(this)
    }

    fun onPortChecked(id: Int) {
        viewModel.aspectTryCounter = Constants.ASPECT_GET_TRY
        viewModel.lastPortId = id
        if (id == Constants.INPUT_HOME_ID) {
            setPort(id)
            RxBus.publish(SendActionEvent(Action.HOME_DOWN))
            RxBus.publish(SendActionEvent(Action.HOME_UP))
            fragmentManager?.popBackStack()
        } else {
            if (AspectHolder.message?.serverVersionCode ?: 0 < Constants.VER_ASPECT_XIX) { //old server version does not support port check - some values confused confused
                setPort(id)
            } else {
                setPortServerCheck(id)
            }
        }
    }
}


//
//    override fun onResume() {
//        super.onResume()
//    }
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//    }