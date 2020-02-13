package com.kivi.remote.presentation.home.recommendations.deep

import com.bumptech.glide.request.RequestOptions
import com.kivi.remote.R
import com.kivi.remote.common.glide.GlideApp
import com.kivi.remote.databinding.RecomendCardMovieSmallBinding
import com.kivi.remote.net.model.Recommendation
import com.kivi.remote.presentation.base.recycler.LazyAdapter
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class RecsDeepAdapter(itemClickListener: OnItemClickListener<Recommendation>) : LazyAdapter<Recommendation, RecomendCardMovieSmallBinding>(itemClickListener) {

    override fun bindData(data: Recommendation, binding: RecomendCardMovieSmallBinding) {
        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(data) }

        GlideApp.with(binding.root.context).load(data.imageUrl)
                    .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(5, 0, RoundedCornersTransformation.CornerType.ALL)))
                .into(binding.imageMovie)
    }

    override fun getLayoutId(): Int = R.layout.recomend_card_movie_small
}