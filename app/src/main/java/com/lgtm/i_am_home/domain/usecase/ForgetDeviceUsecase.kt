package com.lgtm.i_am_home.domain.usecase

import com.lgtm.i_am_home.data.BluetoothRepository
import com.lgtm.i_am_home.domain.Device
import javax.inject.Inject

class ForgetDeviceUsecase @Inject constructor(
    private val bluetoothRepository: BluetoothRepository
) {

    operator fun invoke(device: Device) {
        bluetoothRepository.removeDevice(device)
    }

}
