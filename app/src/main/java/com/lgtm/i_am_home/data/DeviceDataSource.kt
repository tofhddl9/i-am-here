package com.lgtm.i_am_home.data

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import com.lgtm.i_am_home.domain.Device

interface DeviceDataSource {
    fun saveMyDevice(device: Device)
    fun removeDevice(device: Device)
    fun getMyDevice(): Set<Device>
}

class DeviceLocalDataSource @Inject constructor(
    private val sharedPref: SharedPreferences,
) : DeviceDataSource {

    private var registeredDevices: Set<Device>? = null

    override fun saveMyDevice(device: Device) {
        val newRegisteredDevices = get().toMutableSet().apply {
            add(device)
        }

        save(newRegisteredDevices)
    }

    override fun removeDevice(device: Device) {
        get().filterNot { it.address == device.address }
            .toMutableSet()
            .also {
                save(it)
            }
    }

    override fun getMyDevice(): Set<Device> {
        return get()
    }

    private fun get(): Set<Device> {
        if (registeredDevices == null) {
            val json = sharedPref.getString(REGISTERED_DEVICES, null) ?: return emptySet()
            val type = object : TypeToken<Set<Device>>() {}.type

            registeredDevices = Gson().fromJson(json, type)
        }

        return registeredDevices ?: emptySet()
    }

    private fun save(deviceSet: Set<Device>) {
        registeredDevices = deviceSet

        if (deviceSet.isEmpty()) {
            sharedPref.edit().remove(REGISTERED_DEVICES).apply()

            return
        }

        val json = Gson().toJson(registeredDevices)
        sharedPref.edit().putString(REGISTERED_DEVICES, json).apply()
    }

    companion object {
        private const val REGISTERED_DEVICES = "REGISTERED_DEVICE"
    }
}
