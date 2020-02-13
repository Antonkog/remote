package com.kivi.remote.presentation.home.subscriptions.subs_info

import android.graphics.Color
import com.kivi.remote.R
import com.kivi.remote.databinding.TvprogramItemBinding
import com.kivi.remote.presentation.base.recycler.LazyAdapter

class TvProgramAdapter : LazyAdapter<String, TvprogramItemBinding>() {

    override fun bindData(data: String, binding: TvprogramItemBinding) {
        binding.ivTvprogram.setBackgroundColor(Color.WHITE)
        binding.cvContainer.setOnClickListener {  }
    }

    override fun getLayoutId(): Int = R.layout.tvprogram_item
}