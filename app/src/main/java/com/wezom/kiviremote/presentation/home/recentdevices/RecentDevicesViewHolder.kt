package com.wezom.kiviremote.presentation.home.recentdevices

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.wezom.kiviremote.R


class RecentDevicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val deviceOnlineStatus: ImageView = itemView.findViewById(R.id.devices_online_status)

    val deviceDeleteButton: ImageView = itemView.findViewById(R.id.devices_delete)

    val deviceTvStatus: ImageView = itemView.findViewById(R.id.devices_tv_status)

    val deviceTextStatus: TextView = itemView.findViewById(R.id.devices_text_status)

    val deviceName: TextView = itemView.findViewById(R.id.devices_name)

    val container: ConstraintLayout = itemView.findViewById(R.id.devices_container)
}