package com.wezom.kiviremote.presentation.home.recentdevices.item

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wezom.kiviremote.R
import com.wezom.kiviremote.presentation.home.recentdevice.item_info.TvInfoUnit

class TvInfoAdapter : RecyclerView.Adapter<TvInfoViewHolder>() {

    private val tvUnits = mutableListOf<TvInfoUnit>()

    fun addData(data: TvInfoUnit) {
        tvUnits.add(data)
        notifyItemInserted(tvUnits.size - 1)
    }

    fun swapData(newData: List<TvInfoUnit>) {
        tvUnits.clear()
        tvUnits.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TvInfoViewHolder {
        return TvInfoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.tv_info_item, parent, false))
    }

    override fun onBindViewHolder(holder: TvInfoViewHolder, position: Int) {
        holder.setUnitInfo(tvUnits[position])
    }

    override fun getItemCount(): Int = tvUnits.size

}