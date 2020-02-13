package com.kivi.remote.common.recycler

import android.view.View

@FunctionalInterface
interface RecyclerViewClickListener {
    fun recyclerViewListClicked(v: View, position: Int)
}