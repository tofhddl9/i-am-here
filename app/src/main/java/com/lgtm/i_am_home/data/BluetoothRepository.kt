package com.lgtm.i_am_home.data

import android.bluetooth.*
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.lgtm.i_am_home.data.mapper.DeviceMapper.mapToDevice
import com.lgtm.i_am_home.domain.Device
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class BluetoothRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataSource: DeviceDataSource,
    private val adapter: BluetoothAdapter,
) {

    var br : BroadcastReceiver? = null

    private val scannedDeviceSet: MutableSet<BluetoothDevice> = mutableSetOf()
    val scannedDeviceList: Flow<List<Device>> = flow {
        while (true) {
            emit(scannedDeviceSet.map { it.mapToDevice() })
            delay(1000)
        }
    }

    // TODO: filter in Domain module
    private val filteredScannedDeviceSet: MutableSet<BluetoothDevice> = mutableSetOf()
    val filteredScannedDeviceList: Flow<List<Device>> = flow {
        while (true) {
            Log.d("Doran", "scanned : ${filteredScannedDeviceSet.size}")
            emit(filteredScannedDeviceSet.map { it.mapToDevice() })
            delay(1000)
        }
    }

    private val filteredPairedDeviceSet: MutableSet<BluetoothGatt> = mutableSetOf()
    val filteredPairedDeviceList: Flow<List<Device>> = flow {
        while (true) {
            emit(filteredPairedDeviceSet.map { it.mapToDevice() })
            delay(1000)
        }
    }

    private val gattSet: MutableSet<BluetoothGatt> = mutableSetOf()
    val pairedDeviceList: Flow<List<Device>> = flow {
        while (true) {
            emit(gattSet.map { it.mapToDevice() })
            delay(1000)
        }
    }

    private val rememberedDevices: MutableSet<Device> = dataSource.getMyDevice().toMutableSet()
    val rememberedDeviceList: Flow<List<Device>> = flow {
        while (true) {
            emit(rememberedDevices.toList())
            delay(1000)
        }
    }

    fun addPairedDeviceFlow(device: Device) {
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        gattSet.find { it.device.address == gatt.device.address } ?: run {
                            gattSet.add(gatt)
                        }

//                        scannedDeviceSet.removeIf { it.address == gatt.device.address }
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        gatt.close()
                        gattSet.removeIf { it.device.address == gatt.device.address }
                    }
                }
            }
        }

        scannedDeviceSet.forEach {
            if (it.address == device.address) {
                it.connectGatt(context, false, gattCallback)
                return@forEach
            }
        }
    }

    // TODO : integrate
    fun connectDeviceFlow(devices: List<Device>) {
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        val device = filteredPairedDeviceSet.find { it.device.address == gatt.device.address }
                        if (device == null) {
                            Log.d("Doran", "${gatt.device.address} is Connected")
                            filteredPairedDeviceSet.add(gatt)
                        } else {
                            filteredPairedDeviceSet.remove(device)
                            filteredPairedDeviceSet.add(gatt)
                        }


//                        scannedDeviceSet.removeIf { it.address == gatt.device.address }
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d("Doran", "${gatt.device.name} is Disconnected")
                        gatt.close()
                        filteredPairedDeviceSet.removeIf { it.device.address == gatt.device.address }
                        filteredScannedDeviceSet.removeIf { it.address == gatt.device.address }
                    }
                }
            }
        }

        devices.forEach { device ->
            filteredScannedDeviceSet.find { it.address == device.address}?.run {
                filteredPairedDeviceSet.find { it.device.address == address } ?: run {
                    Log.d("Doran", "try to connect real ${this.name}")
                    connectGatt(context, false, gattCallback)
                }
//                filteredPairedDeviceSet.add(this)
            }
            return@forEach
        }
    }

    fun rememberDevice(device: Device) {
        val deviceGatt = gattSet.find { it.device.address == device.address }
        deviceGatt?.let {
            dataSource.saveMyDevice(device)
        }
        rememberedDevices.add(device)
    }

    fun removeDevice(device: Device) {
        rememberedDevices.removeIf {
            it.address == device.address
        }
        dataSource.removeDevice(device)
    }

    // TODO: 정리
    fun registerBluetoothReceiver(){
        adapter.startDiscovery()
        //intentfilter
        val stateFilter = IntentFilter()
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED) //연결 확인
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //연결 끊김 확인
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND) //기기 검색됨
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //기기 검색 시작
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //기기 검색 종료
        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action //입력된 action
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                when (action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                        when (state) {
                            BluetoothAdapter.STATE_OFF -> { }
                            BluetoothAdapter.STATE_TURNING_OFF -> { }
                            BluetoothAdapter.STATE_ON -> { }
                            BluetoothAdapter.STATE_TURNING_ON -> { }
                        }
                    }
                    BluetoothDevice.ACTION_ACL_CONNECTED -> { }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {}
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> { }
                    BluetoothDevice.ACTION_FOUND -> {
                        device?.name ?: return

                        Log.d("Doran", "${device.name} is Scanned")
                        scannedDeviceSet.add(device)

                        // TODO: Usecase
                        dataSource.getMyDevice().find {
                            it.address == device.address
                        }?.also {
                            filteredScannedDeviceSet.add(device)
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        adapter.cancelDiscovery()
                        adapter.startDiscovery()
                        Log.d("Doran", "restart discovery")
                    }

                }
            }
        }
        //리시버 등록
        context.registerReceiver(
            br,
            stateFilter
        )

    }
}
