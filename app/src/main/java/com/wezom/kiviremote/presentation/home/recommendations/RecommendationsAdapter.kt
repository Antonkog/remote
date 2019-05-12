package com.wezom.kiviremote.presentation.home.recommendations

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.net.model.RecommendItem
import com.wezom.kiviremote.presentation.home.apps.AppModel
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port
import timber.log.Timber

class RecommendationsAdapter(private val listener: HorizontalCVContract.HorizontalCVListener)
        : RecyclerView.Adapter<RecommendationsAdapter.RecommendationsViewHolder<*>>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecommendationsViewHolder<*> {
            val context = parent?.context

            return when (viewType) {
                TYPE_INPUTS -> {
                    val view = LayoutInflater.from(context).inflate(com.wezom.kiviremote.R.layout.recomend_card_movie, parent, false)
                    PortViewHolder(view, listener, data)
                }
                TYPE_APPS -> {
                    val view = LayoutInflater.from(parent?.context).inflate(com.wezom.kiviremote.R.layout.recomend_card_app, parent, false)
                    AppsViewHolder(view, listener, data)
                }
                TYPE_RECOMMENDATIONS -> {
                    val view = LayoutInflater.from(parent?.context).inflate(com.wezom.kiviremote.R.layout.recomend_card_port, parent, false)
                    KiviRecsItemHolder(view, listener, data)
                }
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: RecommendationsViewHolder<*>?, position: Int) {
            val element = data[position]
            when (holder) {
                is KiviRecsItemHolder -> holder.bind(element as RecommendItem)
                is PortViewHolder -> holder.bind(element as Port)
                is AppsViewHolder -> holder.bind(element as AppModel)
                else -> throw IllegalArgumentException()
            }
        }


        private val data: MutableList<Comparable<*>>


        companion object {
             val TYPE_APPS = 0
             val TYPE_RECOMMENDATIONS = 1
             val TYPE_TV_CHANNELS = 2
             val TYPE_INPUTS = 3
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
                is AppModel -> TYPE_APPS
                is Port -> TYPE_INPUTS
                is RecommendItem -> TYPE_RECOMMENDATIONS
                else -> throw IllegalArgumentException("Invalid type of data " + position)
            }
        }

        fun sePortActivebyId(id: Int) {

            Timber.e("HorizontalCardsView.sePortActivebyId() " + id)


//        val newPorts = LinkedList<Port>()
//        for (port in ports) {
//            newPorts.add(Port(portName = port.portName, portImageId = port.portImageId, portNum = port.portNum, active = (id == port.portNum)))
//        }
//        ports.clear()
//        ports.addAll(newPorts)
//        notifyDataSetChanged()
        }

        abstract class RecommendationsViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
            abstract fun bind(item: T)
        }


        class PortViewHolder(val view: View, val listener: HorizontalCVContract.HorizontalCVListener,
                             val data: List<Comparable<*>>) : RecommendationsViewHolder<Port>(view), View.OnClickListener {

//        val trailerImageView = view.thumbnail_trailer_image_view

            override fun bind(port: Port) {
//            Picasso.with(view.context)
//                    .load(thumbnailUrl)
//                    .into(trailerImageView)
//            titleTextView.text = port.name
                view!!.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                val position = adapterPosition

                if (position < 0) {
                    return
                }

                val port = data[position] as Port
                listener.onPortChosen(port, adapterPosition)
            }
        }

        class AppsViewHolder(val view: View, val listener: HorizontalCVContract.HorizontalCVListener,
                             val data: List<Comparable<*>>) : RecommendationsViewHolder<AppModel>(view), View.OnClickListener {

            override fun bind(app: AppModel) {

            }

            override fun onClick(view: View) {
                val position = adapterPosition

                if (position < 0) {
                    return
                }

                val app = data[position] as AppModel
                listener.appChosenNeedOpen(app, adapterPosition)
            }
        }

        class KiviRecsItemHolder(val view: View, val listener: HorizontalCVContract.HorizontalCVListener,
                                 val data: List<Comparable<*>>) : RecommendationsViewHolder<RecommendItem>(view), View.OnClickListener {

            override fun bind(recommendItem: RecommendItem) {

            }

            override fun onClick(view: View) {

                val position = adapterPosition

                if (position < 0) {
                    return
                }

                val recommendItem = data[position] as RecommendItem
                listener.onRecommendationChosen(recommendItem, adapterPosition)
            }
        }

    }