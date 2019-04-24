package com.wezom.kiviremote.presentation.home.recommendations

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.wezom.kiviremote.views.HorizontalCardsView
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class RecommendationsFragment : BaseFragment(), HorizontalCardsView.OnClickListener {

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


    private val recommendationsObserver = Observer<List<RecommendItem>> {
        //        it?.let { recommendAdapter(reccomendations = it) }
        it?.takeIf { it.isNotEmpty() }?.let {
            updateRecs( true, recs = it )
        } ?: updateRecs(false, recs = null)
    }
//
//    private val appsObserver = Observer<List<AppModel>> {
//        it?.takeIf { it.isNotEmpty() }?.let {
//            updateApps(true, it)
//        } ?: updateApps(false, null)
//    }
//
//    private val showPortsObserver = Observer<GotAspectEvent> {
//        binding.viewInputs.progressBar.visibility = View.GONE
//        it?.getPortsList()?.let { updatePorts(it) }
//    }
private fun updateRecs(visible: Boolean, recs: List<RecommendItem> ?) {
    var items = LinkedList<RecommendItem>()
//    Timber.i("ports can be passed to adapter: " + ports.size)
//    for (port in ports) {
//        items.addLast(RecommendItem(HorizontalCardsView.ContentType.TYPE_INPUTS.ordinal, port.portNum, port.portName, "", port.portImageId, ""))
//    }
//    binding.viewInputs.setNewItems(items, HorizontalCardsView.ContentType.TYPE_INPUTS)
}
    private fun updatePorts(ports: List<Port>) {
        var items = LinkedList<RecommendItem>()
        Timber.i("ports can be passed to adapter: " + ports.size)
        for (port in ports) {
            items.addLast(RecommendItem(HorizontalCardsView.ContentType.TYPE_INPUTS.ordinal, port.portNum, port.portName, "", port.portImageId, ""))
        }
        binding.viewInputs.setNewItems(items, HorizontalCardsView.ContentType.TYPE_INPUTS)
    }

    private fun updateApps(visible: Boolean, apps: List<AppModel>?) {
        var items = LinkedList<RecommendItem>()
        Timber.i("apps can be passed to adapter: " + apps?.size)
        var counter = 0
        apps?.forEach {
            Timber.i(it.appName + " can be passed to adapter")
            items.addLast(RecommendItem(HorizontalCardsView.ContentType.TYPE_APPS.ordinal, counter++, it.appName, it.appPackage, -1, ""))
        }

        binding.viewApps.setNewItems(items, HorizontalCardsView.ContentType.TYPE_APPS)
    }


    private fun setPortServerCheck(id: Int) {
        binding.viewRecs.visibility = View.VISIBLE
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, id)
        viewModel.requestAspect()
    }


    private fun setPort(portId: Int) {
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, portId)
        binding.viewInputs.sePortActivebyId(portId)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecommendationsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewApps.setAdapter(view.context, cache, HorizontalCardsView.ContentType.TYPE_APPS)
        binding.viewInputs.setAdapter(view.context, cache, HorizontalCardsView.ContentType.TYPE_INPUTS)
        binding.viewRecs.setAdapter(view.context, cache, HorizontalCardsView.ContentType.TYPE_RECOMMENDATIONS)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecommendationsViewModel::class.java)
        viewModel.startUPnPController()
//
//        viewModel?.apps.observe(this@RecommendationsFragment, appsObserver)
//        viewModel?.aspectEvent.observe(this@RecommendationsFragment, showPortsObserver)
        viewModel?.recommendations.observe(this@RecommendationsFragment, recommendationsObserver)

        viewModel.observePorts()
//        if (!AspectHolder.hasAspectSettings() && AspectHolder.initialMsg != null) viewModel?.requestAspect()
//        else (viewModel?.aspectEvent.postValue(GotAspectEvent(AspectHolder.message, AspectHolder.availableSettings, AspectHolder.initialMsg)))

        viewModel?.requestApps()
        viewModel?.populateApps()
        viewModel?.requestAspect()
    }

    override fun injectDependencies() {
        fragmentComponent.inject(this)
    }


    override fun onClick(serverId: Int, type: HorizontalCardsView.ContentType) {
        Timber.e("click " + serverId + " type " + type.name)

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


//        viewModel.run {
//            setData()
//            recommendations.observe(this@RecommendationsFragment, recommendationsObserver)
//            requestRecommendations()
//        }


//            addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
//                    // do nothing
//                }
//
//                override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
//                    when (newState) {
////                        RecyclerView.SCROLL_STATE_IDLE -> allowPress = true
////                        RecyclerView.SCROLL_STATE_DRAGGING -> allowPress = false
////                        RecyclerView.SCROLL_STATE_SETTLING -> allowPress = false
//                    }
//                }
//            })