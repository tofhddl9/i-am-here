package com.lgtm.i_am_home.usecase

import android.bluetooth.BluetoothDevice
import com.lgtm.i_am_home.BluetoothRepository
import javax.inject.Inject

class ConnectDeviceUsecase @Inject constructor(
    private val bluetoothRepository: BluetoothRepository
) {

    operator fun invoke(device: BluetoothDevice) {
        bluetoothRepository.pairedDeviceFlow(device)
    }

}