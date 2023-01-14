package com.lgtm.i_am_home.presentation.radar

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lgtm.i_am_home.R
import com.lgtm.i_am_home.databinding.FragmentRadarBinding
import com.lgtm.i_am_home.delegate.viewBinding
import com.lgtm.i_am_home.domain.Device
import com.lgtm.i_am_home.presentation.main.MainViewModel
import com.lgtm.i_am_home.presentation.main.ScannedDeviceListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RadarFragment: Fragment(R.layout.fragment_radar) {

    private val binding: FragmentRadarBinding by viewBinding(FragmentRadarBinding::bind)

    private val viewModel: RadarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        initButtons()
        initRecyclerViews()
    }

    private fun initRecyclerViews() {
        binding.targetList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = RadarTargetListAdapter(::onRememberedDeviceItemClicked)
        }

    }

    private fun initButtons() {
        binding.scanStartButton.setOnClickListener {
            //TODO : let's notify event by channel
            viewModel.onScanButtonClicked()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    updateRememberDeviceList(uiState.rememberDeviceList)
                    updateCapturedDeviceList(uiState.capturedDeviceList)
                    updateRadar(uiState.progress)
                }
            }
        }
    }

    private fun updateRememberDeviceList(rememberDeviceList: List<Device>) {
        (binding.targetList.adapter as? RadarTargetListAdapter)?.submitList(rememberDeviceList)
    }

    private fun updateCapturedDeviceList(capturedDeviceList: List<Device>) {
        if (capturedDeviceList.isEmpty()) return

        var msg = ""
        capturedDeviceList.forEach {
            msg += "${it.name} "
        }
        msg += "이 연결되었습니다"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    private fun updateRadar(progress: RadarProgress) {
        when (progress) {
            RadarProgress.INIT -> {
                stopRadar()
            }
            RadarProgress.SCANNING_NOT_FOUND -> {
                setRadarColor("#ff0000")
                startRadar()
            }
            RadarProgress.SCANNING_FOUND_AT_LEAST_ONE -> {
                setRadarColor("#00ff00")
                startRadar()
            }
        }
    }

    private fun startRadar() {
        if (!binding.radar.isStarted) {
            binding.radar.start()
        }

        binding.scanStartButton.text = "스캔 중지"
    }

    private fun stopRadar() {
        if (binding.radar.isStarted) {
            binding.radar.stop()
        }

        binding.scanStartButton.text = "스캔 시작"
    }

    private fun setRadarColor(colorString: String) {
        binding.radar.color = Color.parseColor(colorString)
        binding.radar.duration++ // lib has bug. setColor does not call reset now. :(
    }

    private fun onRememberedDeviceItemClicked(device: Device) {
        AlertDialog.Builder(context)
            .setMessage("이 장치를 더 이상 자동연결하지 않습니다")
            .setPositiveButton("네") { _, _ ->
                viewModel.forgetDevice(device)
            }.setNegativeButton("아니오") { _, _ ->

            }.create()
            .show()
    }

}
// for test