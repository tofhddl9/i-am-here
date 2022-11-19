package com.lgtm.i_am_home

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.lgtm.i_am_home.databinding.ActivityMainBinding
import com.lgtm.i_am_home.permission.PermissionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), BluetoothGuide.OnCheckModelListener, BluetoothGuide.OnNotifyValueListener<XiaomiSensor> {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    // TODO : Hilt
    private val permissionManager = PermissionManager(this)

    private lateinit var bluetoothGuide: BluetoothGuide

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bluetoothGuide = BluetoothGuide(
            adapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        ).apply {
            setOnCheckModelListener(this@MainActivity)
            setOnNotifyValueListener(this@MainActivity)
        }

        // Bluetooth System On with permission
        if (bluetoothGuide.isOn) {
            requestLocationPermission()
        } else {
            requestBluetoothOn()
        }

        setButtons()
    }

    private fun requestBluetoothOn() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(intent, BluetoothGuide.INTENT_REQUEST_BLUETOOTH_ENABLE)
    }

    private fun setButtons() {
        binding.scanButton.setOnClickListener {
            bluetoothGuide.scanDevices()
        }
    }

    private fun requestLocationPermission() {
        permissionManager.setPermissions(100, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            .onPermissionGranted { }
            .onPermissionDenied { }
            .request()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handleRequestPermissionResult(requestCode, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear the resources
        bluetoothGuide.disconnectGATTAll()
        bluetoothGuide.onComplete()
    }

    override fun onValue(deivce: BluetoothDevice?, value: XiaomiSensor) {

    }

    override fun isChecked(bytes: ByteArray?): Boolean {
        bytes ?: return false

        return XiaomiSensor.isType(bytes)
    }

    override fun scannedDevice(result: ScanResult?) {
        // Start Connecting device.
        Log.d("Doran", result?.device.toString())
        bluetoothGuide.connGATT(applicationContext, result!!.device)
    }

    override fun formatter(characteristic: BluetoothGattCharacteristic?): XiaomiSensor {
        // Format the data
        val value = characteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0) ?: 0
        val value2 = characteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1) ?: 0
        val temperature = value * 0.01f
        val humidity = value2 and 0xFF00 shr 8
        return XiaomiSensor(
            System.currentTimeMillis(),
            temperature,
            humidity
        )
    }

}