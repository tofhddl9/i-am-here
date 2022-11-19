package com.lgtm.i_am_home

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import com.lgtm.i_am_home.usecase.ScanDeviceUsecase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class BluetoothGuide(
    private val adapter: BluetoothAdapter
) {
    private val gattList: MutableList<BluetoothGatt> = ArrayList()
    private val hashDeviceMap: HashMap<String, BluetoothDevice?> = HashMap()
    private val mainThreadHandler: Handler = HandlerCompat.createAsync(Looper.getMainLooper())
    private var scanning = false

    data class Device(
        val id: String
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun scannedDevicesFlow(): Flow<Device> = callbackFlow {
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)

                val serviceDataMap: Map<ParcelUuid, ByteArray> = result.scanRecord?.serviceData ?: return
                onCheckModelListener ?: return

                for (parcelUuid in serviceDataMap.keys) {
                    if (onCheckModelListener!!.isChecked(result.scanRecord?.getServiceData(parcelUuid))) {
                        if (!hasDevice(result.device.toString())) {
                            addDevice(result.device.address, result.device)
                            onCheckModelListener?.scannedDevice(result)
                            sendBlocking(Device("1"))
                        }
                        break
                    }
                }
            }
        }
    }

    /**
     * System Bluetooth On Check
     */
    val isOn: Boolean
        get() = adapter.isEnabled

    /**
     * Check model for ScanRecodeData
     */
    interface OnCheckModelListener {
        fun isChecked(bytes: ByteArray?): Boolean
        fun scannedDevice(result: ScanResult?)
    }

    private var onCheckModelListener: OnCheckModelListener? = null
    fun setOnCheckModelListener(onCheckModelListener: OnCheckModelListener?): BluetoothGuide {
        this.onCheckModelListener = onCheckModelListener
        return this
    }

    private val callback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val serviceDataMap: Map<ParcelUuid, ByteArray> = result.scanRecord?.serviceData ?: return
            onCheckModelListener ?: return

            for (parcelUuid in serviceDataMap.keys) {
                if (onCheckModelListener!!.isChecked(result.scanRecord?.getServiceData(parcelUuid))) {
                    if (!hasDevice(result.device.toString())) {
                        addDevice(result.device.address, result.device)
                        onCheckModelListener?.scannedDevice(result)
                    }
                    break
                }
            }
        }
    }

    /**
     * DO NOT CONNECT DEVICE
     */
    private fun addDevice(address: String, device: BluetoothDevice) {
        hashDeviceMap[address] = device
    }

    /**
     * DO NOT CONNECT DEVICE
     */
    private fun hasDevice(address: String): Boolean {
        return hashDeviceMap[address] != null
    }

    /**
     * DO NOT CONNECT DEVICE
     */
    fun onComplete() {
        hashDeviceMap.clear()
    }

    /**
     * Start Scan
     */
    fun scanDevices() {
        if (!adapter.isEnabled) return
        if (scanning) return
        val scanner = adapter.bluetoothLeScanner
        mainThreadHandler.postDelayed({
            scanning = false
            scanner.stopScan(callback)
        }, 2 * 60 * 1000)
        scanning = true
        scanner.startScan(callback)
    }

    /**
     * Connecting Device
     */
    fun connGATT(context: Context?, device: BluetoothDevice) {
        gattList.add(device.connectGatt(context, false, gattCallback))
    }

    /**
     * Disconnected All Device
     */
    fun disconnectGATTAll() {
        for (bluetoothGatt in gattList) {
            bluetoothGatt.disconnect()
            bluetoothGatt.close()
        }
        gattList.clear()
    }

    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (status == BluetoothGatt.GATT_FAILURE) {
                gatt.disconnect()
                gatt.close()
                hashDeviceMap.remove(gatt.device.address)
                return
            }
            if (status == 133) // Unknown Error
            {
                gatt.disconnect()
                gatt.close()
                hashDeviceMap.remove(gatt.device.address)
                return
            }
            if (newState == BluetoothGatt.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                // "Connected to " + gatt.getDevice().getName()
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val services = gatt.services
                for (service in services) {
                    // "Found service : " + service.getUuid()
                    for (characteristic in service.characteristics) {
                        //"Found characteristic : " + characteristic.getUuid()
                        if (hasProperty(
                                characteristic,
                                BluetoothGattCharacteristic.PROPERTY_READ
                            )
                        ) {
                            // "Read characteristic : " + characteristic.getUuid());
                            gatt.readCharacteristic(characteristic)
                        }
                        if (hasProperty(
                                characteristic,
                                BluetoothGattCharacteristic.PROPERTY_NOTIFY
                            )
                        ) {
                            // "Register notification for characteristic : " + characteristic.getUuid());
                            gatt.setCharacteristicNotification(characteristic, true)
                        }
                    }
                }
            }
        }

        // 가이드를 사용하는애가 데이터에 대해 알고 있음.
        // 가이드는 데이터의 구조에 대해서는 모름
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (onReadValueListener == null) return
                // This is Background Thread
                mainThreadHandler.post {
//                    onReadValueListener!!.onValue(
//                        gatt.device,
//                        onReadValueListener!!.formatter(characteristic)
//                    )
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (onNotifyValueListener == null) return
            // This is Background Thread
            mainThreadHandler.post {
//                onNotifyValueListener!!.onValue(
//                    gatt.device,
//                    onNotifyValueListener?.formatter(characteristic)
//                )
            }
        }
    }

    fun hasProperty(characteristic: BluetoothGattCharacteristic, property: Int): Boolean {
        val prop = characteristic.properties and property
        return prop == property
    }

    interface OnNotifyValueListener<T> {
        fun onValue(deivce: BluetoothDevice?, value: T)
        fun formatter(characteristic: BluetoothGattCharacteristic?): T
    }

    interface OnReadValueListener<T> {
        fun onValue(deivce: BluetoothDevice?, value: T?)
        fun formatter(characteristic: BluetoothGattCharacteristic?): T
    }

    private var onNotifyValueListener: OnNotifyValueListener<*>? = null
    fun setOnNotifyValueListener(onNotifyValueListener: OnNotifyValueListener<*>?): BluetoothGuide {
        this.onNotifyValueListener = onNotifyValueListener
        return this
    }

    private var onReadValueListener: OnReadValueListener<*>? = null
    fun setOnReadValueListener(onReadValueListener: OnReadValueListener<*>?): BluetoothGuide {
        this.onReadValueListener = onReadValueListener
        return this
    }

    companion object {
        const val INTENT_REQUEST_BLUETOOTH_ENABLE = 0x0701
    }
}