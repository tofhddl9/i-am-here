package com.lgtm.i_am_home.usecase

import com.lgtm.i_am_home.data.BluetoothRepository
import com.lgtm.i_am_home.domain.Device
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ConnectDeviceUsecase @Inject constructor(
    private val bluetoothRepository: BluetoothRepository
) {

//    operator fun invoke(device: Device): Flow<List<Device>> =
//        bluetoothRepository.addPairedDeviceFlow(device)

    operator fun invoke(device: Device): Flow<List<Device>> {
        bluetoothRepository.addPairedDeviceFlow(device)
        return bluetoothRepository.pairedDeviceList
    }

}
