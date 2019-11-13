package com.wezom.kiviremote.presentation.home.devicesearch

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.R
import com.wezom.kiviremote.bus.ChangeSnackbarStateEvent
import com.wezom.kiviremote.bus.KillPingEvent
import com.wezom.kiviremote.common.GpsUtils
import com.wezom.kiviremote.common.NetConnectionUtils
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.databinding.HomeFragmentBinding
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter
import com.wezom.kiviremote.presentation.base.recycler.addItemDivider
import com.wezom.kiviremote.presentation.base.recycler.initWithLinLay
import com.wezom.kiviremote.presentation.home.HomeActivity
import javax.inject.Inject

/**
 * Created by andre on 22.05.2017.
 */

class DeviceSearchFragment : BaseFragment(), LazyAdapter.OnItemClickListener<NsdServiceInfo> {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    lateinit var binding: HomeFragmentBinding
    lateinit var viewModel: DeviceSearchViewModel

    private val networkStateObserver: Observer<Boolean> = Observer { isAvailable ->
        updateWifiInfoViews()
        if (!(isAvailable != null && isAvailable)) {
            adapter.swapData(listOf())
            showProgress(false)
        }
    }

    private val nsdDevicesObserver: Observer<Set<NsdServiceInfo>> = Observer { devices ->
        devices?.let {
            if(it.isNotEmpty())
            updateDeviceList(it)
            else showProgress(true)
        }
    }

    private var adapter = DeviceSearchAdapter(this)

    override fun injectDependencies() = fragmentComponent.inject(this)

    fun showProgress(visibility: Boolean) {
        binding.searchProgressContainer.visibility = if (visibility) View.VISIBLE else View.GONE
        binding.devicesContainer.visibility = if (!visibility) View.VISIBLE else View.GONE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DeviceSearchViewModel::class.java)

        viewModel.initResolveListener()
        viewModel.discoverDevices()

        setWifiName(true)
        viewModel.nsdDevices.observe(this, nsdDevicesObserver)
        viewModel.networkState.observe(this, networkStateObserver)

        // recyclerview init
        binding.devicesContainer.initWithLinLay(LinearLayoutManager.VERTICAL, adapter, listOf())
        binding.devicesContainer.addItemDivider()

        //resetDisconnectStatus
        RxBus.publish(ChangeSnackbarStateEvent(true))
        RxBus.publish(KillPingEvent())

        //resetMediaPlayback
//        val homeActivity = activity as HomeActivity
//        homeActivity.isHasContent = false
//        homeActivity.stopPlayback()
    }

    override fun onResume() {
        super.onResume()
        (activity as HomeActivity).run {
            uncheckMenu()
            changeFabVisibility(View.GONE)
            setToolbarTxt("")
        }
        viewModel.initResolveListener()
        viewModel.discoverDevices()
    }


    private fun updateDeviceList(set: Set<NsdServiceInfo>) {
        adapter.swapData(set.toList())
        viewModel.tryAutoConnect(set)
        showProgress(false)
    }


    private fun setWifiName(enabled: Boolean) {
        // show wifi name
        if (enabled && NetConnectionUtils.isConnectedWithWifi(context!!)) {
            binding.wifiName.text = NetConnectionUtils.getCurrentSsid(context!!).replace("\"", "")
        } else {
            binding.wifiName.text = resources.getString(R.string.unknown_wifi)
        }
    }

    private fun updateWifiInfoViews() {
        val isConnectedWithWifi = NetConnectionUtils.isConnectedWithWifi(context!!)
        if (isConnectedWithWifi) {
            GpsUtils.enableGPS(activity!!, null, null)
        }

        binding.wifiIcon.visibility = if (isConnectedWithWifi) View.VISIBLE else View.INVISIBLE
        binding.wifiName.visibility = if (isConnectedWithWifi) View.VISIBLE else View.INVISIBLE
    }

    override fun onLazyItemClick(data: NsdServiceInfo) {
        viewModel.connect(data)
    }
}