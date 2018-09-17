package com.wezom.kiviremote.presentation.home.devicesearch

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.databinding.DeviceSearchItemBinding
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper
import com.wezom.kiviremote.presentation.base.BaseViewHolder


class DeviceSearchAdapter : RecyclerView.Adapter<DeviceSearchAdapter.DeviceSearchViewHolder>() {

    private val devices: MutableList<NsdServiceInfoWrapper> = arrayListOf()

    private var currentCheckedPosition: Int = -1
    private var lastCheckedPosition = -1

    data class DeviceSearchModel(
        val name: String,
        val checked: Boolean
    )

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DeviceSearchViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val binding = DeviceSearchItemBinding.inflate(inflater, parent, false)
        return DeviceSearchViewHolder(binding)
    }

    override fun getItemCount(): Int = devices.size

    override fun onBindViewHolder(holder: DeviceSearchViewHolder?, position: Int) {
        if (position == currentCheckedPosition)
            holder?.bind(DeviceSearchModel(devices[position].serviceName, true))
        else
            holder?.bind(DeviceSearchModel(devices[position].serviceName, false))
    }

    fun setData(items: List<NsdServiceInfoWrapper>) {
        devices.clear()
        devices.addAll(items)
        notifyDataSetChanged()
    }

    fun getCurrentSelectedItem(): NsdServiceInfoWrapper? =
        if (currentCheckedPosition != -1)
            devices[currentCheckedPosition]
        else
            null

    inner class DeviceSearchViewHolder(binding: DeviceSearchItemBinding) :
        BaseViewHolder<DeviceSearchItemBinding>(binding), View.OnClickListener {

        init {
            binding.run {
                deviceSearchContainer.setOnClickListener(this@DeviceSearchViewHolder)
            }
        }

        override fun bind(item: Any) {
            val model = item as DeviceSearchModel
            binding.model = model
            binding.executePendingBindings()
        }

        override fun onClick(v: View?) {
            lastCheckedPosition = currentCheckedPosition
            currentCheckedPosition = adapterPosition
            if (lastCheckedPosition != -1)
                notifyItemChanged(lastCheckedPosition)

            binding.checkbox.isChecked = true
        }
    }
}