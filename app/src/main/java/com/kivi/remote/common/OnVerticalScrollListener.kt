package com.kivi.remote.common

import androidx.recyclerview.widget.RecyclerView


abstract class OnVerticalScrollListener : RecyclerView.OnScrollListener() {

    /**
     * If dy < 0 then user scrolled up, else scrolled down, call appropriate methods
     */
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (!recyclerView.canScrollVertically(-1)) onScrolledToTop()

        if (!recyclerView.canScrollVertically(1)) onScrolledToBottom()

        if (dy < 0) onScrolledUp()

        if (dy > 0) onScrolledDown()
    }

    abstract fun onScrolledUp()

    abstract fun onScrolledDown()

    abstract fun onScrolledToTop()

    abstract fun onScrolledToBottom()
}