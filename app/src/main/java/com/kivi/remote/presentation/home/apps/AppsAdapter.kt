package com.kivi.remote.presentation.home.apps

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kivi.remote.BR
import com.kivi.remote.common.KiviCache
import com.kivi.remote.databinding.AppItemBinding
import com.kivi.remote.presentation.base.BaseViewHolder


class AppsAdapter(private val cache: KiviCache,
                  private inline val command: (String) -> Unit)
    : RecyclerView.Adapter<AppsAdapter.AppsViewHolder>() {

    private var apps: MutableList<AppModel> = mutableListOf()

    data class AppBindingModel(val app: AppModel, val icon: Bitmap)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AppItemBinding.inflate(layoutInflater, parent, false)
        binding.setVariable(BR.adapter, this)
        return AppsViewHolder(binding)
    }

    override fun getItemCount(): Int = apps.size

    override fun onBindViewHolder(holder: AppsViewHolder, position: Int) {
        val app = apps[position]
        val bitmap = cache.get(app.appName)
        if (bitmap != null) {
            val appModel = AppBindingModel(app, bitmap)
            holder.bind(appModel)
        }
    }

    fun launchApp(app: AppModel) = command(app.appPackage)

    fun setNewApps(apps: List<AppModel>?) {
        if (apps == null || apps.isEmpty()) {
            this.apps.clear()
            notifyDataSetChanged()
            return
        }

        val diffResult = DiffUtil.calculateDiff(AppsDiffCallback(this.apps, apps))
        diffResult.dispatchUpdatesTo(this)
        this.apps = apps.toMutableList()
    }

    inner class AppsViewHolder(binding: AppItemBinding) : BaseViewHolder<AppItemBinding>(binding) {
        override fun bind(item: Any) {
            binding.setVariable(BR.model, item)
            binding.executePendingBindings()
        }
    }
}