package com.wezom.kiviremote.presentation.home.recentdevice

import com.wezom.kiviremote.R
import com.wezom.kiviremote.databinding.TvInfoItemBinding
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter

class TvInfoAdapter : LazyAdapter<TvInfoUnit, TvInfoItemBinding>() {
    override fun bindData(data: TvInfoUnit, binding: TvInfoItemBinding) {
        binding.deviceUnitName.text = data.unitName
        binding.deviceUnitValue.text = data.unitValue
    }

    override fun getLayoutId(): Int = R.layout.tv_info_item
}

data class TvInfoUnit(val unitName: String, val unitValue: String)