package com.lgtm.i_am_home.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lgtm.i_am_home.R
import com.lgtm.i_am_home.databinding.FragmentMainBinding
import com.lgtm.i_am_home.delegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment: Fragment(R.layout.fragment_main) {

    private val binding: FragmentMainBinding by viewBinding(FragmentMainBinding::bind)

    private val viewModel: MainViewModel by viewModels()

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermission()
        initViews()
        observeViewModel()
    }

    private fun initViews() {
        initButtons()
        initRecyclerViews()
    }

    private fun initRecyclerViews() {
        binding.pairedDeviceList

        binding.connectableDeviceList
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
            viewModel.startScan()
        }

        binding.bluetoothToggleButton.setOnClickListener {

        }
    }

    private fun observeViewModel() {

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

    companion object {
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    }
}