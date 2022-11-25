package com.lgtm.i_am_home.usecase

import android.bluetooth.le.ScanResult
import com.lgtm.i_am_home.BluetoothRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScanDeviceUsecase @Inject constructor(
    private val bluetoothRepository: BluetoothRepository,
) {
    operator fun invoke(): Flow<Device> =
        bluetoothRepository.scannedDeviceFlow().map {
            it.mapToDevice()
        }
}

data class Device(
    val id: String
)

fun ScanResult.mapToDevice() : Device {
    return Device(id = "1")
}