package ru.zeburek.saberh.views

import javafx.geometry.NodeOrientation
import javafx.scene.control.ProgressBar
import javafx.scene.paint.Paint
import org.controlsfx.glyphfont.Glyph
import ru.zeburek.saberh.controllers.DevicesController
import ru.zeburek.saberh.controllers.LayoutContoller
import tornadofx.*

class BottomToolbar : View("My View") {
    val devicesController: DevicesController by inject()
    val layoutController: LayoutContoller by inject()
    var progressBar: ProgressBar by singleAssign()

    override val root = toolbar {
        nodeOrientation = NodeOrientation.RIGHT_TO_LEFT
        prefHeight = 20.0
        style {
            backgroundColor += Paint.valueOf("linear-gradient(#ddd,#ccc)")
        }
        button {
            prefHeight = 20.0
            fun setGraphic() {
                graphic = if (layoutController.jobsVisible) {
                    Glyph("FontAwesome", "ARROW_RIGHT")
                } else {
                    Glyph("FontAwesome", "ARROW_LEFT")
                }
            }
            action {
                layoutController.toggleJobs()
                setGraphic()
            }
            setGraphic()
        }
        label(stringBinding(devicesController.currentDevice.deviceName) {
            if (value == null) "No device selected" else "Device: $value"
        })
        progressbar {
            progressBar = this
            prefHeight = 10.0
            progress = 0.0
        }
    }
}
