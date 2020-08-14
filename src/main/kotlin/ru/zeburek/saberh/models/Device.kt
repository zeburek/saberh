package ru.zeburek.saberh.models

import tornadofx.*

enum class DeviceStatus(val status: String) {
    DEVICE("device"),
    UNAUTHORIZED("unauthorized"),
    UNKNOWN("");

    companion object {
        fun valueOfStatus(status: String): DeviceStatus {
            enumValues<DeviceStatus>().forEach {
                if (it.status == status)
                    return it
            }
            return UNKNOWN
        }
    }
}

class Device(id: String? = null, deviceName: String? = null, deviceStatus: DeviceStatus? = null) {
    var id by property(id)
    val idProperty = getProperty(Device::id)

    var deviceName by property(deviceName)
    val deviceNameProperty = getProperty(Device::deviceName)

    var deviceStatus by property(deviceStatus)
    val deviceStatusProperty = getProperty(Device::deviceStatus)

    val info
        get() = "$deviceName ($id)"
}

class DeviceModel(device: Device?) : ItemViewModel<Device>(device) {
    val id = bind(Device::idProperty)
    val deviceName = bind(Device::deviceNameProperty)
    val deviceStatus = bind(Device::deviceStatusProperty)
    val info = bind(Device::info)
}
