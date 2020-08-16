package ru.zeburek.saberh.views

import javafx.scene.layout.BorderPane
import ru.zeburek.saberh.controllers.AdbController
import ru.zeburek.saberh.controllers.adbStartServer
import ru.zeburek.saberh.controllers.adbStopServer
import tornadofx.*

class MasterView: View() {
    var mainPane: BorderPane by singleAssign()

    val mainToolbar: MainToolbar by inject()
    val outputTabPane: OutputTabPane by inject()
    val bottomToolbar: BottomToolbar by inject()

    override val root = anchorpane {
        borderpane {
            mainPane = this
            prefHeight = 600.0
            prefWidth = 1280.0
            anchorpaneConstraints {
                leftAnchor = 0.0
                rightAnchor = 0.0
                bottomAnchor = 0.0
                topAnchor = 0.0
            }
            top = mainToolbar.root
            center = outputTabPane.root
            bottom = bottomToolbar.root
            // TODO: Take a look at drawer: https://edvin.gitbooks.io/tornadofx-guide/content/part1/7_Layouts_and_Menus.html
        }
    }
}