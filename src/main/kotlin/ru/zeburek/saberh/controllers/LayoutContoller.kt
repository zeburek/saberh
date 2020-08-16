package ru.zeburek.saberh.controllers

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.scene.control.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mu.KotlinLogging
import ru.zeburek.saberh.models.JobInfo
import ru.zeburek.saberh.views.BottomToolbar
import ru.zeburek.saberh.views.JobsView
import ru.zeburek.saberh.views.MasterView
import tornadofx.*

private val logger = KotlinLogging.logger {}
class LayoutContoller: Controller() {
    val masterView: MasterView by inject()
    val bottomToolbar: BottomToolbar by inject()
    val jobsTable: JobsView by inject()
    var jobsVisible: Boolean by property(false)

    var progressStatusProperty = SimpleDoubleProperty(this, "progressStatus", 0.0)
    var progressStatus by progressStatusProperty
    var progressTextProperty = SimpleStringProperty(this, "progressText", "")
    var progressText by progressTextProperty

    fun toggleJobs() {
        logger.info { "Jobs triggered: $jobsVisible" }
        jobsVisible = !jobsVisible
        setJobsState(jobsVisible)
    }

    fun setJobsState(visible: Boolean) {
        if (visible) {
            masterView.mainPane.right = jobsTable.root
        } else {
            masterView.mainPane.right = null
        }
    }

    fun setIndeterminateProgress(text: String): () -> Unit {
        GlobalScope.launch(Dispatchers.Main) {
            progressText = "Running: $text"
            progressStatus = ProgressBar.INDETERMINATE_PROGRESS
        }
        return {
            setFinishProgress("Finished: $text")
        }
    }

    fun setFinishProgress(text: String) {
        GlobalScope.launch(Dispatchers.Main) {
            progressText = text
            progressStatus = 0.0
            launch(Dispatchers.IO) {
                Thread.sleep(3000)
                launch(Dispatchers.Main) {
                    progressText = ""
                }
            }
        }
    }

    fun <T> selectFirstInComboBox(combobox: ComboBox<T>) {
        GlobalScope.launch(Dispatchers.Main) {
            combobox.selectionModel.selectFirst()
        }
    }

    companion object {
        fun TabPane.tabContainer(job: JobInfo, tableViewData: TableView<OutputLine>.() -> Unit): Tab =
            tab(job.command) {
                anchorpane {
                    scrollpane {
                        isFitToHeight = true
                        isFitToWidth = true
                        anchorpaneConstraints {
                            topAnchor = 0.0
                            bottomAnchor = 0.0
                            leftAnchor = 0.0
                            rightAnchor = 0.0
                        }

                        tableview(job.outputList) {
                            smartResize()
                            tableViewData()
                        }
                    }
                }
                onCloseRequest = EventHandler {
                    job.job.cancel("Tab closed")
                }
            }
    }
}