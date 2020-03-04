package com.kivi.remote.presentation.home.recommendations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.kivi.remote.R
import com.kivi.remote.Screens
import com.kivi.remote.bus.SendActionEvent
import com.kivi.remote.common.Action
import com.kivi.remote.common.Constants
import com.kivi.remote.common.RxBus
import com.kivi.remote.common.extensions.Run
import com.kivi.remote.common.extensions.removeMasks
import com.kivi.remote.common.sendLogFile
import com.kivi.remote.databinding.RecommendationsFragmentBinding
import com.kivi.remote.net.model.*
import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.home.HomeActivity
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

    private lateinit var dialogDowngrade : AlertDialog
    private lateinit var ratingDialog : AlertDialog

    private val serverOldObserver = Observer<Boolean> {
        if (it == true) {
            (activity as HomeActivity).run {
                showTouchPad()
                hideSlidingPanel()
            }
            hideRefreshBar(false)
            dialogDowngrade?.show()
        } else {
            hideRefreshBar(true)
            dialogDowngrade?.cancel()
        }
    }


    private val showRatingObserver = Observer<Boolean> {
       if(it == true){
           ratingDialog.show()
       }else{
           ratingDialog.cancel()
       }
    }

    private val recommendationsObserver = Observer<List<Comparable<Recommendation>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterRecommend.swapData(it)
            changeMoviesVisible(View.VISIBLE)
        } ?: changeMoviesVisible(View.GONE)
    }

    private val channelsObserver = Observer<List<Comparable<Channel>>> {
        it?.takeIf { it.isNotEmpty() }?.let {
            adapterChannels.swapData(it)
            changeChannelsVisible(View.VISIBLE)
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
            showTouchPad()
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
            showTouchPad()
            hideSlidingPanel()
        }
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

    private fun hideRefreshBar(hide: Boolean) {
        if (hide) {
            binding.recsRefreshBar.visibility = View.GONE
            binding.scrollTop.visibility = View.VISIBLE
        } else {
            binding.recsRefreshBar.visibility = View.VISIBLE
            binding.scrollTop.visibility = View.INVISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecommendationsFragmentBinding.inflate(inflater, container, false)
        setupDowgradeDialog(inflater)
        setupRatingDialog(inflater)
        return binding.root
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
            oldVersionTv.observe(this@RecommendationsFragment, serverOldObserver)
            showRatingDialog.observe(this@RecommendationsFragment, showRatingObserver)

            populateApps()
            populatePorts()
            populateChannels()
            populateRecommendations()

            Run.after(Constants.DELAY_CHANNELS_GET) {
                    viewModel.requestAspect()
            }
        }


        val listener = View.OnClickListener { view ->
            run {
                when (view.id) {
                    com.kivi.remote.R.id.img_recommend_menu, com.kivi.remote.R.id.text_subscriptions -> {
                        viewModel.navigateTo(Screens.KIVI_CATALOG_FRAGMENT)
                    }

                    com.kivi.remote.R.id.img_apps_menu, com.kivi.remote.R.id.text_apps -> {
                        viewModel.navigateTo(Screens.RECS_APPS_DEEP_FRAGMENT)
                    }

                    com.kivi.remote.R.id.img_channels_menu, com.kivi.remote.R.id.text_channel -> {
                        viewModel.navigateTo(Screens.RECS_CHANNELS_DEEP_FRAGMENT)
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

    private fun setupDowgradeDialog(inflater: LayoutInflater) {
        val checkBoxView = inflater.inflate(R.layout.layout_checkbox, null)

        dialogDowngrade = AlertDialog.Builder(binding.root.context, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                .setTitle(R.string.downgrade_error)
                .setMessage(R.string.downgrade_description)
                .setPositiveButton(R.string.download) { dialog1, which -> viewModel.sendToRemoteApp(binding.root.context, toOldRemote = false)}
                .setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }
                .create()

        checkBoxView.findViewById<CheckBox>(R.id.checkBox).setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) viewModel.updateAsked = isChecked
        }

        dialogDowngrade.setView(checkBoxView)
        dialogDowngrade.setCancelable(true)


    }


    private fun setupRatingDialog(inflater: LayoutInflater) {

        val ratingView = inflater.inflate(R.layout.layout_rating, null)

        dialogDowngrade = AlertDialog.Builder(binding.root.context, R.style.ThemeOverlay_AppCompat_Dialog_Alert)
                .setTitle(R.string.downgrade_error)
                .setMessage(R.string.rating_description)
                .setPositiveButton(R.string.rate) { dialog1, which -> viewModel.sendToRemoteApp(binding.root.context, toOldRemote = true) }
                .setNegativeButton(R.string.later) {
                    dialog, which -> dialog.cancel()
                    viewModel.ratingAsked = true
                }
                .create()

        ratingView.findViewById<RatingBar>(R.id.rating_bar).setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if(rating > 3){
                viewModel.sendToRemoteApp(binding.root.context, toOldRemote = true)
            } else {
                sendLogFile(binding.root.context)
            }
        }
        dialogDowngrade.setView(ratingView)
        dialogDowngrade.setCancelable(true)


    }


    override fun injectDependencies() {
        fragmentComponent.inject(this)
    }

    fun onPortChecked(id: Int) {
        viewModel.aspectTryCounter = Constants.ASPECT_GET_TRY
        viewModel.lastPortId = id
        viewModel.sendAspectSingleChangeEvent(AspectMessage.ASPECT_VALUE.INPUT_PORT, id)
        if (id == Constants.INPUT_HOME_ID) {
            RxBus.publish(SendActionEvent(Action.HOME_DOWN))
            RxBus.publish(SendActionEvent(Action.HOME_UP))
            fragmentManager?.popBackStack()
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

