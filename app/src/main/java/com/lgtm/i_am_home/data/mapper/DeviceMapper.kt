package com.lgtm.i_am_home.data.mapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import com.lgtm.i_am_home.domain.Device

object DeviceMapper {

    @SuppressLint("MissingPermission")
    fun BluetoothDevice.mapToDevice(): Device =
        Device(
            name = name ?: "Unknown Device",
            address = address,
            isConnected = false
        )

    @SuppressLint("MissingPermission")
    fun BluetoothGatt.mapToDevice(): Device =
        Device(
            name = device.name,
            address = device.address,
            isConnected = true
        )

}