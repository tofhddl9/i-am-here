package com.lgtm.i_am_home

import java.util.*
import kotlin.experimental.and

class XiaomiSensor {
    var updateTime: Long = 0
    var temperature = 0f
    var humidity = 0

    constructor() {}
    constructor(updateTime: Long, temperature: Float, humidity: Int) {
        this.updateTime = updateTime
        this.temperature = temperature
        this.humidity = humidity
    }

    companion object {
        const val DEVICE_TYPE = "5b 05" // LYWSD03MMC
        fun isType(serviceData: ByteArray): Boolean {
            return byteArrayToHex(serviceData).lowercase(Locale.getDefault()).contains(
                DEVICE_TYPE
            )
        }

        fun byteArrayToHex(a: ByteArray): String {
            val sb = StringBuilder()
            for (b in a) sb.append(String.format("%02x ", b and 0xff.toByte()))
            return sb.toString()
        }
    }
}