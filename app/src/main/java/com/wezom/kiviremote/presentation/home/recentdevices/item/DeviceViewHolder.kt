package com.wezom.kiviremote.presentation.home.recentdevices.item

import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.extensions.removeMasks
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper
import com.wezom.kiviremote.presentation.home.recentdevices.TvDeviceInfo

class DeviceViewHolder(private val view: View, val navigateCommand: (TvDeviceInfo) -> Unit = {}, val connectCommand: (NsdServiceInfoWrapper) -> Unit = {}) : RecyclerView.ViewHolder(view) {

    private val ivIcon = view.findViewById<ImageView>(R.id.device_icon)
    private val tvName = view.findViewById<TextView>(R.id.device_name)
    private val ivInfo = view.findViewById<ImageView>(R.id.device_info_btn)

    val container: ConstraintLayout = view.findViewById(R.id.device_container)

    fun setRecentDevice(deviceInfo: TvDeviceInfo, currentConnection: String, isShowInfoIcon: Boolean) {
        // Icon
        ivIcon.setImageResource(if (deviceInfo.recentDevice.isOnline) R.drawable.ic_tv_colored else R.drawable.ic_tv_no_colored)

        // Text Name
        tvName.text = if (deviceInfo.recentDevice.userDefinedName != null) deviceInfo.recentDevice.userDefinedName else deviceInfo.recentDevice.actualName.removeMasks()
        tvName.setTextColor(ResourcesCompat.getColor(view.context.resources, if (deviceInfo.recentDevice.isOnline) R.color.colorTextPrimary else com.wezom.kiviremote.R.color.colorSecondaryText, null))

        // Info button
        ivInfo.visibility = if (isShowInfoIcon) View.VISIBLE else View.GONE
        ivInfo.setOnClickListener { navigateCommand(deviceInfo) }

        // Container click event
        if (deviceInfo.recentDevice.actualName != currentConnection)
        deviceInfo.nsdServiceInfoWrapper?.let { nsdWrapper -> container.setOnClickListener { showDialog(deviceInfo.recentDevice.actualName, nsdWrapper) } }
    }

    private fun showDialog(tvName: String, nsdServiceInfoWrapper: NsdServiceInfoWrapper) {// Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle("Подключение")
                .setMessage("Вы действительно хотите подключить $tvName?")
                .setPositiveButton("ПОДКЛЮЧИТЬ") { _, _ ->
                    connectCommand(nsdServiceInfoWrapper)
                }
                .setNegativeButton("ОТМЕНА") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)

        builder.create().show()
    }

}