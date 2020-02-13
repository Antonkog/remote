package com.kivi.remote.presentation.home.media

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.kivi.remote.R
import com.kivi.remote.common.Constants.*
import com.kivi.remote.upnp.org.droidupnp.model.upnp.didl.IDIDLItem
import com.kivi.remote.upnp.org.droidupnp.view.DIDLObjectDisplay


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

        progress.progressDrawable.colorFilter = PorterDuffColorFilter(ResourcesCompat.getColor(progress.resources, R.color.colorAccent, null), PorterDuff.Mode.MULTIPLY)
        progress.visibility = View.INVISIBLE

        allowClicks = true

        val item = entry.didlObject

        when (entry.title) {
            VIDEO -> {
                title.text = context.getString(R.string.video);   icon.setImageResource(R.drawable.placeholder_video)
            }
            IMAGE -> {
                title.text = context.getString(R.string.photo); icon.setImageResource(R.drawable.placeholder_image)
            }
            AUDIO -> {
                title.text = context.getString(R.string.audio); icon.setImageResource(R.drawable.placeholder_audio)
            }
        }

        view.setOnClickListener {
            if (item !is IDIDLItem && allowClicks) {
                if (command(item.id, item.title)) {
                    progress.visibility = View.VISIBLE
                    allowClicks = false
                }
            }
        }
        return view
    }
}