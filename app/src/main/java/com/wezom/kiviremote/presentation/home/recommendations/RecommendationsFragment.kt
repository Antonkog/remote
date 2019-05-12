package com.wezom.kiviremote.presentation.home.recommendations

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
    private lateinit var adapter: RecommendationsAdapter


    private val recommendationsObserver = Observer<List<Comparable<RecommendItem>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapter.swapData(it)
        } ?: Timber.e("TYPE_RECOMMENDATIONS empty")
    }

    private val appsObserver = Observer<List<Comparable<AppModel>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapter.swapData(it)
        } ?: Timber.e("TYPE_RECOMMENDATIONS empty")
    }

    private val showPortsObserver = Observer<List<Comparable<Port>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapter.swapData(it)
        } ?: Timber.e("TYPE_INPUTS empty")
    }


    override fun onPortChosen(port: Port, position: Int) {
        setPort(position)
    }

    override fun onRecommendationChosen(item: RecommendItem, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun appChosenNeedOpen(appModel: AppModel, positio: Int) {
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


        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = RecommendationsAdapter(this)
        binding.recycler.layoutManager = layoutManager
        binding.recycler.adapter = adapter


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecommendationsViewModel::class.java)
        viewModel.startUPnPController()

        viewModel.apps.observe(this@RecommendationsFragment, appsObserver)
        viewModel.ports.observe(this@RecommendationsFragment, showPortsObserver)
        viewModel.recommendations.observe(this@RecommendationsFragment, recommendationsObserver)

        viewModel.observePorts()
//        if (!AspectHolder.hasAspectSettings() && AspectHolder.initialMsg != null) viewModel?.requestAspect()
//        else (viewModel?.aspectEvent.postValue(GotAspectEvent(AspectHolder.message, AspectHolder.availableSettings, AspectHolder.initialMsg)))

        viewModel.requestApps()
        viewModel.populateApps()
        viewModel.requestAspect()
        viewModel.setRecommendData()
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