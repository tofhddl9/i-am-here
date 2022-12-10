package com.lgtm.i_am_home.usecase

import android.util.Log
import com.lgtm.i_am_home.data.BluetoothRepository
import com.lgtm.i_am_home.domain.Device
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ConnectDeviceUsecase @Inject constructor(
    private val bluetoothRepository: BluetoothRepository
) {

    operator fun invoke(device: Device): Flow<List<Device>> {
        bluetoothRepository.addPairedDeviceFlow(device)
        return bluetoothRepository.pairedDeviceList
    }

    operator fun invoke(devices: List<Device>): Flow<List<Device>> {
        Log.d("Doran", "try to connect ${devices.getOrNull(0)}")
        bluetoothRepository.connectDeviceFlow(devices)
        return bluetoothRepository.filteredPairedDeviceList
    }

}
