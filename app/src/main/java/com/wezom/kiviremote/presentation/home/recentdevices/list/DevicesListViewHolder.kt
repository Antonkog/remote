package com.wezom.kiviremote.presentation.home.recentdevices.list

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.presentation.home.recentdevices.TvDeviceInfo
import com.wezom.kiviremote.presentation.home.recentdevices.item.DevicesAdapter

class DevicesListViewHolder(val view: View): RecyclerView.ViewHolder(view) {

    private var tvTitle = view.findViewById<TextView>(R.id.tv_title)
    private var devicesContainer = view.findViewById<RecyclerView>(R.id.devices_container)

    fun bindData(title: String, devices: List<TvDeviceInfo>, adapter: DevicesAdapter) {
        tvTitle.text = title
        devicesContainer.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }

        adapter.swapData(devices)
    }
}