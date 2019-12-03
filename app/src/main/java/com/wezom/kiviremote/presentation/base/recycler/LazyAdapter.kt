package com.wezom.kiviremote.presentation.base.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class LazyAdapter <DataType, LayoutClassBinding : ViewDataBinding> (val itemClickListener: OnItemClickListener<DataType>? = null) : RecyclerView.Adapter<LazyAdapter.LazyViewHolder<DataType>>() {

    private val data = mutableListOf<DataType>()

    fun swapData(newData: List<DataType>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: LazyViewHolder<DataType>, position: Int) {
        holder.bindData(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LazyViewHolder<DataType> {
        val binding: LayoutClassBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), getLayoutId(), parent, false)
        return object: LazyViewHolder<DataType>(binding) {
            override fun bindData(data: DataType) {
                bindData(data, binding)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    abstract fun bindData(data: DataType, binding: LayoutClassBinding)

    abstract fun getLayoutId(): Int

    abstract class LazyViewHolder<DataType>(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bindData(data: DataType)
    }

    interface OnItemClickListener <DataType> {
        fun onLazyItemClick(data: DataType)
    }

}