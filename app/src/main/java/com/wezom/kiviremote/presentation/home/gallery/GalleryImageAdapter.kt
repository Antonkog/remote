package com.wezom.kiviremote.presentation.home.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.ImageInfo
import com.wezom.kiviremote.common.VideoInfo
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.didl.IDIDLItem
import com.wezom.kiviremote.upnp.org.droidupnp.view.DIDLObjectDisplay


class GalleryImageAdapter(private val context: Context,
                          private val images: Set<ImageInfo>,
                          private val items: ArrayList<DIDLObjectDisplay>?,
                          private val layoutId: Int,
                          private inline val command: (IDIDLItem, String?, Int, Set<ImageInfo>?, Set<VideoInfo>?, GalleryFragment.MediaType) -> Unit)
    : RecyclerView.Adapter<GalleryImageAdapter.GalleryImageVideoViewHolder>() {
    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryImageVideoViewHolder {
        return GalleryImageVideoViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
    }

    override fun getItemCount(): Int = items?.size ?: 0

    override fun onBindViewHolder(holder: GalleryImageVideoViewHolder, position: Int) {
        val item = items?.get(position)
        val didlItem = item?.didlObject as IDIDLItem
        var uri = didlItem.uri
        holder?.run {
            images.asSequence().forEach {
                val remoteFileTitle = item.title
                val thumbTitle = it.title
                if (remoteFileTitle == thumbTitle) {
                    Glide.with(context)
                            .load(it.data)
                            .apply(RequestOptions().error(R.drawable.placeholder_image))
                            .into(icon)
                    uri = it.data
                    return@forEach
                }
            }

            icon.setOnClickListener {
                command(didlItem,
                        uri,
                        position, images, null,
                        GalleryFragment.MediaType.IMAGE)
            }
        }
    }

    inner class GalleryImageVideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.gallery_image_icon)
    }
}