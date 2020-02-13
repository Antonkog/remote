package com.kivi.remote.presentation.home.subscriptions.subs_tariff_plans

import com.kivi.remote.R
import com.kivi.remote.databinding.TariffListItemBinding
import com.kivi.remote.presentation.base.recycler.LazyAdapter
import com.kivi.remote.presentation.base.recycler.initWithGridLay

class TariffListAdapter : LazyAdapter<TariffChunk, TariffListItemBinding>()  {
    override fun bindData(data: TariffChunk, binding: TariffListItemBinding) {
        binding.tvTariffName.text = data.tariffPlan
        binding.tvTariffPrice.text = "${data.pricePerMonth} грн./мес"
        binding.rlTariffs.initWithGridLay(4, TariffAdapter(), data.imgUrls)
    }

    override fun getLayoutId(): Int = R.layout.tariff_list_item
}

data class TariffChunk(val tariffPlan: String, val pricePerMonth: Int, val imgUrls: List<String>)