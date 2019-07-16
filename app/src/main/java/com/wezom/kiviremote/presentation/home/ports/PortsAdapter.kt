package com.wezom.kiviremote.presentation.home.ports

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wezom.kiviremote.databinding.PortItemBinding
import com.wezom.kiviremote.net.model.Input
import java.util.*


class PortsAdapter(val listener: CheckListener) : RecyclerView.Adapter<PortsAdapter.PortsViewHolder>() {

    companion object {
        var checkListener: CheckListener? = null
    }

    private var inputs: MutableList<Input> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PortItemBinding.inflate(layoutInflater, parent, false)
        return PortsViewHolder(binding)
    }

    override fun getItemCount(): Int = inputs.size

    override fun onBindViewHolder(holder: PortsViewHolder, position: Int) {
        val input = inputs[position]
        val title = input.name
        checkListener = listener

        holder.run {
            binding.textPort.text = title
            binding.imagePort.setImageResource(InputSourceHelper.INPUT_PORT.getPicById(input.intID))

            if (input.isActive) {
                binding.checkPort.isChecked = true
            } else {
                binding.checkPort.isChecked = false
            }

            binding.portLayoutContent.setOnClickListener { _ ->
                if (!binding.checkPort.isChecked) {
                    checkListener?.onPortChecked(input.intID)
                }
            }
        }
    }


    interface CheckListener {
        fun onPortChecked(portId: Int)
    }

    fun setData(newports: List<Input>) {
        inputs.clear()
        inputs.addAll(newports)
        notifyDataSetChanged()
    }

    fun setInputActiveById(id: Int) {
        val newInputs = LinkedList<Input>()
        for (input in inputs) {
            newInputs.add(input.addActive(id == input.intID))
        }
        inputs.clear()
        inputs.addAll(newInputs)
        notifyDataSetChanged()
    }

    inner class PortsViewHolder(val binding: PortItemBinding) : RecyclerView.ViewHolder(binding.root)
}

