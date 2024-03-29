package com.wezom.kiviremote.presentation.home.apps

import android.support.v7.util.DiffUtil


class AppsDiffCallback(private val oldApps: List<AppModel>, private val newApps: List<AppModel>) : DiffUtil.Callback() {


    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            (oldApps[oldItemPosition].appPackage) == newApps[newItemPosition].appPackage

    override fun getOldListSize(): Int = oldApps.size

    override fun getNewListSize(): Int = newApps.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldApps[oldItemPosition] == newApps[newItemPosition]
}