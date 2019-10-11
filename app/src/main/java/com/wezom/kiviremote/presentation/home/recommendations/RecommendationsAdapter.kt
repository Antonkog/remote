package com.wezom.kiviremote.presentation.home.recommendations

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants
import com.wezom.kiviremote.common.Constants.SMALL_BITMAP
import com.wezom.kiviremote.common.KiviCache
import com.wezom.kiviremote.common.dpToPx
import com.wezom.kiviremote.common.glide.GlideApp
import com.wezom.kiviremote.common.glide.PreviewsTransformation
import com.wezom.kiviremote.net.model.*
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import timber.log.Timber


class RecommendationsAdapter(private val listener: HorizontalCVContract.HorizontalCVListener, val cache: KiviCache)
    : RecyclerView.Adapter<RecommendationsAdapter.RecommendationsViewHolder<*>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationsViewHolder<*> {
        val context = parent.context

        return when (viewType) {
            TYPE_INPUTS -> {
                val view = LayoutInflater.from(context).inflate(R.layout.recomend_card_port, parent, false)
                InputsViewHolder(view, listener, data, cache)
            }
            TYPE_APPS -> {
                val view = LayoutInflater.from(context).inflate(R.layout.recomend_card_app, parent, false)
                AppsViewHolder(view, listener, data, cache)
            }
            TYPE_RECOMMENDATIONS -> {
                val view = LayoutInflater.from(context).inflate(R.layout.recomend_card_movie, parent, false)
                RecommendationsHolder(view, listener, data)
            }
            TYPE_TV_CHANNELS -> {
                val view = LayoutInflater.from(context).inflate(R.layout.recomend_card_channel, parent, false)
                ChannelsViewHolder(view, listener, data)
            }
            else -> throw Throwable("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecommendationsViewHolder<*>, position: Int) {
        val element = data[position]
        when (holder) {
            is RecommendationsHolder -> holder.bind(element as Recommendation)
            is InputsViewHolder -> holder.bind(element as Input)
            is AppsViewHolder -> holder.bind(element as ServerAppInfo)
            is ChannelsViewHolder -> holder.bind(element as Channel)
            else -> throw IllegalArgumentException()
        }
    }


    private val data: MutableList<Comparable<*>>


    companion object {
        val TYPE_FAVOURITES = 0
        val TYPE_RECOMMENDATIONS = 1
        val TYPE_TV_CHANNELS = 2
        val TYPE_APPS = 3
        val TYPE_INPUTS = 4
        val TYPE_NONE = -1
    }

    init {
        data = ArrayList()
    }

    fun swapData(newData: List<Comparable<*>>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }


    override fun getItemViewType(position: Int): Int {
        val comparable = data[position]
        return when (comparable) {
            is ServerAppInfo -> TYPE_APPS
            is Input -> TYPE_INPUTS
            is Recommendation -> TYPE_RECOMMENDATIONS
            is Channel -> TYPE_TV_CHANNELS
            else -> throw Throwable("Invalid type of data " + position)
        }
    }

    abstract class RecommendationsViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }


    class InputsViewHolder(val view: View, val listener: HorizontalCVContract.HorizontalCVListener,
                           val data: List<Comparable<*>>, val cache: KiviCache) : RecommendationsViewHolder<Input>(view), View.OnClickListener {

        override fun bind(item: Input) {
            view.setOnClickListener(this)
            val imageView = view.findViewById(R.id.image_port) as ImageView
            imageView.setImageResource(InputSourceHelper.INPUT_PORT.getPicById(item.intID))
            view.findViewById<TextView>(R.id.text).text = item.name
        }

        override fun onClick(view: View) {
            val position = adapterPosition

            if (position < 0) {
                return
            }

            val intut = data[position] as Input
            listener.onInputChosen(intut, position)
        }
    }

    class AppsViewHolder(val view: View, val listener: HorizontalCVContract.HorizontalCVListener,
                         val data: List<Comparable<*>>, val cache: KiviCache) : RecommendationsViewHolder<ServerAppInfo>(view), View.OnClickListener {

        override fun bind(item: ServerAppInfo) {
//            view.findViewById<CardView>(R.id.cardView).setBackgroundColor(if (App.isDarkMode()) ResourcesCompat.getColor(view.resources, R.color.colorBlack, null) else
//                ResourcesCompat.getColor(view.resources, R.color.colorWhite, null))


            val imageView = view.findViewById(com.wezom.kiviremote.R.id.image_app) as ImageView

            if (Constants.MEDIA_SHARE_TXT_ID == item.applicationName) {
                imageView.setImageResource(com.wezom.kiviremote.R.drawable.ic_media_share)
                imageView.isClickable = false
            } else
                if (item.packageName != null)
                    cache.get(item.packageName).let {

                        if (it?.width != null && it.width > SMALL_BITMAP) {

                            Timber.e(" PreviewsTransformation app width ${it?.width} " + item.packageName)

                            GlideApp.with(view.context)
                                    .load(it)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .transform(PreviewsTransformation(5, 5))
                                    .into(imageView)
                        }
                        if (it?.width != null && it.width <= SMALL_BITMAP) {
                            Timber.e(" PreviewsTransformation2 app width ${it?.width} " + item.packageName)

                            GlideApp.with(view.context)
                                    .load(it)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerInside()
                                    .into(imageView)
                        }

                    }

            view.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition

            if (position < 0) {
                return
            }

            val app = data[position] as ServerAppInfo
            listener.appChosenNeedOpen(app, position)
            Timber.d("  app " + app.applicationName + " onClick ")

        }
    }

    class RecommendationsHolder(val view: View, val listener: HorizontalCVContract.HorizontalCVListener,
                                val data: List<Comparable<*>>) : RecommendationsViewHolder<Recommendation>(view), View.OnClickListener {

        override fun bind(item: Recommendation) {
//            view.findViewById<CardView>(R.id.cardView).setBackgroundColor(if (App.isDarkMode()) ResourcesCompat.getColor(view.resources, R.color.colorBlack, null) else
//                ResourcesCompat.getColor(view.resources, R.color.colorWhite, null))

            view.setOnClickListener(this)

            val image: ImageView = view.findViewById(com.wezom.kiviremote.R.id.image_movie)

            GlideApp.with(view.context)
                    .load(item.imageUrl)
                    .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(dpToPx(view.context, 5), 0, RoundedCornersTransformation.CornerType.ALL)))
                    .into(image)

            Timber.d(" loading Recommendation " + item.imageUrl + " into view")
        }

        override fun onClick(view: View) {

            val position = adapterPosition

            if (position < 0) {
                return
            }

            val recommendItem = data[position] as Recommendation
            listener.onRecommendationChosen(recommendItem, position)
            Timber.d("  recItem " + recommendItem.name + " onClick ")
        }
    }


    class ChannelsViewHolder(val view: View, val listener: HorizontalCVContract.HorizontalCVListener,
                             val data: List<Comparable<*>>) : RecommendationsAdapter.RecommendationsViewHolder<Channel>(view), View.OnClickListener {

        override fun bind(item: Channel) {

//            view.findViewById<CardView>(R.id.cardView).setBackgroundColor(if (App.isDarkMode()) ResourcesCompat.getColor(view.resources, R.color.colorBlack, null) else
//                ResourcesCompat.getColor(view.resources, R.color.colorWhite, null))

            val image: ImageView = view.findViewById(com.wezom.kiviremote.R.id.image_channel)
            GlideApp.with(view.context).load(item.imageUrl)
                    .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(dpToPx(view.context, 5), 0, RoundedCornersTransformation.CornerType.ALL)))
                    .into(image)


//
//            GlideApp.with(view.context)
//                    .load(item.imageUrl)
//                    .into(DrawableImageViewTarget(image, /*waitForLayout=*/ true));


            view.setOnClickListener(this)
            Timber.d(" loading channel " + item.imageUrl + " into view")
        }

        override fun onClick(view: View) {

            val position = adapterPosition

            if (position < 0) {
                return
            }

            val recommendItem = data[position] as Channel
            listener.onChannelChosen(recommendItem, position)
            Timber.d("  recItem " + recommendItem.name + " onClick ")

        }
    }


}