package com.wezom.kiviremote.presentation.home.recentdevices.item

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.wezom.kiviremote.R
import com.wezom.kiviremote.presentation.home.recentdevice.item_info.TvInfoUnit


class TvInfoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val tvUnitName = view.findViewById<TextView>(R.id.device_unit_name)
    private val tvUnitValue = view.findViewById<TextView>(R.id.device_unit_value)

    fun setUnitInfo(info: TvInfoUnit) {
        tvUnitName.text = info.unitName
        tvUnitValue.text = info.unitValue
    }

}