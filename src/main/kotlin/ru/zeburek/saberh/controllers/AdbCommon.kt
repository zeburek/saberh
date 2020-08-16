package ru.zeburek.saberh.controllers

import ru.zeburek.saberh.controllers.LayoutContoller.Companion.tabContainer
import tornadofx.readonlyColumn
import tornadofx.select
import tornadofx.smartResize


fun AdbController.adbDevices() {
    val job = runResultsAction(cmdDevices)
    outputTabPane.outputTabPane.apply {
        val newTab = tabContainer(job) {
            smartResize()
            readonlyColumn("#", OutputLine::lineNo)
            readonlyColumn("Message", OutputLine::string)
        }
        newTab.select()
    }
}

fun AdbController.adbStartServer() = runResultsAction(cmdStartServer)
fun AdbController.adbStopServer() = runResultsAction(cmdKillServer)
