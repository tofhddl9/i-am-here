package com.lgtm.i_am_home

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.core.os.HandlerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Exception
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BluetoothRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adapter: BluetoothAdapter,
) {

    private val gattList: MutableList<BluetoothGatt> = ArrayList()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun scannedDeviceFlow(): Flow<ScanResult> = callbackFlow {
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(result).onFailure { }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun pairedDeviceFlow(device: BluetoothDevice): Flow<BluetoothGatt> = callbackFlow {
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        trySend(gatt)
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {

                    }
                }
            }
        }

        if (adapter.bondedDevices.contains(device)) {
            device.connectGatt(context, false, gattCallback)
        }

    }
}
