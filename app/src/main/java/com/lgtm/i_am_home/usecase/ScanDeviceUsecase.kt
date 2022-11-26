package com.lgtm.i_am_home.usecase

import com.lgtm.i_am_home.data.BluetoothRepository
import com.lgtm.i_am_home.domain.Device
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ScanDeviceUsecase @Inject constructor(
    private val bluetoothRepository: BluetoothRepository,
) {
    operator fun invoke(): Flow<List<Device>> =
        bluetoothRepository.scannedDeviceFlow()
}