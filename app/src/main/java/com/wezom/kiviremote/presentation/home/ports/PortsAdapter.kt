package com.wezom.kiviremote.presentation.home.ports

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wezom.kiviremote.databinding.PortItemBinding
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port


class PortsAdapter(val listener: CheckListener) : RecyclerView.Adapter<PortsAdapter.PortsViewHolder>() {

    companion object {
        var checkListener: CheckListener? = null
    }

    private var ports: MutableList<Port> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PortItemBinding.inflate(layoutInflater, parent, false)
        return PortsViewHolder(binding)
    }

    override fun getItemCount(): Int = ports.size

    override fun onBindViewHolder(holder: PortsViewHolder, position: Int) {
        val port = ports[position]
        val title = port.portName
        checkListener = listener

        holder.run {
            binding.textPort.text = title
            binding.imagePort.setImageResource(port.portImageId)

            if (port.active) {
                binding.checkPort.isChecked = true
            } else {
                binding.checkPort.isChecked = false
            }

            binding.portLayoutContent.setOnClickListener { view ->
                if (!binding.checkPort.isChecked) {
                    checkListener?.onPortChecked(port.portNum)
                }
            }
        }
    }


    open interface CheckListener {
        fun onPortChecked(portId: Int)
    }

    fun setData(newports: List<Port>) {
        ports.clear()
        ports.addAll(newports)
        notifyDataSetChanged()
    }


    inner class PortsViewHolder(val binding: PortItemBinding) : RecyclerView.ViewHolder(binding.root)
}

