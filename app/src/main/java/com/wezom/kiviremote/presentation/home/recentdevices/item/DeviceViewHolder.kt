package com.wezom.kiviremote.presentation.home.recentdevices.item

import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.extensions.removeMasks
import com.wezom.kiviremote.persistence.model.RecentDevice

class DeviceViewHolder(val view: View, val command: (RecentDevice) -> Unit = {}) : RecyclerView.ViewHolder(view) {

    private val tvName = view.findViewById<TextView>(R.id.device_name)
    private val ivInfo = view.findViewById<ImageView>(R.id.device_info_btn)
    private val dividerBottomView = view.findViewById<View>(R.id.device_bottom_divider)

    val container: ConstraintLayout = view.findViewById(R.id.device_container)

    fun setRecentDevice(device: RecentDevice, currentConnection: String, isMineDevice: Boolean) {
        tvName.text = if (device.userDefinedName != null) device.userDefinedName else device.actualName.removeMasks()
        tvName.setTextColor(ResourcesCompat.getColor(view.context.resources,
                if (device.actualName == currentConnection || device.isOnline)
                    R.color.colorTextPrimary else
                    R.color.colorSecondaryText,
                null))

        container.setOnClickListener { command(device) }
        ivInfo.visibility = if (isMineDevice) View.VISIBLE else View.GONE
    }

}