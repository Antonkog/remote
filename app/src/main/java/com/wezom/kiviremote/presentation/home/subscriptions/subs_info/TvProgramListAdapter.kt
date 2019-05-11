package com.wezom.kiviremote.presentation.home.subscriptions.subs_info

import com.wezom.kiviremote.R
import com.wezom.kiviremote.databinding.TvprogramListItemBinding
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter
import com.wezom.kiviremote.presentation.base.recycler.initWithGridLay

class TvProgramListAdapter : LazyAdapter<TvProgramsChunk, TvprogramListItemBinding>() {

    override fun bindData(data: TvProgramsChunk, binding: TvprogramListItemBinding) {
        binding.tvTitle.text = data.categoryName
        binding.rvTvprograms.initWithGridLay(4, TvProgramAdapter(), data.imgUrls)
    }

    override fun getLayoutId(): Int = R.layout.tvprogram_list_item
}

data class TvProgramsChunk(val categoryName: String, val imgUrls: List<String>)