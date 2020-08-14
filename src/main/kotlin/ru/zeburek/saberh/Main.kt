package ru.zeburek.saberh

import javafx.scene.layout.AnchorPane
import ru.zeburek.saberh.views.MasterView
import tornadofx.*

class MainView: View() {
    override var root = AnchorPane()
    val masterView: MasterView by inject()

    init {
        root = masterView.root
        title = "SaberH"
        // TODO: Use FXLauncher for building package: https://github.com/edvin/fxlauncher
    }
}

class MainApp: App(MainView::class)

fun main(args: Array<String>) {
    launch<MainApp>(args)
}