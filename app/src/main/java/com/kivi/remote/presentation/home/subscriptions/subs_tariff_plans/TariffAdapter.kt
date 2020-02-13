package com.kivi.remote.presentation.home.subscriptions.subs_tariff_plans

import android.graphics.Color
import com.kivi.remote.R
import com.kivi.remote.databinding.TariffItemBinding
import com.kivi.remote.presentation.base.recycler.LazyAdapter

class TariffAdapter : LazyAdapter<String, TariffItemBinding>() {
    override fun bindData(data: String, binding: TariffItemBinding) {
        binding.ivTvprogram.setBackgroundColor(Color.WHITE)
    }

    override fun getLayoutId(): Int = R.layout.tariff_item
}