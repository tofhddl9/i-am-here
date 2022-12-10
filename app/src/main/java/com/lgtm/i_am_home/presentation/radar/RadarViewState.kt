package com.lgtm.i_am_home.presentation.radar

import com.lgtm.i_am_home.domain.Device

data class RadarViewState(
    val rememberDeviceList: List<Device> = mutableListOf(),
    val capturedDeviceList: List<Device> = mutableListOf(),
    val progress: RadarProgress = RadarProgress.INIT,
)

enum class RadarProgress {
    INIT, SCANNING_NOT_FOUND, SCANNING_FOUND_AT_LEAST_ONE,
}