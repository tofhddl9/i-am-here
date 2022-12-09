package com.lgtm.i_am_home.presentation.radar

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lgtm.i_am_home.R
import com.lgtm.i_am_home.databinding.FragmentRadarBinding
import com.lgtm.i_am_home.delegate.viewBinding
import com.lgtm.i_am_home.presentation.main.MainViewModel
import com.lgtm.i_am_home.presentation.main.ScannedDeviceListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RadarFragment: Fragment(R.layout.fragment_radar) {

    private val binding: FragmentRadarBinding by viewBinding(FragmentRadarBinding::bind)

    private val viewModel: MainViewModel by viewModels()

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
//        binding.connectableDeviceList.apply {
//            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//            adapter = ScannedDeviceListAdapter(::onScannedItemClick)
//        }

    }

    private fun initButtons() {
//        binding.scanButton.setOnClickListener {
//            requireContext().checkAllPermission(REQUIRED_PERMISSIONS)
//            viewModel.startScan()
//        }

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                }
            }
        }
    }

}