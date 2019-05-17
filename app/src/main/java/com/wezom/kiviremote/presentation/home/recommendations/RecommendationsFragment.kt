package com.wezom.kiviremote.presentation.home.recommendations

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.os.Bundle
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
import com.wezom.kiviremote.net.model.AspectMessage
import com.wezom.kiviremote.net.model.RecommendItem
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.apps.AppModel
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port
import timber.log.Timber
import javax.inject.Inject


class RecommendationsFragment : BaseFragment(), HorizontalCVContract.HorizontalCVListener {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var cache: KiviCache

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var viewModel: RecommendationsViewModel

    private lateinit var binding: RecommendationsFragmentBinding

    private lateinit var adapterPorts: RecommendationsAdapter
    private lateinit var adapterApps: RecommendationsAdapter
    private lateinit var adapterRecommend: RecommendationsAdapter


    private val recommendationsObserver = Observer<List<Comparable<RecommendItem>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterRecommend.swapData(it)
        } ?: Timber.e("TYPE_RECOMMENDATIONS empty")
    }

    private val appsObserver = Observer<List<Comparable<AppModel>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterApps.swapData(it)
        } ?: Timber.e("TYPE_RECOMMENDATIONS empty")
    }

    private val showPortsObserver = Observer<List<Comparable<Port>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterPorts.swapData(it)
        } ?: Timber.e("TYPE_INPUTS empty")
    }


    override fun onPortChosen(port: Port, position: Int) {
        Toast.makeText(context, "port chosen " + port.portName, Toast.LENGTH_SHORT).show()
        onPortChecked(port.portNum)
    }

    override fun onRecommendationChosen(item: RecommendItem, position: Int) {
        Toast.makeText(context, "rec chosen " + item.title, Toast.LENGTH_SHORT).show()
    }

    override fun appChosenNeedOpen(appModel: AppModel, positio: Int) {
        Toast.makeText(context, "app chosen " + appModel.appName, Toast.LENGTH_SHORT).show()
        viewModel.launchApp(appModel.appPackage)
    }

    private fun setPortServerCheck(id: Int) {
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, id)
        viewModel.requestAspect()
    }


    private fun setPort(portId: Int) {
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, portId)
//        binding.viewInputs.adapter.swapData()
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

        viewModel.run {

            binding.reciclerSubscriptions.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.reciclerSubscriptions.adapter = adapterRecommend

            adapterRecommend.swapData(viewModel.setRecommendData())

            binding.reciclerApps.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.reciclerApps.adapter = adapterApps

            binding.reciclerPorts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.reciclerPorts.adapter = adapterPorts

            startUPnPController()

            apps.observe(this@RecommendationsFragment, appsObserver)
            ports.observe(this@RecommendationsFragment, showPortsObserver)
//            recommendations.observe(this@RecommendationsFragment, recommendationsObserver)

            observePorts()
//        if (!AspectHolder.hasAspectSettings() && AspectHolder.initialMsg != null) viewModel?.requestAspect()
//        else (viewModel?.aspectEvent.postValue(GotAspectEvent(AspectHolder.message, AspectHolder.availableSettings, AspectHolder.initialMsg)))
            requestApps()
            populateApps()
            requestAspect()

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
            if (AspectHolder?.message?.serverVersionCode ?: 0 < Constants.VER_ASPECT_XIX) { //old server version does not support port check - some values confused confused
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