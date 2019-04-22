package com.wezom.kiviremote.presentation.home.recentdevices.list

import android.content.SharedPreferences
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.extensions.string
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper
import com.wezom.kiviremote.persistence.model.RecentDevice
import com.wezom.kiviremote.presentation.home.recentdevices.item.DevicesAdapter
import java.util.*

class DevicesListAdapter(preferences: SharedPreferences, val command: (RecentDevice) -> Unit) : RecyclerView.Adapter<DevicesListViewHolder>() {

    companion object {
        private const val TYPE_RECENT = 0
        private const val TYPE_ONLINE = 1
        private const val TYPE_OTHER = 2
    }

    /* Counter for cleaning device lists, when counter == 2 then clear lists
       Created for right filtering */
    private var counter = 0

    private var currentConnection by preferences.string(Constants.UNIDENTIFIED, Constants.CURRENT_CONNECTION_KEY)
    private val deviceChunks: HashMap<Int, MutableList<RecentDevice>?> = hashMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesListViewHolder {
        return DevicesListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.devices_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: DevicesListViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_RECENT -> {
                counter++
                deviceChunks[TYPE_RECENT]?.let {
                    holder.bindData(holder.view.context.getString(R.string.mine_devices).toString(), it, DevicesAdapter(currentConnection, command, true))
                }
            }
            TYPE_ONLINE -> {
                counter++
                deviceChunks[TYPE_ONLINE]?.let {
                    holder.bindData(holder.view.context.getString(R.string.other_devices).toString(), it, DevicesAdapter(currentConnection, command, false))
                }
            }
        }

        if (counter == 2) {
            counter = 0
            deviceChunks[TYPE_RECENT] = null
            deviceChunks[TYPE_ONLINE] = null
        }
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_RECENT
        1 -> TYPE_ONLINE
        else -> TYPE_OTHER
    }

    override fun getItemCount(): Int = deviceChunks.size

    fun setRecentDevices(recentDevices: List<RecentDevice>) {
        deviceChunks[TYPE_RECENT] = recentDevices.toMutableList()
        deviceChunks[TYPE_ONLINE]?.let { sortExistingRecentDevices(it) }
    }

    fun setOnlineDevices(onlineDevices: Set<NsdServiceInfoWrapper>) {
        deviceChunks[TYPE_ONLINE] = onlineDevices.map { RecentDevice(it.serviceName, null).also { it.isOnline = true } }.toMutableList()
        deviceChunks[TYPE_RECENT]?.let { sortExistingRecentDevices(deviceChunks[TYPE_ONLINE]!!) }
    }

    private fun sortExistingRecentDevices(onlineDevices: List<RecentDevice>) {
        // Sorting RecentDevices by online state and current connection name
        deviceChunks[TYPE_RECENT]?.forEach { it.isOnline = onlineDevices.any { onlineDev -> onlineDev.actualName == it.actualName } }
        deviceChunks[TYPE_RECENT]?.sortBy { !it.isOnline }
        Collections.swap(deviceChunks[TYPE_RECENT], 0, deviceChunks[TYPE_RECENT]?.indexOfFirst { it.actualName == currentConnection } ?: 0)

        // Removing OnlineDevices elements that contains in RecentDevices
        deviceChunks[TYPE_ONLINE]?.removeAll { deviceChunks[TYPE_RECENT]?.any { recentDev -> recentDev.actualName == it.actualName } ?: false }
        notifyDataSetChanged()
    }

}