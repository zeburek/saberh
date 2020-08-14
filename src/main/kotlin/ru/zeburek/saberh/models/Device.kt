package ru.zeburek.saberh.models

import javafx.beans.property.*
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
    val idProperty = SimpleStringProperty(this, "id", id)
    var id by idProperty

    val deviceNameProperty = SimpleStringProperty(this, "deviceName", deviceName)
    var deviceName by deviceNameProperty

    val deviceStatusProperty = SimpleObjectProperty<DeviceStatus>(this, "deviceStatus", deviceStatus)
    var deviceStatus by deviceStatusProperty

    val info
        get() = "$deviceName ($id)"
}

class DeviceModel(device: Device?) : ItemViewModel<Device>(device) {
    val id = bind(Device::idProperty)
    val deviceName = bind(Device::deviceNameProperty)
    val deviceStatus = bind(Device::deviceStatusProperty)
    val info = bind(Device::info)
}
