package com.lgtm.i_am_home.presentation.radar

import com.lgtm.i_am_home.domain.Device

data class RadarViewState(
    val rememberDeviceList: List<Device> = mutableListOf(),
    val isScanning: Boolean = false,
)
