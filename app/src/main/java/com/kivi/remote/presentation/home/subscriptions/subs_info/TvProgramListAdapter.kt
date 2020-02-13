package com.kivi.remote.presentation.home.subscriptions.subs_info

import com.kivi.remote.R
import com.kivi.remote.databinding.TvprogramListItemBinding
import com.kivi.remote.presentation.base.recycler.LazyAdapter
import com.kivi.remote.presentation.base.recycler.initWithGridLay

class TvProgramListAdapter : LazyAdapter<TvProgramsChunk, TvprogramListItemBinding>() {

    override fun bindData(data: TvProgramsChunk, binding: TvprogramListItemBinding) {
        binding.tvTitle.text = data.categoryName
        binding.rvTvprograms.initWithGridLay(4, TvProgramAdapter(), data.imgUrls)
    }

    override fun getLayoutId(): Int = R.layout.tvprogram_list_item
}

data class TvProgramsChunk(val categoryName: String, val imgUrls: List<String>)