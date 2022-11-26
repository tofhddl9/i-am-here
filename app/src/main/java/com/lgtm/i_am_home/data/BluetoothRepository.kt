package com.lgtm.i_am_home.data

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.lgtm.i_am_home.data.mapper.DeviceMapper.mapToDevice
import com.lgtm.i_am_home.domain.Device
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Exception
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.*

class BluetoothRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adapter: BluetoothAdapter,
) {

    private val scanResultSet: MutableSet<ScanResult> = mutableSetOf()
    private val scannedDeviceList: List<Device>
        get() {
            return scanResultSet.map {
                it.device.mapToDevice()
            }.toList()
        }
//    val scannedDeviceList: Flow<List<Device>> = flow {
//        scanResultList.map {
//            it.device.mapToDevice()
//        }.asFlow()
//    }

    private val gattList: MutableSet<BluetoothGatt> = mutableSetOf()
    val pairedDeviceList: Flow<List<Device>> = flow {
        gattList.asFlow()
    }

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun scannedDeviceFlow(): Flow<List<Device>> = callbackFlow {
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (scanResultSet.any {
                    it.device.name == result.device.name
                }) {
                    return
                }

                scanResultSet.add(result)
                trySend(scannedDeviceList).onFailure {  }
//                trySend(result.device.mapToDevice())
//                    .onFailure {  }
            }
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        val scanner = adapter.bluetoothLeScanner
        try {
            scanner.startScan(null, settings, scanCallback)
        } catch (e: Exception) {
            Log.d("Doran", "$e")
        }

        awaitClose {
            try {
                scanner.stopScan(scanCallback)
            } catch (e: Exception) {

            }
        }
    }

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun addPairedDeviceFlow(device: Device): Flow<Device> = callbackFlow {
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        gattList.add(gatt)
                        trySend(gatt.mapToDevice())
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {

                    }
                }
            }
        }

        adapter.bondedDevices.first { connectableDevice ->
            connectableDevice.address == device.address
        }.also {
            it.connectGatt(context, false, gattCallback)
        }

    }
}
