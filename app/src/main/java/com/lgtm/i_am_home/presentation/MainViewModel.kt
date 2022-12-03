package com.lgtm.i_am_home.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lgtm.i_am_home.domain.Device
import com.lgtm.i_am_home.usecase.ConnectDeviceUsecase
import com.lgtm.i_am_home.usecase.ReadFromConnectedDeviceUsecase
import com.lgtm.i_am_home.usecase.ScanDeviceUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    //private val bluetoothOnOff: BluetoothOnOffUsecase,
    private val scanDevice: ScanDeviceUsecase,
    private val connectDevice: ConnectDeviceUsecase,
    //private val readFromConnectedDevice: ReadFromConnectedDeviceUsecase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewState())
    val uiState = _uiState.asStateFlow()

    fun startScan() {
        viewModelScope.launch {
            scanDevice().collectLatest { devices ->
                _uiState.value = _uiState.value.copy(
                    scannedDeviceList = devices
                )
            }

        }
    }

    fun connectDevice(device: Device) {
        viewModelScope.launch {
            connectDevice.invoke(device).collectLatest { devices ->
                _uiState.value = _uiState.value.copy(
                    pairedDeviceList = devices
                )
            }
        }
    }

}