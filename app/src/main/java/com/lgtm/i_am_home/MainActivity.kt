package com.lgtm.i_am_home

import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import com.lgtm.i_am_home.Constant.PERMISSIONS
import com.lgtm.i_am_home.Constant.REQUEST_ALL_PERMISSION
import com.lgtm.i_am_home.Constant.REQUEST_ENABLE_BT
import com.lgtm.i_am_home.databinding.ActivityMainBinding
import com.lgtm.i_am_home.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

private const val SCAN_PERIOD: Long = 10000

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private var scanResults: ArrayList<BluetoothDevice>? = ArrayList()
    private var bleGatt: BluetoothGatt? = null

    private val BLEScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(_error: Int) {
        }

        /**
         * Add scan result
         */
        private fun addScanResult(result: ScanResult) {
            // get scanned device
            val device = result.device
            // get scanned device MAC address
            val deviceAddress = device.address
            val deviceName = device.name

            // 중복 체크
            for (dev in scanResults!!) {
                if (dev.address == deviceAddress) return
            }
            // add arrayList
            scanResults?.add(result.device)
            // status text UI update
            // statusTxt.set("add scanned device: $deviceAddress")

            // TODO
            // scanlist update 이벤트
            // _listUpdate.value = Event(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkSupportBLE()
        requestPermission()

        setButtons()
    }

    private fun setButtons() {
        binding.scanButton.setOnClickListener {
            if (bluetoothAdapter == null || !bluetoothAdapter?.isEnabled!!) {
                requestEnableBLE()
            }

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()

            bluetoothAdapter?.bluetoothLeScanner?.startScan(null, settings, BLEScanCallback)
        }
    }

    // TODO : Dialog가 더 적합
    private fun checkSupportBLE() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showSnackBar("BLE를 지원하지 않는 기기입니다.\n3초뒤 앱을 종료합니다.")
            Handler().postDelayed ({
                finish()
            }, 3000)
        }
    }


    private fun requestPermission() {
        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }
    }

    // TODO : requestContract
    private fun requestEnableBLE() {
        val bleEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(bleEnableIntent, REQUEST_ENABLE_BT)
    }

    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSnackBar("Permissions granted!")
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    showSnackBar("Permissions must be granted!")
                }
            }
        }
    }


}