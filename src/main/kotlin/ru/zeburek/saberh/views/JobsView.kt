package ru.zeburek.saberh.views

import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.controlsfx.glyphfont.Glyph
import ru.zeburek.saberh.controllers.AdbController
import ru.zeburek.saberh.models.JobInfo
import tornadofx.*
import java.text.SimpleDateFormat

private val logger = KotlinLogging.logger {}
class JobsView : View("My View") {
    val adbController: AdbController by inject()
    var jobsTable: TableView<JobInfo> by singleAssign()

    override val root = anchorpane {
        prefWidth = 300.0
        scrollpane {
            isFitToHeight = true
            isFitToWidth = true
            anchorpaneConstraints {
                topAnchor = 0.0
                bottomAnchor = 0.0
                leftAnchor = 0.0
                rightAnchor = 0.0
            }

            tableview<JobInfo> {
                jobsTable = this
                isEditable = false
                smartResize()
                readonlyColumn("Started at", JobInfo::createdAt) {
                    value {
                        SimpleDateFormat("H:m:s.S y-M-d").format(it.value.createdAt)
                    }
                }
                readonlyColumn("Command", JobInfo::command)
                readonlyColumn("", JobInfo::job) {
                    prefWidth = 25.0
                }.cellFormat {
                    if (this@cellFormat.tableRow != null && this@cellFormat.tableRow.item != null) {
                        graphic = if (this.tableRow.item.job.isActive) Glyph("FontAwesome", "SPINNER") else Glyph("FontAwesome", "BAN")
                    }
                }
                readonlyColumn("Stop", JobInfo::job).cellFormat {
                    graphic = button(graphic = Glyph("FontAwesome", "STOP")) {
                        padding = insets(0.0)
                        this.isDisable = true
                        action {
                            val job = this@cellFormat.tableRow.item.job
                            logger.info { "Stopping coroutine $job" }
                            job.cancel("Job stopped")
                            logger.info { "Stopped coroutine $job ${job.isCancelled}" }
                            this.isDisable = true
                        }
                        if (this@cellFormat.tableRow != null && this@cellFormat.tableRow.item != null) {
                            val job = this@cellFormat.tableRow.item.job
                            this.isDisable = !job.isActive
                        }
                    }
                }
                setOnKeyPressed {
                    event -> if (event.code == KeyCode.ESCAPE) this.selectionModel.clearSelection()
                }
                itemsProperty().bind(adbController.jobs.jobs)
            }
        }
    }
}
