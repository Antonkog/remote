package com.wezom.kiviremote.presentation.home.recentdevices.list

import android.content.SharedPreferences
import android.net.nsd.NsdServiceInfo
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.extensions.string
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.home.recentdevices.TvDeviceInfo
import com.wezom.kiviremote.presentation.home.recentdevices.item.DevicesAdapter
import timber.log.Timber
import java.util.*

class DevicesListAdapter(preferences: SharedPreferences, val navigateCommand: (TvDeviceInfo) -> Unit, val connectCommand: (NsdServiceInfo) -> Unit) : RecyclerView.Adapter<DevicesListViewHolder>() {

    companion object {
        private const val TYPE_RECENT = 0
        private const val TYPE_ONLINE = 1
        private const val TYPE_OTHER = 2
    }

    /* Counter for cleaning device lists, when counter == 2 then clear lists
       Created for right filtering */
    private var counter = 0

    private var currentConnection by preferences.string(Constants.UNIDENTIFIED, Constants.CURRENT_CONNECTION_KEY)
    private val tvDeviceChunks: HashMap<Int, MutableList<TvDeviceInfo>?> = hashMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesListViewHolder {
        return DevicesListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.devices_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: DevicesListViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_RECENT -> {
                counter++
                tvDeviceChunks[TYPE_RECENT]?.let {
                    holder.bindData(holder.view.context.getString(R.string.mine_devices).toString(), it, DevicesAdapter(currentConnection, navigateCommand, connectCommand, true))
                }
            }
            TYPE_ONLINE -> {
                counter++
                tvDeviceChunks[TYPE_ONLINE]?.let {
                    holder.bindData(holder.view.context.getString(R.string.other_devices).toString(), it, DevicesAdapter(currentConnection, navigateCommand, connectCommand, false))
                }
            }
        }

        if (counter == 2) {
            counter = 0
            tvDeviceChunks[TYPE_RECENT] = null
            tvDeviceChunks[TYPE_ONLINE] = null
        }
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_RECENT
        1 -> TYPE_ONLINE
        else -> TYPE_OTHER
    }

    override fun getItemCount(): Int = tvDeviceChunks.size

    fun setRecentDevices(recentDevices: List<RecentDevice>) {
        tvDeviceChunks[TYPE_RECENT] = recentDevices.map {
            TvDeviceInfo(RecentDevice(it.actualName, it.userDefinedName, it.isOnline), null)
        }.toMutableList()

        tvDeviceChunks[TYPE_ONLINE]?.let { sortExistingRecentDevices() }
    }


    private fun sortExistingRecentDevices() {
        val onlineTvDevices: List<TvDeviceInfo> = tvDeviceChunks[TYPE_ONLINE]!!.toList()

        // Sorting RecentDevices by online state and current connection name
        tvDeviceChunks[TYPE_RECENT]?.forEach {
            it.recentDevice.isOnline = onlineTvDevices.any { pairOnlineDev -> pairOnlineDev.recentDevice.actualName == it.recentDevice.actualName }
            it.nsdServiceInfoWrapper = if (!it.recentDevice.isOnline) null else onlineTvDevices.first { pairOnlineDev -> pairOnlineDev.recentDevice.actualName == it.recentDevice.actualName }.nsdServiceInfoWrapper
        }

        tvDeviceChunks[TYPE_RECENT]?.sortBy { !it.recentDevice.isOnline }

        val connectedDeviceIndex = tvDeviceChunks[TYPE_RECENT]?.indexOfFirst { it.recentDevice.actualName == currentConnection }
        if (connectedDeviceIndex != -1)
            Collections.swap(tvDeviceChunks[TYPE_RECENT], 0, connectedDeviceIndex ?: 0)

        // Removing OnlineDevices elements that contains in RecentDevices
        tvDeviceChunks[TYPE_ONLINE]?.removeAll { tvDeviceChunks[TYPE_RECENT]?.any { devInfo -> devInfo.recentDevice.actualName == it.recentDevice.actualName } ?: false }

        tvDeviceChunks.forEach{ device -> device.value?.forEach { Timber.e(getItemViewType(device.key).toString() + " " + toString()) }}

        notifyDataSetChanged()
    }

}