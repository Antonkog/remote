package com.wezom.kiviremote.presentation.home.recentdevices

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.home_activity.view.*
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

    private val recentDevicesObserver = Observer<List<RecentDevice>> {
        it?.let { setRecentDevices(it) }
    }

    private val nsdServicesObserver = Observer<Set<NsdServiceInfoWrapper>> {
        it?.let { onNewDevicesDiscovered(it) }
    }

    private val adapter: DevicesListAdapter by lazy {
        DevicesListAdapter(preferences, viewModel::navigateToRecentDevice, this::connect)
    }

    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecentDevicesFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecentDevicesViewModel::class.java)

        viewModel.initResolveListener()
        viewModel.run {
            requestRecentDevices()
            discoverDevices()
            nsdServices.observe(this@RecentDevicesFragment, nsdServicesObserver)
            recentDevices.observe(this@RecentDevicesFragment, recentDevicesObserver)
        }


        binding.devicesContainer.run {
            adapter = this@RecentDevicesFragment.adapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    override fun onPause() {
        (activity as (HomeActivity)).run {
            setHomeAsUp(false)
        }
        super.onPause()
    }

    override fun onResume() {
        (activity as (HomeActivity)).run {
            toolbar.toolbar_text.text = resources.getString(R.string.devices_kivi)
            setHomeAsUp(true)
        }
        super.onResume()
    }


    private fun connect(wrapper: NsdServiceInfoWrapper?) {
        if (wrapper != null) {
            viewModel.connect(wrapper)
        }
    }

    private fun setRecentDevices(devices: List<RecentDevice>) {
        adapter.setRecentDevices(if (devices.size > 5) devices.takeLast(5) else devices)
    }

    private fun onNewDevicesDiscovered(devicesOnline: Set<NsdServiceInfoWrapper>) {
        adapter.setOnlineDevices(devicesOnline)
    }
}