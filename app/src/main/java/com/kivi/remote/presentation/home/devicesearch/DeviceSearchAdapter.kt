package com.kivi.remote.presentation.home.devicesearch

import android.net.nsd.NsdServiceInfo
import androidx.core.content.res.ResourcesCompat
import com.kivi.remote.App
import com.kivi.remote.R
import com.kivi.remote.common.extensions.removeMasks
import com.kivi.remote.databinding.DeviceSearchItemBinding
import com.kivi.remote.presentation.base.recycler.LazyAdapter

class DeviceSearchAdapter(itemClickListener: OnItemClickListener<NsdServiceInfo>) : LazyAdapter<NsdServiceInfo, DeviceSearchItemBinding>(itemClickListener) {

    override fun bindData(data: NsdServiceInfo, binding: DeviceSearchItemBinding) {
        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(data) }
        binding.title.text = data.serviceName.removeMasks()
        binding.icon.setImageDrawable(ResourcesCompat.getDrawable(binding.root.resources, if(App.isDarkMode())  R.drawable.ic_tv_d else  R.drawable.ic_tv, null))
    }

    override fun getLayoutId(): Int = R.layout.device_search_item
}