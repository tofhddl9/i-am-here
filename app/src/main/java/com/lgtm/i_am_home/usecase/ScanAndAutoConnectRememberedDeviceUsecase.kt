package com.lgtm.i_am_home.usecase

import com.lgtm.i_am_home.data.BluetoothRepository
import com.lgtm.i_am_home.domain.Device
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class ScanAndAutoConnectRememberedDeviceUsecase @Inject constructor(
    private val bluetoothRepository: BluetoothRepository
) {

    operator fun invoke() {
        bluetoothRepository.registerBluetoothReceiver() // TODO : integrate
    }

}