package com.lgtm.i_am_home.domain

data class Device(
    val name: String,
    val address: String,
    val isConnected: Boolean,
)