package com.wezom.kiviremote.presentation.home.devicesearch

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wezom.kiviremote.common.recycler.RecyclerViewClickListener
import com.wezom.kiviremote.databinding.DeviceSearchItemBinding
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper
import com.wezom.kiviremote.presentation.base.BaseViewHolder

class DeviceSearchAdapter(val onItemClickListener: RecyclerViewClickListener) : RecyclerView.Adapter<DeviceSearchAdapter.DeviceSearchViewHolder>() {

    private val devices: MutableList<NsdServiceInfoWrapper> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DeviceSearchViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val binding = DeviceSearchItemBinding.inflate(inflater, parent, false)
        return DeviceSearchViewHolder(binding)
    }

    override fun getItemCount(): Int = devices.size

    override fun onBindViewHolder(holder: DeviceSearchViewHolder?, position: Int) {
        holder?.bind(DeviceSearchModel(devices[position].serviceName, false))
    }

    fun setData(items: List<NsdServiceInfoWrapper>) {
        devices.clear()
        devices.addAll(items)
        notifyDataSetChanged()
    }

    inner class DeviceSearchViewHolder(binding: DeviceSearchItemBinding) : BaseViewHolder<DeviceSearchItemBinding>(binding) {
        init {
            binding.deviceSearchContainer.setOnClickListener { view -> onItemClickListener.recyclerViewListClicked(view, adapterPosition) }
        }

        override fun bind(item: Any) {
            val model = item as DeviceSearchModel
            binding.model = model
            binding.executePendingBindings()
        }
    }

    data class DeviceSearchModel(val name: String, val checked: Boolean)
}