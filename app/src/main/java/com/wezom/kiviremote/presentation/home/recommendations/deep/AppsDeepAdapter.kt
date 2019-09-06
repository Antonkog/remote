package com.wezom.kiviremote.presentation.home.recommendations.deep

import android.widget.ImageView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.KiviCache
import com.wezom.kiviremote.common.glide.GlideApp
import com.wezom.kiviremote.common.glide.PreviewsTransformation
import com.wezom.kiviremote.databinding.RecomendCardAppBinding
import com.wezom.kiviremote.net.model.ServerAppInfo
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter
import timber.log.Timber

class AppsDeepAdapter(itemClickListener: OnItemClickListener<ServerAppInfo>, val cache: KiviCache) : LazyAdapter<ServerAppInfo, RecomendCardAppBinding>(itemClickListener) {

    override fun bindData(item: ServerAppInfo, binding: RecomendCardAppBinding) {
        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(item) }
        val imageView = binding.root.findViewById(R.id.image_app) as ImageView

        if (Constants.MEDIA_SHARE_TXT_ID == item.applicationName) {
            imageView.setImageResource(com.wezom.kiviremote.R.drawable.ic_media_share)
            imageView.isClickable = false
        } else
            if (item.packageName != null)
                cache.get(item.packageName).let {



                    if(it?.width!= null &&  it.width > 640){

                        Timber.e(" PreviewsTransformation app width ${it?.width} " + item.packageName)

                        GlideApp.with(imageView.context)
                                .load(it)
                                .transform(PreviewsTransformation(5, 5))
                                .into(imageView)
                    }
                    if(it?.width!= null &&  it.width <= 640){
                        Timber.e(" PreviewsTransformation2 app width ${it?.width} " + item.packageName)

                        GlideApp.with(imageView.context)
                                .load(it)
//                                .transform(RoundedCornersTransformation(5, 5))
                                .fitCenter()
                                .into(imageView)
                    }
                }

    }

    override fun getLayoutId(): Int = R.layout.recomend_card_app
}