package com.lgtm.i_am_home.data

import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

    private val gattSet: MutableSet<BluetoothGatt> = mutableSetOf()
    val pairedDeviceList: Flow<List<Device>> = flow {
        while (true) {
            emit(gattSet.map { it.mapToDevice() })
            delay(1000)
        }
    }

    val rememberedDevices: Set<Device> = dataSource.getMyDevice()

    fun addPairedDeviceFlow(device: Device) {
        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        gattSet.add(gatt)
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {

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

    fun rememberDevice(device: Device) {
        val deviceGatt = gattSet.find { it.device.address == device.address }
        deviceGatt?.let {
            dataSource.saveMyDevice(device)
        }
    }

    // TODO 1. discovery 무한대로 하기
    // TODO 2. 이전에 페어링한 기기 저장해서 스캔 감지시 자동 연결하기.
    // TODO 3. 애니메이션
    fun registerBluetoothReceiver(){
        adapter.startDiscovery()
        //intentfilter
        val stateFilter = IntentFilter()
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED) //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        stateFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED) //연결 확인
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED) //연결 끊김 확인
        stateFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        stateFilter.addAction(BluetoothDevice.ACTION_FOUND) //기기 검색됨
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) //기기 검색 시작
        stateFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) //기기 검색 종료
        stateFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action //입력된 action
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                var name = device?.name ?: "Unknown"

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
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> { }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        //디바이스가 연결 해제될 경우
                        //connected.postValue(false)
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> { }
                    BluetoothDevice.ACTION_FOUND -> {
                        device ?: return
                        scannedDeviceSet.add(device)
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> { }

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
