package com.wezom.kiviremote.presentation.home.subscriptions.subs_tariff_plans

import com.wezom.kiviremote.R
import com.wezom.kiviremote.databinding.TariffListItemBinding
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter
import com.wezom.kiviremote.presentation.base.recycler.initWithGridLay

class TariffListAdapter : LazyAdapter<TariffChunk, TariffListItemBinding>()  {
    override fun bindData(data: TariffChunk, binding: TariffListItemBinding) {
        binding.tvTariffName.text = data.tariffPlan
        binding.tvTariffPrice.text = "${data.pricePerMonth} грн./мес"
        binding.rlTariffs.initWithGridLay(4, TariffAdapter(), data.imgUrls)
    }

    override fun getLayoutId(): Int = R.layout.tariff_list_item
}

data class TariffChunk(val tariffPlan: String, val pricePerMonth: Int, val imgUrls: List<String>)