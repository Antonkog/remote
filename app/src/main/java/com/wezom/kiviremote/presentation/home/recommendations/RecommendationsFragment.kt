package com.wezom.kiviremote.presentation.home.recommendations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.bus.SendActionEvent
import com.wezom.kiviremote.common.Action
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.common.extensions.removeMasks
import com.wezom.kiviremote.databinding.RecommendationsFragmentBinding
import com.wezom.kiviremote.net.model.*
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder
import timber.log.Timber
import javax.inject.Inject


class RecommendationsFragment : BaseFragment(), HorizontalCVContract.HorizontalCVListener {


    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: RecommendationsViewModel
    private lateinit var binding: RecommendationsFragmentBinding

    private lateinit var adapterPorts: RecommendationsAdapter
    private lateinit var adapterApps: RecommendationsAdapter
    private lateinit var adapterChannels: RecommendationsAdapter
    private lateinit var adapterRecommend: RecommendationsAdapter

    private val recommendationsObserver = Observer<List<Comparable<Recommendation>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterRecommend.swapData(it)
            changeMoviesVisible(View.VISIBLE)
            updateRecommendations(true)
        } ?: changeMoviesVisible(View.GONE)
    }

    private val channelsObserver = Observer<List<Comparable<Channel>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterChannels.swapData(it)
            changeChannelsVisible(View.VISIBLE)
            updateRecommendations(true)
        } ?: changeChannelsVisible(View.GONE)

    }

    private val appsObserver = Observer<List<Comparable<ServerAppInfo>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterApps.swapData(it)
            changeAppsVisible(View.VISIBLE)
        } ?: changeAppsVisible(View.GONE)
    }

    private val inputPortObserver = Observer<List<Comparable<Input>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterPorts.swapData(it)
        } ?: Timber.d("TYPE_INPUTS empty")
    }


    override fun onInputChosen(item: Input, position: Int) {
        onPortChecked(item.intID)
    }

    override fun onChannelChosen(item: Channel, position: Int) {
        viewModel.launchChannel(item)
        (activity as HomeActivity).run {
            moveTouchPad(BottomSheetBehavior.STATE_EXPANDED)
            hideSlidingPanel()
            changeFabVisibility(View.GONE)
        }

    }

    override fun onRecommendationChosen(item: Recommendation, position: Int) {
        viewModel.launchRecommendation(item)
    }

    override fun appChosenNeedOpen(appModel: ServerAppInfo, positio: Int) {
        viewModel.launchApp(appModel.packageName)
        (activity as HomeActivity).run {
            moveTouchPad(BottomSheetBehavior.STATE_EXPANDED)
            hideSlidingPanel()
        }
    }

    private fun setPortServerCheck(id: Int) {
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, id)
        viewModel.requestAspect()
    }


    private fun setPort(portId: Int) {
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, portId)
    }

    private fun updateRecommendations(isVisible: Boolean) {
        if (!isVisible) {
            binding.recsRefreshBar.visibility = View.VISIBLE
            binding.scrollTop.visibility = View.INVISIBLE
        } else {
            binding.recsRefreshBar.visibility = View.GONE
            binding.scrollTop.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecommendationsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun changeChannelsVisible(visible: Int) {
        binding.imgChannelsMenu.visibility = visible
        binding.reciclerChannels.visibility = visible
        binding.textChannel.visibility = visible
    }


    private fun changeMoviesVisible(visible: Int) {
        binding.imgRecommendMenu.visibility = visible
        binding.reciclerRecommendations.visibility = visible
        binding.textSubscriptions.visibility = visible
    }


    private fun changeAppsVisible(visible: Int) {
        binding.imgAppsMenu.visibility = visible
        binding.reciclerApps.visibility = visible
        binding.textApps.visibility = visible
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecommendationsViewModel::class.java)

        viewModel.lastNsdHolderName.let {
            (activity as HomeActivity).setToolbarTxt(it.removeMasks())
        }

        adapterApps = RecommendationsAdapter(this, viewModel.cache)
        adapterPorts = RecommendationsAdapter(this, viewModel.cache)
        adapterRecommend = RecommendationsAdapter(this, viewModel.cache)
        adapterChannels = RecommendationsAdapter(this, viewModel.cache)

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
        }


        val listener = View.OnClickListener { view ->
            run {
                when (view.id) {
                    com.wezom.kiviremote.R.id.img_recommend_menu, com.wezom.kiviremote.R.id.text_subscriptions -> {
                        viewModel.goDeep(Screens.RECS_MOVIE_DEEP_FRAGMENT)
                    }

                    com.wezom.kiviremote.R.id.img_apps_menu, com.wezom.kiviremote.R.id.text_apps -> {
                        viewModel.goDeep(Screens.RECS_APPS_DEEP_FRAGMENT)
                    }

                    com.wezom.kiviremote.R.id.img_channels_menu, com.wezom.kiviremote.R.id.text_channel -> {
                        viewModel.goDeep(Screens.RECS_CHANNELS_DEEP_FRAGMENT)
                    }
                }
            }
        }


        binding.imgRecommendMenu.setOnClickListener(listener)
        binding.textSubscriptions.setOnClickListener(listener)

        binding.imgChannelsMenu.setOnClickListener(listener)
        binding.textChannel.setOnClickListener(listener)

        binding.imgAppsMenu.setOnClickListener(listener)
        binding.textApps.setOnClickListener(listener)

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
        (activity as HomeActivity).run {
            changeFabVisibility(View.VISIBLE)
            uncheckMenu()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as HomeActivity).run { changeFabVisibility(View.GONE) }
    }
}

