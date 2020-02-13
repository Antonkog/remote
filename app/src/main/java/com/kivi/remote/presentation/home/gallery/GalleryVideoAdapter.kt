package com.kivi.remote.presentation.home.gallery

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.kivi.remote.R
import com.kivi.remote.common.ImageInfo
import com.kivi.remote.common.VideoInfo
import com.kivi.remote.common.extensions.formatDuration
import com.kivi.remote.upnp.org.droidupnp.model.upnp.didl.IDIDLItem
import com.kivi.remote.upnp.org.droidupnp.view.DIDLObjectDisplay


class GalleryVideoAdapter(
        private val videos: Set<VideoInfo>,
        private val items: ArrayList<DIDLObjectDisplay>?,
        private val layoutId: Int,
        private inline val command: (IDIDLItem, String?, Int, Set<ImageInfo>?, Set<VideoInfo>?, GalleryFragment.MediaType) -> Unit)
    : RecyclerView.Adapter<GalleryVideoAdapter.GalleryImageViewHolder>() {

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GalleryImageViewHolder(
            LayoutInflater.from(parent?.context).inflate(layoutId, parent, false)
    )

    override fun getItemCount(): Int = items?.size ?: 0

    override fun onBindViewHolder(holder: GalleryImageViewHolder, position: Int) {
        val item = items?.get(position)
        val didlItem = item?.didlObject as IDIDLItem
        var uri = didlItem.uri
        holder?.run {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
                icon.clipToOutline = true
            title.text = item.title
            duration.text = item.count?.formatDuration()

            videos.asSequence().forEach {
                val remoteFileTitle = item.title
                val thumbTitle = it.title
                if (remoteFileTitle == thumbTitle) {
                    Glide.with(itemView)
                            .load(it.data)
                            .apply(RequestOptions().error(R.drawable.placeholder_video))
                            .into(icon)
                    uri = it.data
                }
            }
            icon.setOnClickListener { _ ->
                command(didlItem, uri, position, null, videos, GalleryFragment.MediaType.VIDEO)
            }
        }
    }

    inner class GalleryImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.gallery_video_thumb)
        val title: TextView = view.findViewById(R.id.gallery_video_title)
        val duration: TextView = view.findViewById(R.id.gallery_video_duration)
    }
}