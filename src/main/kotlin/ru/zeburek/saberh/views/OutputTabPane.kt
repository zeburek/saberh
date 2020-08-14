package ru.zeburek.saberh.views

import tornadofx.*
import javafx.scene.control.TabPane
import ru.zeburek.saberh.controllers.AdbController

class OutputTabPane : View("My View") {
    val adbController: AdbController by inject()
    var outputTabPane: TabPane by singleAssign()

    override val root = tabpane {
        outputTabPane = this
        id = "outputTabbedPane"
        tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
    }

}
