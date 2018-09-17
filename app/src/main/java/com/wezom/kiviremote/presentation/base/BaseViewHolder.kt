package com.wezom.kiviremote.presentation.base

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView


abstract class BaseViewHolder<out T : ViewDataBinding>(val binding: T)
    : RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(item: Any)
}