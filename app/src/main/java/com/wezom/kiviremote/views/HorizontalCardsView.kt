package com.wezom.kiviremote.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wezom.kiviremote.common.KiviCache
import com.wezom.kiviremote.net.model.RecommendItem
import timber.log.Timber
import java.util.*


class HorizontalCardsView : LinearLayout {

    lateinit var type: TextView
    lateinit var recicler: RecyclerView
    lateinit var progressBar: ProgressBar
    private lateinit var listener: OnClickListener
    private lateinit var adapter: RecommendationsAdapter

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    enum class ContentType : ViewType {
        TYPE_APPS {
            override fun getViewType(): Int {
                return 0
            }
        },
        TYPE_RECOMMENDATIONS {
            override fun getViewType(): Int {
                return 1
            }
        },
        TYPE_TV_CHANNELS {
            override fun getViewType(): Int {
                return 2
            }
        },
        TYPE_INPUTS {
            override fun getViewType(): Int {
                return 3
            }
        },
        TYPE_NONE {
            override fun getViewType(): Int {
                return -1
            }
        };

        companion object {
            fun getCardTypeByName(name: String): Int = valueOf(name.toUpperCase()).getViewType()
        }
    }

    interface ViewType {
        fun getViewType(): Int
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        inflate(context, com.wezom.kiviremote.R.layout.view_horizontal_cards, this)
        type = findViewById(com.wezom.kiviremote.R.id.type)

        val attributes = context.obtainStyledAttributes(
                attrs, com.wezom.kiviremote.R.styleable.HorizontalCardsView, defStyle, 0)

        try {
            type.text = attributes.getString(com.wezom.kiviremote.R.styleable.HorizontalCardsView_type)
        } finally {
            attributes.recycle()
        }

        recicler = findViewById(com.wezom.kiviremote.R.id.recycler)
        progressBar = findViewById(com.wezom.kiviremote.R.id.progress_circular)

    }


    public fun setAdapter(context: Context, cache: KiviCache, type: ContentType) {
        recicler.run {
            adapter = RecommendationsAdapter(context, cache)
            layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
            setItemViewCacheSize(20)
            setHasFixedSize(true)
        }
    }

    fun setListeners(listener: OnClickListener) {
        this.listener = listener
    }

    fun setNewItems(RecommendItems: List<RecommendItem>, type: HorizontalCardsView.ContentType) {
        Timber.i("RecommendItems arrived " + RecommendItems.size)
        if (this::adapter.isInitialized && adapter != null)
            this.adapter.setNewItems(RecommendItems, type)
        else (Timber.e("HorizontalCardsView.setNewItems() should call setAdapter() first" + type.name))
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

    interface OnClickListener {
        fun onClick(serverId: Int, type: HorizontalCardsView.ContentType)
    }


    class RecommendationsAdapter(private val context: Context,
                                 private val cache: KiviCache)
        : RecyclerView.Adapter<RecommendationsViewHolder>() {


        private var items: MutableList<RecommendItem>? = null

        private lateinit var initialList: List<RecommendItem>


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationsViewHolder {
            when (viewType) {

                ContentType.TYPE_INPUTS.ordinal -> {
                    return RecommendationsViewHolder(
                            LayoutInflater
                                    .from(parent.context)
                                    .inflate(com.wezom.kiviremote.R.layout.recomend_card_port, parent, false)
                    )
                }
                ContentType.TYPE_APPS.ordinal -> {
                    return RecommendationsViewHolder(
                            LayoutInflater
                                    .from(parent.context)
                                    .inflate(com.wezom.kiviremote.R.layout.recomend_card_app, parent, false)
                    )
                }
                else -> {
                    Timber.e("wrong content type ")
                }
            }

            return RecommendationsViewHolder(

                    LayoutInflater
                            .from(parent.context)
                            .inflate(com.wezom.kiviremote.R.layout.recomend_card_app, parent, false)

            )

        }

        override fun getItemCount(): Int = if (items != null) items!!.size else 0

        override fun onBindViewHolder(holder: RecommendationsViewHolder, position: Int) {
            Timber.d("items: $items")
            val recommendItem = items!![position]
            val resources = context.resources

            if (recommendItem.packageName != null
                    && !recommendItem.packageName.isBlank()) {  //apps

                val m = cache[recommendItem.packageName]
                if (m != null) Timber.d("drawable exist " + recommendItem.packageName)
            } else {

                holder.apply {
                    Glide.with(imageV).load(recommendItem.url).apply(RequestOptions().error(com.wezom.kiviremote.R.drawable.placeholder_video)).into(imageV)
                    textV.text = recommendItem.title
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            val recommendItem = items!![position]
            return recommendItem.type
        }

        fun setNewItems(recomendItems: List<RecommendItem>, type: HorizontalCardsView.ContentType) {
            when (type) {
                ContentType.TYPE_NONE -> {
                    Timber.e("wrong content type")
                }
                ContentType.TYPE_INPUTS -> {


                }
                ContentType.TYPE_APPS -> {

                }
            }


            Timber.d("New list of items has arrived, size: ${recomendItems.size}")
            this.items = recomendItems.toMutableList()
            initialList = ArrayList(items)

            notifyItemRangeRemoved(0, getLastPosition())
            if (items != null) {
                items?.addAll(recomendItems.toMutableList())
                notifyItemRangeInserted(0, items?.size ?: 0)
            }
        }

        private fun getLastPosition() = if (items?.lastIndex == -1) 0 else items?.lastIndex ?: 0

        fun setActivebyId(serverId: Int) {

        }


    }


    class RecommendationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: ConstraintLayout = itemView.findViewById(com.wezom.kiviremote.R.id.recomendation_container)
        val imageV: ImageView = itemView.findViewById(com.wezom.kiviremote.R.id.image)
        val textV: TextView = itemView.findViewById(com.wezom.kiviremote.R.id.text)
    }





    fun setRecommendData ()  = {
        var list = LinkedList<RecommendItem>()
        list.addLast(
                RecommendItem(
                ContentType.TYPE_RECOMMENDATIONS.ordinal,
                title = "The Godfather",
                serverId = 1,
                url = "https://m.media-amazon.com/images/M/MV5BM2MyNjYxNmUtYTAwNi00MTYxLWJmNWYtYzZlODY3ZTk3OTFlXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_SX300.jpg"
                )
        )

        list.addLast(
                RecommendItem(
                        ContentType.TYPE_RECOMMENDATIONS.ordinal,
                        title = "Disco Godfather",
                        serverId = 2,
                        url ="https://m.media-amazon.com/images/M/MV5BMTU5MzAyMTY1Ml5BMl5BanBnXkFtZTgwNzA2MjI4MzE@._V1._CR46,89.5,1255,1862_SX89_AL_.jpg_V1_SX300.jpg"
                )
        )



        list.addLast(
                RecommendItem(
                ContentType.TYPE_RECOMMENDATIONS.ordinal,
                title =  "The Godfather Family: A Look Inside",
                serverId = 3,
                url =  "https://m.media-amazon.com/images/M/MV5BMTUzOTc0NDAyNF5BMl5BanBnXkFtZTcwNjAwMDEzMQ@@._V1_SX300.jpg"
                )
        )

        list.addLast(
                RecommendItem(
                ContentType.TYPE_RECOMMENDATIONS.ordinal,
                title =  "The Godfather Trilogy: 1901-1980",
                serverId = 4,
                url =  "https://m.media-amazon.com/images/M/MV5BMTY1NzYxNDk0NV5BMl5BanBnXkFtZTYwMjk5MTM5._V1_SX300.jpg"
                )
        )

        setNewItems(list, ContentType.TYPE_RECOMMENDATIONS)
    }
}
