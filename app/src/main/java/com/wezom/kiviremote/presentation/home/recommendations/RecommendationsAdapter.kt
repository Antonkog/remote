package com.wezom.kiviremote.presentation.home.recommendations

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.decodeFromBase64
import com.wezom.kiviremote.common.glide.GlideApp
import com.wezom.kiviremote.net.model.Channel
import com.wezom.kiviremote.net.model.Input
import com.wezom.kiviremote.net.model.Recommendation
import com.wezom.kiviremote.net.model.ServerAppInfo
import timber.log.Timber

class RecommendationsAdapter(private val listener: HorizontalCVContract.HorizontalCVListener)
    : RecyclerView.Adapter<RecommendationsAdapter.RecommendationsViewHolder<*>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationsViewHolder<*> {
        val context = parent.context

        return when (viewType) {
            TYPE_INPUTS -> {
                val view = LayoutInflater.from(context).inflate(R.layout.recomend_card_port, parent, false)
                InputsViewHolder(view, listener, data)
            }
            TYPE_APPS -> {
                val view = LayoutInflater.from(context).inflate(R.layout.recomend_card_app, parent, false)
                AppsViewHolder(view, listener, data)
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
                           val data: List<Comparable<*>>) : RecommendationsViewHolder<Input>(view), View.OnClickListener {

        override fun bind(item: Input) {
            view.setOnClickListener(this)

            if (item.inputIcon != null && item.inputIcon.isNotEmpty())
                decodeFromBase64(item.inputIcon).let { bitmap ->
                    val imageView = view.findViewById(R.id.image) as ImageView

                    imageView.setImageBitmap(bitmap)

                }

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
                         val data: List<Comparable<*>>) : RecommendationsViewHolder<ServerAppInfo>(view), View.OnClickListener {

        override fun bind(item: ServerAppInfo) {
//            view.findViewById<CardView>(R.id.cardView).setBackgroundColor(if (App.isDarkMode()) ResourcesCompat.getColor(view.resources, R.color.colorBlack, null) else
//                ResourcesCompat.getColor(view.resources, R.color.colorWhite, null))

            if (item.baseIcon != null && item.baseIcon.isNotEmpty())
                decodeFromBase64(item.baseIcon).let { bitmap ->
                    val imageView = view.findViewById(R.id.image) as ImageView
                    imageView.setImageBitmap(bitmap)

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

            val image: ImageView = view.findViewById(R.id.image)

            GlideApp.with(view.context).load(item.imageUrl)
//                    .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(5, 0, RoundedCornersTransformation.CornerType.ALL)))
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

            val image: ImageView = view.findViewById(R.id.image)

            GlideApp.with(view.context).load(item.imageUrl)
//                    .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(5, 0, RoundedCornersTransformation.CornerType.ALL)))
                    .into(image)

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