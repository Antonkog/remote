package com.wezom.kiviremote.presentation.home.media

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.common.Constants.*
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.didl.IDIDLItem
import com.wezom.kiviremote.upnp.org.droidupnp.view.DIDLObjectDisplay


class MainContentAdapter(
    context: Context?,
    private val inflater: LayoutInflater,
    private inline val command: (String, String) -> Boolean
) : ArrayAdapter<DIDLObjectDisplay>(context, 0) {

    private val layout = R.layout.media_item
    private var allowClicks = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: inflater.inflate(layout, null)
        val entry = getItem(position)

        val icon: ImageView = view.findViewById(R.id.media_icon)
        val title: TextView = view.findViewById(R.id.media_title)
        val progress: ProgressBar = view.findViewById(R.id.media_item_progress)
        val separator: View = view.findViewById(R.id.separator)

        progress.visibility = View.INVISIBLE
        separator.visibility = View.VISIBLE

        allowClicks = true

        val item = entry.didlObject
        title.text = entry.title
        when (entry.title) {
            VIDEO -> icon.setImageResource(R.drawable.ic_videos)
            IMAGE -> icon.setImageResource(R.drawable.ic_images)
            AUDIO -> icon.setImageResource(R.drawable.ic_audio)
        }

        view.setOnClickListener {
            if (item !is IDIDLItem && allowClicks) {
                if (command(item.id, item.title)) {
                    progress.visibility = View.VISIBLE
                    separator.visibility = View.INVISIBLE
                    allowClicks = false
                }
            }
        }
        return view
    }
}