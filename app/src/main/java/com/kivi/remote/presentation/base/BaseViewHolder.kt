package com.kivi.remote.presentation.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView


abstract class BaseViewHolder<out T : ViewDataBinding>(val binding: T)
    : RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(item: Any)
}