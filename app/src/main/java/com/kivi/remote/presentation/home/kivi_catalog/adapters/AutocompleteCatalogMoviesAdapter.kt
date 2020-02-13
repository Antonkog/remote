package com.kivi.remote.presentation.home.kivi_catalog.adapters

import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kivi.remote.App
import com.kivi.remote.R
import com.kivi.remote.databinding.ItemAutocompleteCatalogMovieBinding
import com.kivi.remote.presentation.base.recycler.LazyAdapter

class AutocompleteCatalogMoviesAdapter(onItemClickListener: OnItemClickListener<MovieData>) : LazyAdapter<MovieData, ItemAutocompleteCatalogMovieBinding>(onItemClickListener) {

    override fun bindData(data: MovieData, binding: ItemAutocompleteCatalogMovieBinding) {
        binding.cvRoot.setCardBackgroundColor(ResourcesCompat.getColor(binding.root.context.resources, if (App.isDarkMode()) R.color.mine_shaft_3 else R.color.autocomplete_back_light, null))
        binding.tvTitle.setTextColor(ResourcesCompat.getColor(binding.root.context.resources, if (App.isDarkMode()) R.color.colorWhite else R.color.limed_spruce, null))
        binding.tvDescription.setTextColor(ResourcesCompat.getColor(binding.root.context.resources, if (App.isDarkMode()) R.color.autocomplete_genre_dark else R.color.autocomplete_genre_light, null))

        binding.tvTitle.text = data.title
        binding.tvDescription.text = data.description

        Glide.with(binding.root.context)
                .load(data.posterUrl)
                .apply(RequestOptions().error(R.drawable.placeholder_image))
                .into(binding.ivContent)

        binding.root.setOnClickListener { itemClickListener?.onLazyItemClick(data) }
    }

    override fun getLayoutId(): Int = R.layout.item_autocomplete_catalog_movie

}