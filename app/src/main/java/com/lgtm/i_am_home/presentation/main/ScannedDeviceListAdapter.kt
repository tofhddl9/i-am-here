package com.lgtm.i_am_home.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lgtm.i_am_home.databinding.DeviceItemBinding
import com.lgtm.i_am_home.domain.Device

class ScannedDeviceListAdapter(
    private val itemClickListener: ((Device) -> Unit)?,
) : ListAdapter<Device, ScannedDeviceListAdapter.VH>(ScannedDeviceDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = DeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val device = currentList[position]
        holder.setData(device)
        holder.itemView.setOnClickListener {
            itemClickListener?.invoke(device)
        }
    }

    class VH(private val binding: DeviceItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setData(device: Device) {
            binding.deviceName.text = device.name
            binding.deviceAddress.text = device.address
        }
    }

}

class ScannedDeviceDiffItemCallback : DiffUtil.ItemCallback<Device>() {
    override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem == newItem
    }
}