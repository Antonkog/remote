package com.kivi.remote.presentation.home.kivi_catalog.adapters

import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kivi.remote.App
import com.kivi.remote.R
import com.kivi.remote.databinding.ItemCatalogSeriesEpisodeBinding
import com.kivi.remote.presentation.base.recycler.LazyAdapter

class CatalogEpisodeAdapter(onItemClickListener: OnItemClickListener<MovieData>) : LazyAdapter<MovieData, ItemCatalogSeriesEpisodeBinding>(onItemClickListener) {

    override fun bindData(data: MovieData, binding: ItemCatalogSeriesEpisodeBinding) {
        binding.cvRoot.setCardBackgroundColor(ResourcesCompat.getColor(binding.root.context.resources, if (App.isDarkMode()) R.color.cod_gray else R.color.athens_gray, null))
        binding.tvTitle.setTextColor(ResourcesCompat.getColor(binding.root.context.resources, if (App.isDarkMode()) R.color.colorWhite else R.color.mine_shaft_33, null))
        binding.tvDescription.setTextColor(ResourcesCompat.getColor(binding.root.context.resources, R.color.gray, null))

        binding.tvTitle.text = data.title
        binding.tvDescription.text = data.description

        Glide.with(binding.root.context)
                .load(data.posterUrl)
                .apply(RequestOptions().error(R.drawable.placeholder_image))
                .into(binding.ivContent)

        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(data) }
    }

    override fun getLayoutId(): Int = R.layout.item_catalog_series_episode

}