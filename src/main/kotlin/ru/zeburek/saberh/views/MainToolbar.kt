package ru.zeburek.saberh.views

import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.input.KeyCombination
import javafx.scene.paint.*
import javafx.scene.text.TextAlignment
import org.controlsfx.glyphfont.Glyph
import ru.zeburek.saberh.controllers.AdbController
import ru.zeburek.saberh.controllers.DevicesController
import ru.zeburek.saberh.controllers.adbDevices
import ru.zeburek.saberh.controllers.adbShowPackages
import ru.zeburek.saberh.models.Device
import ru.zeburek.saberh.models.DeviceStatus
import ru.zeburek.saberh.models.Package
import tornadofx.*

class MainToolbar : View("Main Toolbar") {
    val devicesController: DevicesController by inject(FX.defaultScope)
    val adbController: AdbController by inject(FX.defaultScope)
    var devicesComboBox: ComboBox<Device> by singleAssign()
    var packagesComboBox: ComboBox<Package> by singleAssign()

    override val root = toolbar {
        borderpaneConstraints {
            alignment = Pos.CENTER
            padding = insets(1.0)
            prefHeight = 40.0
        }
        menubutton(graphic = Glyph("FontAwesome", "BARS")) {
            id = "menuBarMenuButton"

            item("Save output...", KeyCombination.keyCombination("Ctrl+S")){id = "saveOutputMainMenuItem"}
            item("Reload lists", KeyCombination.keyCombination("Ctrl+R")){id = "reloadListsMainMenuItem"}
            item("Install App", KeyCombination.keyCombination("Ctrl+I")){id = "appInstallMainMenuItem"}
            item("Take screenshot", KeyCombination.keyCombination("Ctrl+T")){id = "takeScreenshotMainMenuItem"}
            item("About"){id = "aboutInfoMainMenuItem"}
        }
        button(graphic = Glyph("FontAwesome", "PLUS")) {
            id = "tabsAddTubButton"
            textAlignment = TextAlignment.CENTER
        }
        combobox(devicesController.currentDevice.itemProperty, devicesController.devices) {
            devicesComboBox = this
            prefWidth = 150.0
            promptText = "Select device ..."
            cellFormat {
                text = it.info
                isDisable = it.deviceStatus != DeviceStatus.DEVICE
                style {
                    if (it.deviceStatus != DeviceStatus.DEVICE)
                        textFill = c("#000000", 0.3)
                }
            }
            setOnAction {
                adbController.loadPackages()
            }
        }
        combobox(devicesController.currentPackage.itemProperty, devicesController.packages) {
            packagesComboBox = this
            prefWidth = 150.0
            promptText = "Select package ..."
            cellFormat {
                text = it.pkgName
            }
        }
        button(graphic = Glyph("FontAwesome", "REFRESH")) {
            id = "reloadListsButton"
            action { adbController.loadDevices() }
        }
        splitmenubutton("Logcat", Glyph("FontAwesome", "FILE_TEXT")){
            id = "logcatStartMenuItem"

            action { adbController.adbLogcat() }
            item("Stop", KeyCombination.keyCombination("Ctrl+D")){id = "logcatStopMenuItem"}
            item("Clear"){id = "logcatClearMenuItem"}
        }
        splitmenubutton("Devices", Glyph("FontAwesome", "MOBILE_PHONE")){
            id = "devicesListSplitButton"
            action { adbController.adbDevices() }

            item("UnAuth"){id = "devicesUnAuthMenuItem"}
        }
        splitmenubutton("Install App", Glyph("FontAwesome", "TASKS")){
            id = "devicesListSplitButton"

            item("List all packages"){ action { adbController.adbShowPackages("-a") } }
            item("List system packages"){ action { adbController.adbShowPackages("-s") } }
            item("List third-party packages"){ action { adbController.adbShowPackages() } }
            item("List uninstalled packages"){ action { adbController.adbShowPackages("-u") } }
            item("Clear app data"){id = "appClearMenuItem"}
            item("Uninstall app"){id = "appUninstallMenuItem"}
            item("Force-stop app"){id = "appStopMenuItem"}
            item("App version"){id = "appVersionMenuItem"}
            checkmenuitem("Save data on update"){
                id = "rApkInstallCheckbox"
                isSelected = true
            }
            checkmenuitem("Enable downgrade"){id = "dApkInstallCheckbox"}
        }
        splitmenubutton("Record Screen"){
            id = "screenVideoRecordSplitButton"
            graphic = Glyph("FontAwesome", "CIRCLE")
            graphic.style(true) {
                textFill = Paint.valueOf("linear-gradient(rgba(255,0,0,0.3),rgba(255,0,0,0.6),rgba(255,0,0,0.8))")
            }

            item("Take Screenshot", graphic = Glyph("FontAwesome", "CAMERA")){id = "screenTakeScreenshotMenuItem"}
        }
        textfield {
            id = "filterTextField"
            promptText = "Filter..."
        }
        textfield {
            id = "searchTextField"
            promptText = "Search..."
        }
        checkbox("Clear Out") {
            id = "outputClearCheckBox"
        }
    }
}
