package com.wezom.kiviremote.presentation.home.recommendations.deep

import android.widget.ImageView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.decodeFromBase64
import com.wezom.kiviremote.common.dpToPx
import com.wezom.kiviremote.common.glide.GlideApp
import com.wezom.kiviremote.databinding.RecomendCardAppBinding
import com.wezom.kiviremote.net.model.ServerAppInfo
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter

class AppsDeepAdapter(itemClickListener: OnItemClickListener<ServerAppInfo>) : LazyAdapter<ServerAppInfo, RecomendCardAppBinding>(itemClickListener) {

    override fun bindData(data: ServerAppInfo, binding: RecomendCardAppBinding) {
        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(data) }
        val view = binding.root

        if (Constants.MEDIA_SHARE_TXT_ID == data.applicationName) {
            (view.findViewById(com.wezom.kiviremote.R.id.image_app) as ImageView).setImageResource(com.wezom.kiviremote.R.drawable.ic_media_share)
            (view.findViewById(com.wezom.kiviremote.R.id.image_app) as ImageView).alpha = 0.1f //todo: remove later
        }

        if (data.baseIcon != null && data.baseIcon.isNotEmpty())
            decodeFromBase64(data.baseIcon).let { bitmap ->
                val imageView = view.findViewById(R.id.image_app) as ImageView

                GlideApp.with(view.context)
                        .load(bitmap)
                        .override(dpToPx(view.context, 160), dpToPx(view.context, 90))
                        .into(imageView)

            }
    }

    override fun getLayoutId(): Int = R.layout.recomend_card_app
}