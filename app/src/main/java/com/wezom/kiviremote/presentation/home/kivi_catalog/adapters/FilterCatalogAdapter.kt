package com.wezom.kiviremote.presentation.home.kivi_catalog.adapters

import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.wezom.kiviremote.App
import com.wezom.kiviremote.R
import com.wezom.kiviremote.databinding.ItemCatologFilterBinding
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter

class FilterCatalogAdapter : LazyAdapter<CatalogFilter, ItemCatologFilterBinding>() {

    override fun bindData(data: CatalogFilter, binding: ItemCatologFilterBinding) {
        binding.tvTitle.setTextColor(ResourcesCompat.getColor(binding.root.context.resources, if (App.isDarkMode()) R.color.white_87 else R.color.black_87, null))
        binding.tvTitle.text = data.filterName
        binding.ivChecked.visibility = if (data.isChecked) View.VISIBLE else View.INVISIBLE
        binding.root.setOnClickListener {
            data.isChecked = !data.isChecked
            binding.ivChecked.visibility = if (data.isChecked) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun getLayoutId(): Int = R.layout.item_catolog_filter

}

data class CatalogFilter(val id: Int, val filterName: String, val startYear: Int, val endYear: Int, var isChecked: Boolean = false) {

    fun clone(): CatalogFilter {
        return CatalogFilter(id, filterName, startYear, endYear, isChecked)
    }

}