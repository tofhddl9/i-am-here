package com.lgtm.i_am_home.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lgtm.i_am_home.data.BluetoothRepository
import com.lgtm.i_am_home.domain.Device
import com.lgtm.i_am_home.presentation.radar.RadarProgress
import com.lgtm.i_am_home.usecase.ConnectDeviceUsecase
import com.lgtm.i_am_home.usecase.RememberDeviceUsecase
import com.lgtm.i_am_home.usecase.ScanDeviceUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    //private val bluetoothOnOff: BluetoothOnOffUsecase,
    private val scanDevice: ScanDeviceUsecase,
    private val connectDevice: ConnectDeviceUsecase,
    private val rememberDevice: RememberDeviceUsecase,
    private val repository: BluetoothRepository,
    //private val readFromConnectedDevice: ReadFromConnectedDeviceUsecase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainViewState())
    val uiState = _uiState.asStateFlow()

    private var job: Job? = null

    init {
//        viewModelScope.launch {
//            repository.rememberedDeviceList.collectLatest {
//                _uiState.value = _uiState.value.copy(
//                    pairedDeviceList = it
//                )
//            }
//        }
    }

    private fun startScan() {
        job = viewModelScope.launch {
            scanDevice().collectLatest { devices ->
                _uiState.value = _uiState.value.copy(
                    scannedDeviceList = devices,
                    isScanning = true
                )
            }

        }
    }

    private fun stopScan() {
        _uiState.value = _uiState.value.copy(
            isScanning = false
        )

        job?.cancel()
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

    fun rememberDevice(device: Device) {
        viewModelScope.launch {
            rememberDevice.invoke(device)
        }
    }

    fun onScanButtonClicked() {
        if (isScanning()) {
            stopScan()
        } else {
            startScan()
        }
    }

    private fun isScanning(): Boolean = _uiState.value.isScanning
}