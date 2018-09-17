package com.wezom.kiviremote.presentation.home.directories

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants.IMAGE
import com.wezom.kiviremote.common.Constants.VIDEO
import com.wezom.kiviremote.common.imageDirectoriesPreviews
import com.wezom.kiviremote.common.videoDirectoriesPreviews
import com.wezom.kiviremote.databinding.DirectoryItemBinding
import com.wezom.kiviremote.upnp.org.droidupnp.view.DIDLObjectDisplay


class DirectoriesAdapter(private val currentDirType: String,
                         private val command: (DIDLObjectDisplay, String) -> Unit)
    : RecyclerView.Adapter<DirectoriesAdapter.DirectoriesViewHolder>() {
    var directories: List<DIDLObjectDisplay> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoriesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DirectoryItemBinding.inflate(layoutInflater, parent, false)
        return DirectoriesViewHolder(binding)
    }

    override fun getItemCount(): Int = directories.size

    override fun onBindViewHolder(holder: DirectoriesViewHolder, position: Int) {
        val directory = directories[position]
        val title = directory.title.substringAfterLast("/")
        holder.run {
            binding.directoryTitle.text = title
            binding.directoryTitle.setOnClickListener { browseToDirectory(directory, title) }

            val preview: String? = when (currentDirType) {
                IMAGE -> {
                    imageDirectoriesPreviews[directory.title]
                }

                VIDEO -> {
                    videoDirectoriesPreviews[directory.title]
                }

                else -> null
            }

            if (preview != null)
                Glide.with(holder.itemView.context).load(preview).into(binding.directoryPreview)
            binding.directoryPreviewContainer.setOnClickListener { browseToDirectory(directory, title) }


            binding.directoryNumberOfItems.text = holder.itemView.context.resources.getString(R.string.number_of_images, directory.count)

            when (currentDirType) {
                IMAGE -> {
                    binding.directoryNumberOfItems.text = holder.itemView.context.resources.getString(R.string.number_of_images, directory.count)
                }
                VIDEO -> {
                    binding.directoryNumberOfItems.text = holder.itemView.context.resources.getString(R.string.number_of_videos, directory.count)

                }
            }
            binding.directoryNumberOfItems.setOnClickListener { browseToDirectory(directory, title) }
        }
    }

    private fun browseToDirectory(directory: DIDLObjectDisplay, title: String) = command(directory, title)

    inner class DirectoriesViewHolder(val binding: DirectoryItemBinding) : RecyclerView.ViewHolder(binding.root)
}

