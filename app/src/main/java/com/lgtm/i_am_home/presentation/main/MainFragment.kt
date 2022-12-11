package com.lgtm.i_am_home.presentation.main

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lgtm.i_am_home.R
import com.lgtm.i_am_home.databinding.FragmentMainBinding
import com.lgtm.i_am_home.delegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.lgtm.i_am_home.domain.Device
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainFragment: Fragment(R.layout.fragment_main) {

    private val binding: FragmentMainBinding by viewBinding(FragmentMainBinding::bind)

    private val viewModel: MainViewModel by viewModels()

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermission()
        registerBluetoothBR()
        initViews()
        observeViewModel()
    }

    private fun registerBluetoothBR() {

    }

    private fun initViews() {
        initButtons()
        initRecyclerViews()
    }

    private fun initRecyclerViews() {
        binding.connectableDeviceList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ScannedDeviceListAdapter(::onScannedItemClick)
        }

        binding.pairedDeviceList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = PairedDeviceListAdapter(::onPairedItemClick)
        }
    }

    private fun onScannedItemClick(device: Device) {
        AlertDialog.Builder(context)
            .setMessage("페어링 하시겠습니까?")
            .setPositiveButton("네") { _, _ ->
                viewModel.connectDevice(device)
            }.setNegativeButton("아니오") { _, _ ->

            }.create()
            .show()
    }

    private fun onPairedItemClick(device: Device) {
        AlertDialog.Builder(context)
            .setMessage("이 장치를 기억하시겠습니까?")
            .setPositiveButton("네") { _, _ ->
                viewModel.rememberDevice(device)
            }.setNegativeButton("아니오") { _, _ ->

            }.create()
            .show()
    }

    private fun requestPermission() {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
        }.also {
            it.launch(REQUIRED_PERMISSIONS)
        }
    }

    private fun initButtons() {
        binding.scanButton.setOnClickListener {
            requireContext().checkAllPermission(REQUIRED_PERMISSIONS)
            viewModel.onScanButtonClicked()
        }

        binding.radarButton.setOnClickListener {
            moveToRadarPage()
        }

//        binding.bluetoothToggleButton.setOnClickListener {
//        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { uiState ->
                    updateScannedDeviceList(uiState.scannedDeviceList)
                    updatePairedDeviceList(uiState.pairedDeviceList)
                    updateScanState(uiState.isScanning)
                }
            }
        }
    }

    private fun updateScannedDeviceList(scannedDeviceList: List<Device>) {
        (binding.connectableDeviceList.adapter as? ScannedDeviceListAdapter)?.submitList(scannedDeviceList)
    }

    private fun updatePairedDeviceList(pairedDeviceList: List<Device>) {
        (binding.pairedDeviceList.adapter as? PairedDeviceListAdapter)?.submitList(pairedDeviceList)
    }

    private fun updateScanState(isScanning: Boolean) {
        if (isScanning) {
            binding.scanButton.text = "스캔 중지"
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.scanButton.text = "스캔 시작"
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun Context.checkAllPermission(permission: Array<String>) : Boolean {
        var result = false
        permission.forEach {
            result = result or checkSinglePermission(it)
        }
        return result
    }

    private fun Context.checkSinglePermission(permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun moveToRadarPage() {
        val action = MainFragmentDirections.actionMainFragmentToRadarFragment()
        findNavController().navigate(action)
    }

    companion object {
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
        )
    }
}