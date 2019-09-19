package com.wezom.kiviremote.presentation.home.recentdevices

import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.extensions.removeMasks
import com.wezom.kiviremote.persistence.model.RecentDevice

class RecentDevicesAdapter(private val listener: RecentDevicesListener, private val currentConnection: String) : RecyclerView.Adapter<RecentDevicesAdapter.RowsViewHolder<*>>() {
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_DEVICE_ITEM = 1
    }

    abstract class RowsViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    private val data: MutableList<Comparable<*>>

    init {
        data = ArrayList()
    }

    fun swapData(newData: List<Comparable<*>>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowsViewHolder<*> {
        val context = parent.context
        return when (viewType) {
            RecentDevicesAdapter.TYPE_DEVICE_ITEM -> {
                val view = LayoutInflater.from(context).inflate(R.layout.device_item, parent, false)
                RecentDevicesAdapter.RecentDeviceHolder(view, currentConnection, listener, data)
            }

            RecentDevicesAdapter.TYPE_HEADER -> {
                val view = LayoutInflater.from(context).inflate(R.layout.header_item, parent, false)
                RecentDevicesAdapter.HeaderHolder(view, data)
            }
            else -> throw Throwable("Invalid view type")
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RowsViewHolder<*>?, position: Int) {
        val element = data[position]
        when (holder) {
            is RecentDevicesAdapter.RecentDeviceHolder -> holder.bind(element as RecentDevice)
            is RecentDevicesAdapter.HeaderHolder -> holder.bind(element as String)
            else -> throw IllegalArgumentException()
        }
    }



    override fun getItemViewType(position: Int): Int {
        val comparable = data[position]
        return when (comparable) {
            is RecentDevice -> TYPE_DEVICE_ITEM
            is String -> TYPE_HEADER
            else -> throw Throwable("Invalid type of data " + position)
        }
    }

    class RecentDeviceHolder(val view: View, val currentConnection: String ,  val listener: RecentDevicesListener,  val data: List<Comparable<*>>) : RecentDevicesAdapter.RowsViewHolder<RecentDevice>(view) {

        override fun bind(recentDevice: RecentDevice) {

            view.setOnClickListener{ listener.connectDeviceChosen(recentDevice, adapterPosition)}

            val tvName = view.findViewById(R.id.device_name) as TextView
            val ivIcon = view.findViewById(R.id.device_icon) as ImageView
            val ivInfo = view.findViewById(R.id.device_info_btn) as ImageView

            // Icon
            ivIcon.setImageResource(if (recentDevice.isOnline) R.drawable.ic_tv_colored else R.drawable.ic_tv_no_colored)

            // Text Name
            tvName.text = if (recentDevice.userDefinedName != null) recentDevice.userDefinedName else recentDevice.actualName.removeMasks()
            tvName.setTextColor(ResourcesCompat.getColor(view.context.resources, if (recentDevice.isOnline) R.color.colorTextPrimary else com.wezom.kiviremote.R.color.colorSecondaryText, null))

            // Info button
            ivInfo.visibility = if (recentDevice.wasConnected != null && recentDevice.wasConnected != 0L) View.VISIBLE else View.GONE
            ivInfo.setOnClickListener { listener.infoBtnChosen(recentDevice, adapterPosition) }

        }
    }



    class HeaderHolder(val view: View, val data: List<Comparable<*>>) : RecentDevicesAdapter.RowsViewHolder<String>(view) {
        override fun bind(item: String) {
            val headerView = view.findViewById(R.id.recent_header) as TextView
            headerView.text = item
        }
    }
}