package com.lgtm.i_am_home.usecase

import com.lgtm.i_am_home.data.BluetoothRepository
import com.lgtm.i_am_home.domain.Device
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

class ScanDeviceUsecase @Inject constructor(
    private val bluetoothRepository: BluetoothRepository,
) {

    operator fun invoke(
        doesFilterOnlyRememberDevice: Boolean = false
    ): Flow<List<Device>> {
        bluetoothRepository.registerBluetoothReceiver()
        if (!doesFilterOnlyRememberDevice) {
            return bluetoothRepository.scannedDeviceList
        }

        return bluetoothRepository.filteredScannedDeviceList
    }

}