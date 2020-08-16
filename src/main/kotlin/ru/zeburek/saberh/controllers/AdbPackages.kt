package ru.zeburek.saberh.controllers

import ru.zeburek.saberh.controllers.LayoutContoller.Companion.tabContainer
import tornadofx.readonlyColumn
import tornadofx.select
import tornadofx.smartResize

fun AdbController.adbShowPackages(listType: String = "-3") {
    val job = runPerLineAction(cmdPackages(listType))
    outputTabPane.outputTabPane.apply {
        val newTab = tabContainer(job) {
            smartResize()
            readonlyColumn("#", OutputLine::lineNo)
            readonlyColumn("Message", OutputLine::string)
        }
        newTab.select()
    }
}