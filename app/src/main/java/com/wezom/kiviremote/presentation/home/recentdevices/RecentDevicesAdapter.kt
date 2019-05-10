package com.wezom.kiviremote.presentation.home.recentdevices

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants.CURRENT_CONNECTION_KEY
import com.wezom.kiviremote.common.Constants.UNIDENTIFIED
import com.wezom.kiviremote.common.extensions.getTvUniqueId
import com.wezom.kiviremote.common.extensions.removeMasks
import com.wezom.kiviremote.common.extensions.string
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper
import com.wezom.kiviremote.persistence.AppDatabase
import com.wezom.kiviremote.persistence.model.RecentDevice
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*


class RecentDevicesAdapter(private val context: Context,
                           private val database: AppDatabase,
                           preferences: SharedPreferences,
                           private val command: (RecentDevice) -> Unit)
    : RecyclerView.Adapter<RecentDevicesViewHolder>() {

    private var currentConnection by preferences.string(UNIDENTIFIED, CURRENT_CONNECTION_KEY)

    private var toDelete: MutableList<RecentDevice> = ArrayList()
    private var devices: MutableList<RecentDevice>? = null
    private var devicesOnline: Set<NsdServiceInfoWrapper>? = null

    private lateinit var initialList: List<RecentDevice>

    private var showDelete: Boolean = false
    var allowNavigation: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentDevicesViewHolder {
        return RecentDevicesViewHolder(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.recent_devices_item, parent, false)
        )
    }

    override fun getItemCount(): Int = if (devices != null) devices!!.size else 0

    override fun onBindViewHolder(holder: RecentDevicesViewHolder, position: Int) {
        Timber.d("Devices: $devices")
        val device = devices!![position]
        val resources = context.resources

        holder.apply {
            // TODO Refactor this mess
            if (device.userDefinedName != null)
                deviceName.text = device.userDefinedName
            else
                deviceName.text = device.actualName.removeMasks()

            if (device.actualName == currentConnection) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    deviceTvStatus.setImageResource(R.drawable.ic_devices_tv_active)
                    deviceOnlineStatus.setImageResource(R.drawable.ic_devices_online)
                } else {
                    deviceTvStatus.setImageResource(R.drawable.ic_devices_tv_active)
                    deviceOnlineStatus.setImageResource(R.drawable.ic_devices_online)
                }

                deviceTextStatus.setTextColor(ContextCompat.getColor(deviceTextStatus.context, R.color.colorPrimary))

                deviceTextStatus.text = context.resources.getString(R.string.connected)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    deviceTvStatus.setImageResource(R.drawable.ic_devices_tv_inactive)
                } else {
                    deviceTvStatus.setImageResource(R.drawable.ic_devices_tv_inactive)
                }

                if (device.isOnline) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        deviceOnlineStatus.setImageResource(R.drawable.ic_devices_online)
                    } else {
                        deviceOnlineStatus.setImageResource(R.drawable.ic_devices_online)
                    }

                    deviceTextStatus.setTextColor(ContextCompat.getColor(deviceTextStatus.context, R.color.colorPrimary))

                    deviceTextStatus.text = context.resources.getString(R.string.online)
                } else {
                    deviceTextStatus.setTextColor(ContextCompat.getColor(deviceTextStatus.context, R.color.colorSecondaryText))

                    deviceTextStatus.text = context.resources.getText(R.string.offline)
                    deviceOnlineStatus.setImageDrawable(null)
                }
            }

            if (showDelete) {
                deviceOnlineStatus.visibility = View.GONE
                deviceDeleteButton.visibility = View.VISIBLE
                container.setOnClickListener(null)
            } else {
                deviceOnlineStatus.visibility = View.VISIBLE
                deviceDeleteButton.visibility = View.GONE
            }

            container.setOnClickListener {
                if (allowNavigation)
                    command(device)
            }

            deviceDeleteButton.setOnClickListener {
                markForDeletion(device)
                devices?.remove(device)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount - position)
            }
        }
    }

    private fun markForDeletion(device: RecentDevice) = toDelete.add(device)

    fun confirmDeletion() {
        Timber.d("List of devices to delete: $toDelete")
        Completable.complete()
                .subscribeOn(Schedulers.io())
                .subscribe({
                    database.recentDeviceDao().deleteDevices(toDelete)
                    toDelete.clear()
                }, { Timber.e(it, it.message) })
    }

    fun discardChanges() {
        if(::initialList.isInitialized) {
            devices = ArrayList(initialList)
            notifyDataSetChanged()
        }
    }

    fun setNewDevices(devices: List<RecentDevice>) {
        Timber.d("New list of devices has arrived, size: ${devices.size}")
        this.devices = devices.toMutableList()
        initialList = ArrayList(devices)
        if (devicesOnline != null) onDevicesOnlineStatusChange(devicesOnline!!)
    }

    fun showDelete() {
        showDelete = !showDelete
        notifyDataSetChanged()
    }

    fun onDevicesOnlineStatusChange(devicesOnline: Set<NsdServiceInfoWrapper>) {//nsd change
        this.devicesOnline = devicesOnline
        for (onlineDevice in devicesOnline) {
            devices?.let {
                it.asSequence()
                        .filter { onlineDevice.service.serviceName.contains(it.actualName.getTvUniqueId()) }
                        .forEach { it.isOnline = true }
            }
        }

        if (devices != null) {
            devices?.sort()
            for (m in 0 until devices!!.size) {
                if (devices!!.get(m).actualName == currentConnection) {
                    Collections.swap(devices, 0, m)
                }
            }
        }

        notifyDataSetChanged()
    }
}