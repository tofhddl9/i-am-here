package com.lgtm.i_am_home.usecase

import android.bluetooth.le.ScanResult
import com.lgtm.i_am_home.BluetoothGuide

class ScanDeviceUsecase(
    val bluetoothGuide: BluetoothGuide,
) : BluetoothGuide.OnCheckModelListener {

    operator fun invoke() {
        bluetoothGuide.scanDevices()
    }

    override fun isChecked(bytes: ByteArray?): Boolean {

    }

    override fun scannedDevice(result: ScanResult?) {
        TODO("Not yet implemented")
    }
}