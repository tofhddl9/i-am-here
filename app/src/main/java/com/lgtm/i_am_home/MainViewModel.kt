package com.lgtm.i_am_home

import androidx.lifecycle.ViewModel
import com.lgtm.i_am_home.usecase.ConnectDeviceUsecase
import com.lgtm.i_am_home.usecase.ReadFromConnectedDeviceUsecase
import com.lgtm.i_am_home.usecase.ScanDeviceUsecase
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MainViewModel(
    private val scanDevice: ScanDeviceUsecase,
    private val connectDevice: ConnectDeviceUsecase,
    private val readFromConnectedDevice: ReadFromConnectedDeviceUsecase,
) : ViewModel() {



}