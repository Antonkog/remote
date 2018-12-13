package com.wezom.kiviremote.presentation.home.ports

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wezom.kiviremote.common.Constants.IMAGE
import com.wezom.kiviremote.common.imageDirectoriesPreviews
import com.wezom.kiviremote.databinding.PortItemBinding
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port


class PortsAdapter(private val currentDirType: String,  private val command: (Port) -> Unit)
    : RecyclerView.Adapter<PortsAdapter.PortsViewHolder>() {

    var ports: List<Port> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PortItemBinding.inflate(layoutInflater, parent, false)
        return PortsViewHolder(binding)
    }

    override fun getItemCount(): Int = ports.size

    override fun onBindViewHolder(holder: PortsViewHolder, position: Int) {
        val port = ports[position]
        val title = port.portName
        holder.run {
            binding.textPort.text = title
            binding.imagePort.setImageResource(port.portImageId)
//            binding.textPort.setOnClickListener { browseToDirectory(port, title) }

            val preview: String? = when (currentDirType) {
                IMAGE -> {
                    imageDirectoriesPreviews[port.portName]
                }

                else -> null
            }

        }
    }


    inner class PortsViewHolder(val binding: PortItemBinding) : RecyclerView.ViewHolder(binding.root)
}

