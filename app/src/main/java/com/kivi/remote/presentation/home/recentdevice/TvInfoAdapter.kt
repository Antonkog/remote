package com.kivi.remote.presentation.home.recentdevice

import com.kivi.remote.R
import com.kivi.remote.databinding.TvInfoItemBinding
import com.kivi.remote.presentation.base.recycler.LazyAdapter

class TvInfoAdapter : LazyAdapter<TvInfoUnit, TvInfoItemBinding>() {
    override fun bindData(data: TvInfoUnit, binding: TvInfoItemBinding) {
        binding.deviceUnitName.text = data.unitName
        binding.deviceUnitValue.text = data.unitValue
    }

    override fun getLayoutId(): Int = R.layout.tv_info_item
}

data class TvInfoUnit(val unitName: String, val unitValue: String)