package com.kivi.remote.presentation.home.devicesearch

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.kivi.remote.R
import com.kivi.remote.bus.ChangeSnackbarStateEvent
import com.kivi.remote.bus.KillPingEvent
import com.kivi.remote.common.GpsUtils
import com.kivi.remote.common.NetConnectionUtils
import com.kivi.remote.common.RxBus
import com.kivi.remote.databinding.HomeFragmentBinding
import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.base.recycler.LazyAdapter
import com.kivi.remote.presentation.base.recycler.addItemDivider
import com.kivi.remote.presentation.base.recycler.initWithLinLay
import com.kivi.remote.presentation.home.HomeActivity
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
        viewModel.nsdDevices.observe(viewLifecycleOwner, nsdDevicesObserver)
        viewModel.networkState.observe(viewLifecycleOwner, networkStateObserver)

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
            hideTouchPad()
            hideSlidingPanel()
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