package ru.zeburek.saberh.models

import tornadofx.*

class Package(pkgName: String? = null, installer: String? = null, uid: Int? = null) {
    var pkgName by property(pkgName)
    val pkgNameProperty = getProperty(Package::pkgName)

    var installer by property(installer)
    val installerProperty = getProperty(Package::installer)

    var uid by property(uid)
    val uidProperty = getProperty(Package::uid)
}

class PackageModel(pkg: Package?) : ItemViewModel<Package>(pkg) {
    val id = bind(Package::pkgNameProperty)
    val deviceName = bind(Package::installerProperty)
    val deviceStatus = bind(Package::uidProperty)
}