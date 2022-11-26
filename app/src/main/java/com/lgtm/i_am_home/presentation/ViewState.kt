package com.lgtm.i_am_home.presentation

import com.lgtm.i_am_home.domain.Device

data class ViewState(
    val scannedDeviceList: List<Device> = mutableListOf(),
    val pairedDeviceList: List<Device> = mutableListOf(),
    val isBluetoothOn: Boolean = false,
    val isScanning: Boolean = false,
)
