package com.wezom.kiviremote.presentation.home.recommendations

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.wezom.kiviremote.R
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
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.presentation.home.main.BackHandler
import kotlinx.android.synthetic.main.home_activity.*
import java.lang.ref.WeakReference
import kotlin.collections.HashMap


class RecommendationsFragment : BaseFragment(), HorizontalCVContract.HorizontalCVListener, BackHandler.OnBackClickListener {


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

    private var rowsViews = HashMap<Int, WeakReference<View>>()

    private val recommendationsObserver = Observer<List<Comparable<Recommendation>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterRecommend.swapData(it)
            updateRecommendations(true)
        } ?: Timber.e("TYPE_RECOMMENDATIONS empty")
    }

    private val channelsObserver = Observer<List<Comparable<Channel>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterChannels.swapData(it)
            updateRecommendations(true)
        } ?: Timber.e("TYPE_Channels empty")
    }

    private val appsObserver = Observer<List<Comparable<ServerAppInfo>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterApps.swapData(it)
        } ?: Timber.e("TYPE_APPS empty")
    }

    private val inputPortObserver = Observer<List<Comparable<Input>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterPorts.swapData(it)
        } ?: Timber.e("TYPE_INPUTS empty")
    }


    override fun onInputChosen(item: Input, position: Int) {
        Toast.makeText(context, "port chosen id: " + item.intID + "name" + item.name, Toast.LENGTH_SHORT).show()
        onPortChecked(item.intID)
    }

    override fun onChannelChosen(item: Channel, position: Int) {
        Toast.makeText(context, "channel chosen " + item.toString(), Toast.LENGTH_SHORT).show()
        viewModel.launchChannel(item)

    }

    override fun onRecommendationChosen(item: Recommendation, position: Int) {
        Toast.makeText(context, "rec chosen " + item.toString(), Toast.LENGTH_SHORT).show()
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

    private fun updateRecommendations(isVisible: Boolean) {
        if(!isVisible){
            binding.recsRefreshBar.visibility = View.VISIBLE
            binding.scrollTop.visibility = View.INVISIBLE
        }else{
            binding.recsRefreshBar.visibility = View.GONE
            binding.scrollTop.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecommendationsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecommendationsViewModel::class.java)

        adapterApps = RecommendationsAdapter( this)
        adapterPorts = RecommendationsAdapter( this)
        adapterRecommend = RecommendationsAdapter(this)
        adapterChannels = RecommendationsAdapter(this)

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
            populateChannels()
            populateRecommendations()
            requestAllPreviews()
        }

        rowsViews.put(binding.reciclerApps.id, WeakReference(binding.reciclerApps))
        rowsViews.put(binding.reciclerPorts.id, WeakReference(binding.reciclerPorts))
        rowsViews.put(binding.reciclerRecommendations.id, WeakReference(binding.reciclerRecommendations))
        rowsViews.put(binding.reciclerChannels.id, WeakReference(binding.reciclerChannels))
        rowsViews.put(binding.textSubscriptions.id, WeakReference(binding.textSubscriptions))
        rowsViews.put(binding.textChannel.id, WeakReference(binding.textChannel))
        rowsViews.put(binding.textApps.id, WeakReference(binding.textApps))
        rowsViews.put(binding.textPorts.id, WeakReference(binding.textPorts))
        rowsViews.put(binding.imgAppsMenu.id, WeakReference(binding.imgAppsMenu))
        rowsViews.put(binding.imgChannelsMenu.id, WeakReference(binding.imgChannelsMenu))
        rowsViews.put(binding.imgRecommendMenu.id, WeakReference(binding.imgRecommendMenu))


        binding.imgAppsMenu.setOnClickListener {
            isDeepMenuOpen = true
            changeRowsVisibility(binding.reciclerApps.id, View.GONE)
            (activity as HomeActivity).setHomeAsUp(true)
            binding.reciclerApps.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            binding.reciclerApps.adapter.notifyItemRangeChanged(0, binding.reciclerApps.adapter?.itemCount
                    ?: 0)
        }

        binding.imgChannelsMenu.setOnClickListener {
            isDeepMenuOpen = true
            changeRowsVisibility(binding.reciclerChannels.id, View.GONE)
            (activity as HomeActivity).setHomeAsUp(true)
            binding.reciclerChannels.layoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
            binding.reciclerChannels.adapter.notifyItemRangeChanged(0, binding.reciclerChannels.adapter?.itemCount
                    ?: 0)

        }

        binding.imgRecommendMenu.setOnClickListener {
            isDeepMenuOpen = true
            changeRowsVisibility(binding.reciclerRecommendations.id, View.GONE)
            (activity as HomeActivity).setHomeAsUp(true)
            binding.reciclerRecommendations.layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
            binding.reciclerRecommendations.adapter.notifyItemRangeChanged(0, binding.reciclerRecommendations.adapter?.itemCount
                    ?: 0)
        }
    }

    //this is START for handling back in fragment
    private var backButtonHandler: BackHandler? = null

    override fun onAttachFragment(childFragment: Fragment?) {
        super.onAttachFragment(childFragment)
        attachListener(activity as HomeActivity?)
    }

    override fun onDetach() {
        detachListener()
        super.onDetach()
    }

    private fun detachListener() {
        backButtonHandler?.removeBackListener(this)
        backButtonHandler = null
    }

    private fun attachListener(activity: HomeActivity?) {
        if (activity != null)
            backButtonHandler = activity.addBackListener(this)
    }


    override fun onBackClick(): Boolean {
        Timber.e("onBackClick")
        if (isDeepMenuOpen) {
            isDeepMenuOpen = false
            (activity as HomeActivity).setHomeAsUp(false)
            binding.reciclerApps.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.reciclerRecommendations.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.reciclerChannels.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)

            binding.reciclerRecommendations.adapter.notifyItemRangeChanged(0, binding.reciclerRecommendations.adapter?.itemCount ?: 0)
            binding.reciclerChannels.adapter.notifyItemRangeChanged(0, binding.reciclerChannels.adapter?.itemCount ?: 0)
            binding.reciclerApps.adapter.notifyItemRangeChanged(0, binding.reciclerApps.adapter?.itemCount ?: 0)
            changeRowsVisibility(0, View.VISIBLE)
            return true
        } else return false
    }

//this END is for handling back in fragment

    private fun changeRowsVisibility(viewExceptId: Int, visibility: Int) {
        for ((key, value) in rowsViews) {
            if (key != viewExceptId) {
                value.get()?.visibility = visibility
            }
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

    override fun onResume() {
        super.onResume()
        (activity as HomeActivity).changeFabVisibility(View.VISIBLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as HomeActivity).changeFabVisibility(View.GONE)
    }
}


