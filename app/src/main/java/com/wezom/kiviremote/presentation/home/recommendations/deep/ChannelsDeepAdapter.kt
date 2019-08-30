package com.wezom.kiviremote.presentation.home.recommendations.deep

import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.glide.GlideApp
import com.wezom.kiviremote.databinding.RecomendCardChannelBinding
import com.wezom.kiviremote.net.model.Channel
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter

class ChannelsDeepAdapter(itemClickListener: OnItemClickListener<Channel>) : LazyAdapter<Channel, RecomendCardChannelBinding>(itemClickListener) {

    override fun bindData(data: Channel, binding: RecomendCardChannelBinding) {
        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(data) }

        GlideApp.with(binding.cardView.context).load(data.imageUrl)
//                    .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(5, 0, RoundedCornersTransformation.CornerType.ALL)))
                .into(binding.imageChannel)
    }

    override fun getLayoutId(): Int = R.layout.recomend_card_channel
}