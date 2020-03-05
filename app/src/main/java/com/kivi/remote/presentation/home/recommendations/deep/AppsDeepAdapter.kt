package com.kivi.remote.presentation.home.recommendations.deep

import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kivi.remote.R
import com.kivi.remote.common.Constants
import com.kivi.remote.common.Constants.SMALL_BITMAP
import com.kivi.remote.common.KiviCache
import com.kivi.remote.common.glide.GlideApp
import com.kivi.remote.common.glide.PreviewsTransformation
import com.kivi.remote.databinding.RecomendCardAppBinding
import com.kivi.remote.net.model.ServerAppInfo
import com.kivi.remote.presentation.base.recycler.LazyAdapter
import timber.log.Timber

class AppsDeepAdapter(itemClickListener: OnItemClickListener<ServerAppInfo>, val cache: KiviCache) : LazyAdapter<ServerAppInfo, RecomendCardAppBinding>(itemClickListener) {

    override fun bindData(item: ServerAppInfo, binding: RecomendCardAppBinding) {
        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(item) }
        val imageView = binding.root.findViewById(R.id.image_app) as ImageView

        if (Constants.MEDIA_SHARE_TXT_ID == item.applicationName) {
            imageView.setImageResource(com.kivi.remote.R.drawable.ic_media_share)
            imageView.isClickable = false
        } else
            if (item.packageName != null)
                cache.get(item.packageName).let {

                    if(it?.width!= null &&  it.width > SMALL_BITMAP){

                        Timber.e(" PreviewsTransformation app width ${it.width} " + item.packageName)

                        GlideApp.with(imageView.context)
                                .load(it)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .transform(PreviewsTransformation(5, 5))
                                .into(imageView)
                    }
                    if(it?.width!= null &&  it.width <= SMALL_BITMAP){
                        Timber.e(" PreviewsTransformation2 app width ${it.width} " + item.packageName)

                        GlideApp.with(imageView.context)
                                .load(it)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerInside()
                                .into(imageView)
                    }
                }

    }

    override fun getLayoutId(): Int = R.layout.recomend_card_app
}