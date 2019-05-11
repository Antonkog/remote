package com.wezom.kiviremote.presentation.home.subscriptions.subs_tariff_plans

import android.graphics.Color
import com.wezom.kiviremote.R
import com.wezom.kiviremote.databinding.TariffItemBinding
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter

class TariffAdapter : LazyAdapter<String, TariffItemBinding>() {
    override fun bindData(data: String, binding: TariffItemBinding) {
        binding.ivTvprogram.setBackgroundColor(Color.WHITE)
    }

    override fun getLayoutId(): Int = R.layout.tariff_item
}