package com.wezom.kiviremote.presentation.home.subscriptions.subs_info

import android.graphics.Color
import com.wezom.kiviremote.R
import com.wezom.kiviremote.databinding.TvprogramItemBinding
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter

class TvProgramAdapter : LazyAdapter<String, TvprogramItemBinding>() {

    override fun bindData(data: String, binding: TvprogramItemBinding) {
        binding.ivTvprogram.setBackgroundColor(Color.WHITE)
        binding.cvContainer.setOnClickListener {  }
    }

    override fun getLayoutId(): Int = R.layout.tvprogram_item
}