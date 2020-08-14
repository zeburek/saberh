package ru.zeburek.saberh.models

import javafx.beans.property.*
import tornadofx.*

class Package(pkgName: String? = null, installer: String? = null, uid: Int = 0) {
    val pkgNameProperty = SimpleStringProperty(this, "pkgName", pkgName)
    var pkgName by pkgNameProperty

    val installerProperty = SimpleStringProperty(this, "installer", installer)
    var installer by installerProperty

    val uidProperty = SimpleIntegerProperty(this, "uid", uid)
    var uid by uidProperty
}

class PackageModel(pkg: Package?) : ItemViewModel<Package>(pkg) {
    val pkgName = bind(Package::pkgNameProperty)
    val installer = bind(Package::installerProperty)
    val uid = bind(Package::uidProperty)
}