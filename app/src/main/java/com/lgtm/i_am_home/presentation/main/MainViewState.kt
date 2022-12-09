package com.lgtm.i_am_home.presentation.main

import com.lgtm.i_am_home.domain.Device

data class MainViewState(
    val scannedDeviceList: List<Device> = mutableListOf(),
    val pairedDeviceList: List<Device> = mutableListOf(),
    val isBluetoothOn: Boolean = false,
    val isScanning: Boolean = false,
)
