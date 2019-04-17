package com.wezom.kiviremote.presentation.home.recentdevices

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.transition.AutoTransition
import android.support.transition.Transition
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.R
import com.wezom.kiviremote.databinding.RecentDevicesFragmentBinding
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.home.HomeActivity
import com.wezom.kiviremote.presentation.home.recentdevices.list.DevicesListAdapter
import java.util.*
import javax.inject.Inject

class RecentDevicesFragment : BaseFragment() {

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var viewModel: RecentDevicesViewModel

    private lateinit var binding: RecentDevicesFragmentBinding

    private var isInEditMode: Boolean = false

    private var allowPress = true

    private val mainConstraintSet = ConstraintSet()

    private val mainEditConstraintSet = ConstraintSet()

    private val recentDevicesObserver = Observer<List<RecentDevice>> {
        it?.let { setRecentDevices(it) }
    }

    private val nsdServicesObserver = Observer<Set<NsdServiceInfoWrapper>> {
        it?.let { onNewDevicesDiscovered(it) }
    }

    private val adapter: DevicesListAdapter by lazy {
        DevicesListAdapter(preferences,viewModel::navigateToRecentDevice)
    }

    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecentDevicesFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecentDevicesViewModel::class.java)

        viewModel.run {
            requestRecentDevices()
            nsdServices.observe(this@RecentDevicesFragment, nsdServicesObserver)
            recentDevices.observe(this@RecentDevicesFragment, recentDevicesObserver)
        }

        binding.devicesContainer.run {
            adapter = this@RecentDevicesFragment.adapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    // do nothing
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> allowPress = true
                        RecyclerView.SCROLL_STATE_DRAGGING -> allowPress = false
                        RecyclerView.SCROLL_STATE_SETTLING -> allowPress = false
                    }
                }
            })
        }

        ///viewModel.requestRecentDevices()
        viewModel.discoverDevices()

        setupConstraintMagic()
        (activity as HomeActivity).hideSlidingPanel()

        binding.devicesEdit.setOnClickListener {
            if (allowPress) toggleEdit()
        }

        binding.devicesClose.setOnClickListener {
            if (allowPress) {
                binding.recentDevicesContainer.clearAnimation()
                binding.devicesContainer.clearAnimation()
                viewModel.navigateBack()
            }
        }

        binding.devicesHome.setOnClickListener {
            if (allowPress) {
                binding.recentDevicesContainer.clearAnimation()
                binding.devicesContainer.clearAnimation()
                viewModel.navigateToHome()
            }
        }

//        binding.devicesCancel.setOnClickListener {
//            if (allowPress) {
//                adapter.discardChanges()
//                toggleEdit()
//            }
//        }
//
//        binding.devicesConfirm.setOnClickListener {
//            if (allowPress) {
//                adapter.confirmDeletion()
//                toggleEdit()
//            }
//        }

        val wrapper = NsdServiceInfoWrapper(NsdServiceInfo(), "LeoTV")
        val set = HashSet<NsdServiceInfoWrapper>()
        set.add(wrapper)
        set.add(wrapper)
        set.add(wrapper)

        setRecentDevices(listOf(RecentDevice("Huy","Bleat"),RecentDevice("Huy","Bleat")))
        onNewDevicesDiscovered(set)

    }

    private fun setupConstraintMagic() {
        mainConstraintSet.clone(binding.recentDevicesContainer)
        mainEditConstraintSet.clone(activity, R.layout.recent_devices_edit_fragment)
    }

    private fun toggleEdit() {
        binding.devicesContainer.apply {
            visibility = View.GONE
            animate().translationX(this.height.toFloat())
        }

        val autoTransition = AutoTransition()
        autoTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition) {
                binding.devicesContainer.apply {
                    visibility = View.VISIBLE
                    animate().translationX(0F)
                    activateClicks()
                }
            }

            override fun onTransitionResume(transition: Transition) {
                // do nothing
            }

            override fun onTransitionPause(transition: Transition) {
                // do nothing
            }

            override fun onTransitionCancel(transition: Transition) {
                // do nothing
            }

            override fun onTransitionStart(transition: Transition) {
                // do nothing
            }
        })

//        adapter.apply {
//            val constraint: ConstraintSet
//            if (isInEditMode) {
//                constraint = mainConstraintSet
//                allowNavigation = true
//                showDelete()
//            } else {
//                constraint = mainEditConstraintSet
//                allowNavigation = false
//                showDelete()
//            }
//
//            constraint.applyTo(binding.recentDevicesContainer)
//            disableClicks()
//            TransitionManager.go(Scene(binding.recentDevicesContainer), autoTransition)
//        }


        if (!isInEditMode) {
            binding.devicesHome.apply {
                visibility = View.GONE
                animate()?.translationY(200F)
            }

        } else {
            binding.devicesHome.apply {
                visibility = View.VISIBLE
                animate()?.translationY(0F)
            }
        }

        isInEditMode = !isInEditMode
    }

    private fun disableClicks() {
        binding.devicesHome.isEnabled = false
        binding.devicesClose.isEnabled = false
    }

    private fun activateClicks() {
        binding.devicesHome.isEnabled = true
        binding.devicesClose.isEnabled = true
    }

    private fun setRecentDevices(devices: List<RecentDevice>) {
        adapter.setRecentDevices(if (devices.size > 5) devices.takeLast(5) else devices)
    }

    private fun onNewDevicesDiscovered(devicesOnline: Set<NsdServiceInfoWrapper>) {
        adapter.setOnlineDevices(devicesOnline)
    }
}