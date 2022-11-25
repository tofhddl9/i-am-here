package com.lgtm.i_am_home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lgtm.i_am_home.usecase.ConnectDeviceUsecase
import com.lgtm.i_am_home.usecase.ReadFromConnectedDeviceUsecase
import com.lgtm.i_am_home.usecase.ScanDeviceUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    //private val bluetoothOnOff: BluetoothOnOffUsecase,
    private val scanDevice: ScanDeviceUsecase,
    private val connectDevice: ConnectDeviceUsecase,
    //private val readFromConnectedDevice: ReadFromConnectedDeviceUsecase,
) : ViewModel() {

    fun startScan() {
        viewModelScope.launch {
            scanDevice().collect { it ->
                Log.d("Doran", "$it")
            }

        }
    }

}