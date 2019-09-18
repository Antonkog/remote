package com.wezom.kiviremote.presentation.home.recentdevices.item

import android.net.nsd.NsdServiceInfo
import android.support.constraint.ConstraintLayout
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.presentation.home.recentdevices.TvDeviceInfo

class DeviceViewHolder(private val view: View, val navigateCommand: (TvDeviceInfo) -> Unit = {}, val connectCommand: (NsdServiceInfo) -> Unit = {}) : RecyclerView.ViewHolder(view) {

    private val ivIcon = view.findViewById<ImageView>(R.id.device_icon)
    private val tvName = view.findViewById<TextView>(R.id.device_name)
    private val ivInfo = view.findViewById<ImageView>(R.id.device_info_btn)

    val container: ConstraintLayout = view.findViewById(R.id.device_container)

    fun setRecentDevice(deviceInfo: TvDeviceInfo, currentConnection: String, isShowInfoIcon: Boolean) {
        // Icon
        ivIcon.setImageResource(if (deviceInfo.recentDevice.isOnline) R.drawable.ic_tv_colored else R.drawable.ic_tv_no_colored)

        // Text Name
        tvName.text = if (deviceInfo.recentDevice.userDefinedName != null) deviceInfo.recentDevice.userDefinedName else deviceInfo.recentDevice.actualName
        tvName.setTextColor(ResourcesCompat.getColor(view.context.resources, if (deviceInfo.recentDevice.isOnline) R.color.colorTextPrimary else com.wezom.kiviremote.R.color.colorSecondaryText, null))

        // Info button
        ivInfo.visibility = if (isShowInfoIcon) View.VISIBLE else View.GONE
        ivInfo.setOnClickListener { navigateCommand(deviceInfo) }

        // Container click event
        if (deviceInfo.recentDevice.actualName != currentConnection)
            deviceInfo.nsdServiceInfoWrapper?.let { nsdWrapper -> container.setOnClickListener { showDialog(deviceInfo.recentDevice.actualName, nsdWrapper) } }
    }

    private fun showDialog(tvName: String, nsdServiceInfoWrapper: NsdServiceInfo) {// Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle(view.context.resources.getString(R.string.conection))
                .setMessage(view.context.resources.getString(R.string.really_connect, tvName))
                .setPositiveButton(view.context.resources.getString(R.string.connect).toUpperCase()) { _, _ ->
                    connectCommand(nsdServiceInfoWrapper)
                }
                .setNegativeButton(view.context.resources.getString(R.string.cancel).toUpperCase()) { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)

        builder.create().show()
    }

}