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
import kotlin.collections.ArrayList

class DevicesListAdapter(preferences: SharedPreferences, val command: (RecentDevice) -> Unit) : RecyclerView.Adapter<DevicesListViewHolder>() {

    companion object {
        private const val RECENT = "Мои устройства"
        private const val ONLINE = "Другие устройства"

        private const val TYPE_RECENT = 0
        private const val TYPE_ONLINE = 1
        private const val TYPE_OTHER = 2

    }

    private var currentConnection by preferences.string(Constants.UNIDENTIFIED, Constants.CURRENT_CONNECTION_KEY)
    private val deviceChunks: HashMap<Int, List<RecentDevice>> = hashMapOf()

    init {
        deviceChunks[TYPE_RECENT] = listOf()
        deviceChunks[TYPE_ONLINE] = listOf()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesListViewHolder {
        return DevicesListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.devices_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: DevicesListViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_RECENT -> { holder.bindData(RECENT, deviceChunks[TYPE_RECENT]!!, DevicesAdapter(currentConnection, command)) }
            TYPE_ONLINE -> { holder.bindData(ONLINE, deviceChunks[TYPE_ONLINE]!!, DevicesAdapter(currentConnection, command)) }
        }
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_RECENT
        1 -> TYPE_ONLINE
        else -> TYPE_OTHER
    }

    override fun getItemCount(): Int = deviceChunks.size

    fun setRecentDevices(recentDevices: List<RecentDevice>) {
        deviceChunks[TYPE_RECENT] = recentDevices
        deviceChunks[TYPE_ONLINE]?.let { sortExistingRecentDevices(it) }
    }

    fun setOnlineDevices(onlineDevices: Set<NsdServiceInfoWrapper>) {
        deviceChunks[TYPE_ONLINE] = ArrayList(onlineDevices.map { RecentDevice(it.serviceName, null) }.toList())
        deviceChunks[TYPE_RECENT]?.let { sortExistingRecentDevices(deviceChunks[TYPE_ONLINE]!!) }
    }

    private fun sortExistingRecentDevices(onlineDevices: List<RecentDevice>) {
        deviceChunks[TYPE_RECENT]?.forEach { it.isOnline = onlineDevices.contains(it) }
        deviceChunks[TYPE_RECENT]?.sortedBy { it.isOnline }
        //deviceChunks[TYPE_ONLINE] = deviceChunks[TYPE_ONLINE]?.intersect(deviceChunks[TYPE_RECENT]!!)?.toList() ?: listOf()
        notifyDataSetChanged()
    }

}