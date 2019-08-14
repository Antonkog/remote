package com.wezom.kiviremote.presentation.home.devicesearch

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.bus.ChangeSnackbarStateEvent
import com.wezom.kiviremote.bus.KillPingEvent
import com.wezom.kiviremote.bus.LocationEnabledEvent
import com.wezom.kiviremote.common.GpsUtils
import com.wezom.kiviremote.common.NetConnectionUtils
import com.wezom.kiviremote.common.PreferencesManager
import com.wezom.kiviremote.common.RxBus
import com.wezom.kiviremote.databinding.HomeFragmentBinding
import com.wezom.kiviremote.nsd.LastNsdHolder
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper
import com.wezom.kiviremote.presentation.base.BaseFragment
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter
import com.wezom.kiviremote.presentation.base.recycler.addItemDivider
import com.wezom.kiviremote.presentation.base.recycler.initWithLinLay
import com.wezom.kiviremote.presentation.home.HomeActivity
import java.util.*
import javax.inject.Inject

/**
 * Created by andre on 22.05.2017.
 */

class DeviceSearchFragment : BaseFragment(), LazyAdapter.OnItemClickListener<NsdServiceInfoWrapper> {

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

    private val nsdDevicesObserver: Observer<Set<NsdServiceInfoWrapper>> = Observer { devices ->
        devices?.let {
            updateDeviceList(it)
//            tryGoMainScreen(it)
        }
    }

    private var adapter = DeviceSearchAdapter(this)
    private val currentDevices = ArrayList<NsdServiceInfoWrapper>()

    override fun injectDependencies() = fragmentComponent.inject(this)

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as HomeActivity).changeFabVisibility(View.GONE)
        RxBus.listen(LocationEnabledEvent::class.java).subscribe { (enabled) ->
            if (enabled) {
                if (NetConnectionUtils.isConnectedWithWifi(context!!)) {
                    binding.wifiName.text = NetConnectionUtils.getCurrentSsid(context!!).replace("\"", "")
                }
            }
        }
    }

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

        viewModel.updateRecentDevices()
        viewModel.initResolveListener()
        viewModel.discoverDevices()

        // show wifi name
        if (NetConnectionUtils.isConnectedWithWifi(context!!)) {
            binding.wifiName.text = NetConnectionUtils.getCurrentSsid(context!!).replace("\"", "")
        }

        viewModel.nsdDevices.observe(this, nsdDevicesObserver)
        viewModel.networkState.observe(this, networkStateObserver)

        PreferencesManager.setSelectedTab(0)

        // recyclerview init
        binding.devicesContainer.initWithLinLay(LinearLayoutManager.VERTICAL, adapter, listOf())
        binding.devicesContainer.addItemDivider()

        //resetDisconnectStatus
        RxBus.publish(ChangeSnackbarStateEvent(true))
        RxBus.publish(KillPingEvent())

        //resetMediaPlayback
        val homeActivity = activity as HomeActivity
        homeActivity.isHasContent = false
        homeActivity.stopPlayback()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateRecentDevices()
        viewModel.initResolveListener()
        viewModel.discoverDevices()
    }


    private fun updateDeviceList(set: Set<NsdServiceInfoWrapper>) {
        currentDevices.clear()
        currentDevices.addAll(set)
        adapter.swapData(currentDevices)
        showProgress(false)
    }

    private fun updateWifiInfoViews() {
        val isConnectedWithWifi = NetConnectionUtils.isConnectedWithWifi(context!!)
        if (isConnectedWithWifi) {
            GpsUtils.enableGPS(activity!!, null, null)
        }

        binding.wifiIcon.visibility = if (isConnectedWithWifi) View.VISIBLE else View.INVISIBLE
        binding.wifiName.visibility = if (isConnectedWithWifi) View.VISIBLE else View.INVISIBLE
    }

    override fun onLazyItemClick(data: NsdServiceInfoWrapper) {
        LastNsdHolder.nsdServiceWrapper = data
        viewModel.connect(data)
    }
}