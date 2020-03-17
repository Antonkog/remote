package com.kivi.remote.presentation.home.recentdevices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.kivi.remote.R
import com.kivi.remote.databinding.RecentDevicesFragmentBinding
import com.kivi.remote.persistence.model.RecentDevice
import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.home.HomeActivity
import java.util.*
import javax.inject.Inject

class RecentDevicesFragment : BaseFragment(), RecentDevicesListener {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var viewModel: RecentDevicesViewModel

    private lateinit var binding: com.kivi.remote.databinding.RecentDevicesFragmentBinding

    private val recentDevicesObserver = Observer<List<RecentDevice>> {
        it?.let { setRecentDevices(it) }
    }

    private lateinit var adapter: RecentDevicesAdapter

    override fun injectDependencies() = fragmentComponent.inject(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecentDevicesFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RecentDevicesViewModel::class.java)

        adapter = RecentDevicesAdapter(this, currentConnection = viewModel.lastNsdHolderName)

        binding.devicesContainer.run {
            adapter = this@RecentDevicesFragment.adapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }

        viewModel.run {
            requestRecentDevices()
            recentDevices.observe(viewLifecycleOwner, recentDevicesObserver)
        }
    }

    override fun onResume() {
        (activity as (HomeActivity)).run {
            setToolbarTxt(resources.getString(R.string.devices_kivi))
        }
        super.onResume()
    }

    override fun connectDeviceChosen(recentDevice: RecentDevice, position: Int) {
        //show dialog
    }

    override fun infoBtnChosen(recentDevice: RecentDevice, position: Int) {
        viewModel.navigateToRecentDevice(recentDevice)
    }

/*
    private fun connect(data: NsdServiceInfo) {
        if (context != null) {
            viewModel.connect(data, context!!)
        }
    }
*/


    private fun setRecentDevices(devices: List<RecentDevice>) {
        Collections.sort(devices)
        val newData : MutableList<Comparable<*>>  = ArrayList()
        newData.add(resources.getString(R.string.mine_devices))
        devices.forEach { if(it.wasConnected != null &&  it.wasConnected > 0) newData.add(it) }
        newData.add(resources.getString(R.string.other_devices))
        devices.forEach { if(it.wasConnected == null || it.wasConnected == 0L) newData.add(it) }
        adapter.swapData(newData)
    }
}