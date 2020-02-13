package com.kivi.remote.presentation.home.subscriptions.subs_price_list

import com.kivi.remote.R
import com.kivi.remote.databinding.PriceListItemBinding
import com.kivi.remote.presentation.base.recycler.LazyAdapter
import java.io.Serializable

class PriceAdapter(itemClickListener: OnItemClickListener<PricePerTime>) : LazyAdapter<PricePerTime, PriceListItemBinding>(itemClickListener) {
    override fun bindData(data: PricePerTime, binding: PriceListItemBinding) {
        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(data) }
        binding.tvTimeAmount.text = data.timeAmount.toString()
        binding.tvTimeUnit.text = data.timeUnit
        binding.tvPriceAmount.text = data.priceAmount.toString()
        binding.tvPriceUnit.text = data.priceUnit
    }

    override fun getLayoutId(): Int = R.layout.price_list_item
}

data class PricePerTime(val timeAmount: Int, val timeUnit: String, val priceAmount: Int, val priceUnit: String) : Serializable