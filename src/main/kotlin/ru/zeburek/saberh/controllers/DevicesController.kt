package ru.zeburek.saberh.controllers

import javafx.collections.FXCollections
import ru.zeburek.saberh.models.Device
import ru.zeburek.saberh.models.DeviceModel
import ru.zeburek.saberh.models.Package
import ru.zeburek.saberh.models.PackageModel
import tornadofx.*

class DevicesController : Controller() {
    val devices = FXCollections.observableArrayList<Device>()
    val currentDevice = DeviceModel(null)
    val packages = FXCollections.observableArrayList<Package>()
    val currentPackage = PackageModel(null)

}