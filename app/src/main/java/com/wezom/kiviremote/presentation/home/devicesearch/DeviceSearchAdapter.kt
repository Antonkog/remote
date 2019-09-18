package com.wezom.kiviremote.presentation.home.devicesearch

import android.net.nsd.NsdServiceInfo
import android.support.v4.content.res.ResourcesCompat
import com.wezom.kiviremote.App
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.extensions.removeMasks
import com.wezom.kiviremote.databinding.DeviceSearchItemBinding
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter

class DeviceSearchAdapter(itemClickListener: OnItemClickListener<NsdServiceInfo>) : LazyAdapter<NsdServiceInfo, DeviceSearchItemBinding>(itemClickListener) {

    override fun bindData(data: NsdServiceInfo, binding: DeviceSearchItemBinding) {
        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(data) }
        binding.title.text = data.serviceName.removeMasks()
        binding.icon.setImageDrawable(ResourcesCompat.getDrawable(binding.root.resources, if(App.isDarkMode())  R.drawable.ic_tv_d else  R.drawable.ic_tv, null))
    }

    override fun getLayoutId(): Int = R.layout.device_search_item
}