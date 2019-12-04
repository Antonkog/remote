package com.wezom.kiviremote.presentation.home.kivi_catalog.adapters

import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wezom.kiviremote.App
import com.wezom.kiviremote.R
import com.wezom.kiviremote.databinding.ItemCatalogMovieBinding
import com.wezom.kiviremote.kivi_catalog.model.IviSeason
import com.wezom.kiviremote.presentation.base.recycler.LazyAdapter
import java.io.Serializable

class CatalogMoviesAdapter(onItemClickListener: OnItemClickListener<MovieData>) : LazyAdapter<MovieData, ItemCatalogMovieBinding>(onItemClickListener) {

    override fun bindData(data: MovieData, binding: ItemCatalogMovieBinding) {
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

    override fun getLayoutId(): Int = R.layout.item_catalog_movie

}

data class MovieData(val id: Int, val title: String, val description: String, val posterUrl: String, val seasons: List<IviSeason>? = null, val isSeries: Boolean = false) : Serializable