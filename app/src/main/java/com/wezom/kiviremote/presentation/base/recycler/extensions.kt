package com.wezom.kiviremote.presentation.base.recycler

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


fun <DataType, LayoutClassBinding : ViewDataBinding> RecyclerView.initWithLinLay(orientation: Int, adapter: LazyAdapter<DataType, LayoutClassBinding>, data: List<DataType>) {
    this.apply {
        this.adapter = adapter
        this.layoutManager = LinearLayoutManager(this.context, orientation, false)
        setHasFixedSize(true)
    }

    if (data.isNotEmpty())
        adapter.swapData(data)
}



fun <DataType, LayoutClassBinding : ViewDataBinding> RecyclerView.initWithManager(layoutManager: LinearLayoutManager, adapter: LazyAdapter<DataType, LayoutClassBinding>, data: List<DataType>) {
    this.apply {
        this.adapter = adapter
        this.layoutManager = layoutManager
        setHasFixedSize(true)
    }
    adapter.swapData(data)
}


fun <DataType, LayoutClassBinding : ViewDataBinding> RecyclerView.initWithGridLay(spanCount: Int, adapter: LazyAdapter<DataType, LayoutClassBinding>, data: List<DataType>) {
    this.apply {
        this.adapter = adapter
        this.layoutManager = GridLayoutManager(this.context, spanCount)
        setHasFixedSize(true)
    }

    if (data.isNotEmpty())
        adapter.swapData(data)
}

fun RecyclerView.addItemDivider() {
    val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    val dividerItemDecoration = DividerItemDecoration(this.context, layoutManager.orientation)
    addItemDecoration(dividerItemDecoration)
}