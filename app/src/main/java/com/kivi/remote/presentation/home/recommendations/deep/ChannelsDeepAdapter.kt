package com.kivi.remote.presentation.home.recommendations.deep

import com.bumptech.glide.request.RequestOptions
import com.kivi.remote.R
import com.kivi.remote.common.glide.GlideApp
import com.kivi.remote.databinding.RecomendCardChannelBinding
import com.kivi.remote.net.model.Channel
import com.kivi.remote.presentation.base.recycler.LazyAdapter
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class ChannelsDeepAdapter(itemClickListener: OnItemClickListener<Channel>) : LazyAdapter<Channel, RecomendCardChannelBinding>(itemClickListener) {

    override fun bindData(data: Channel, binding: RecomendCardChannelBinding) {
        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(data) }

        GlideApp.with(binding.root.context).load(data.imageUrl)
                    .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(5, 0, RoundedCornersTransformation.CornerType.ALL)))
                .into(binding.imageChannel)
    }

    override fun getLayoutId(): Int = R.layout.recomend_card_channel
}