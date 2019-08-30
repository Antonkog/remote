package com.wezom.kiviremote.presentation.home.recommendations.deep

import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.glide.GlideApp
import com.wezom.kiviremote.databinding.RecomendCardMovieSmallBinding
import com.wezom.kiviremote.net.model.Recommendation
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter

class RecsDeepAdapter(itemClickListener: OnItemClickListener<Recommendation>) : LazyAdapter<Recommendation, RecomendCardMovieSmallBinding>(itemClickListener) {

    override fun bindData(data: Recommendation, binding: RecomendCardMovieSmallBinding) {
        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(data) }

        GlideApp.with(binding.cardView.context).load(data.imageUrl)
//                    .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(5, 0, RoundedCornersTransformation.CornerType.ALL)))
                .into(binding.imageMovieSmall)
    }

    override fun getLayoutId(): Int = R.layout.recomend_card_movie_small
}