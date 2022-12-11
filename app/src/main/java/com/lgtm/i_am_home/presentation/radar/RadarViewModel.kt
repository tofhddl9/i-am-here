package com.lgtm.i_am_home.presentation.radar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lgtm.i_am_home.data.BluetoothRepository
import com.lgtm.i_am_home.domain.Device
import com.lgtm.i_am_home.domain.usecase.ConnectDeviceUsecase
import com.lgtm.i_am_home.domain.usecase.ForgetDeviceUsecase
import com.lgtm.i_am_home.domain.usecase.ScanDeviceUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class RadarViewModel @Inject constructor(
    //private val bluetoothOnOff: BluetoothOnOffUsecase,
    private val scanDevice: ScanDeviceUsecase,
    private val connectDevice: ConnectDeviceUsecase,
    private val forgetDeviceUsecase: ForgetDeviceUsecase,
    private val repository: BluetoothRepository, // TODO : remove
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadarViewState())
    val uiState = _uiState.asStateFlow()

    private var job: Job? = null

    init {
        viewModelScope.launch {
            repository.rememberedDeviceList.collectLatest {
                _uiState.value = _uiState.value.copy(
                    rememberDeviceList = it
                )
            }
        }
    }

    fun forgetDevice(device: Device) {
        viewModelScope.launch {
            forgetDeviceUsecase.invoke(device)
        }
    }

    private fun startScan() {
        _uiState.value = _uiState.value.copy(
            progress = RadarProgress.SCANNING_NOT_FOUND
        )

        job = viewModelScope.launch {
            scanDevice(doesFilterOnlyRememberDevice = true).collectLatest {
                connectDevice(it).collectLatest { capturedDevices ->
                    Log.d("Doran", "captured : ${capturedDevices.size}")
                    _uiState.value = _uiState.value.copy(
                        progress = if (capturedDevices.isNotEmpty()) {
                            RadarProgress.SCANNING_FOUND_AT_LEAST_ONE
                        } else {
                            if (_uiState.value.progress == RadarProgress.SCANNING_FOUND_AT_LEAST_ONE) {
//                                cancel()
//                                RadarProgress.INIT
                                RadarProgress.SCANNING_NOT_FOUND
                            } else {
                                RadarProgress.SCANNING_NOT_FOUND
                            }
                        },
                        capturedDeviceList = capturedDevices,
                    )
                }
            }
        }
    }

    private fun stopScan() {
        _uiState.value = _uiState.value.copy(
            progress = RadarProgress.INIT,
            capturedDeviceList = emptyList()
        )

        job?.cancel()
    }

    fun onScanButtonClicked() {
        if (isScanning()) {
            stopScan()
        } else {
            startScan()
        }
    }

    private fun isScanning(): Boolean = _uiState.value.progress != RadarProgress.INIT

}