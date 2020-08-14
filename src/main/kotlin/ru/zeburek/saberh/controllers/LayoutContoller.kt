package ru.zeburek.saberh.controllers

import mu.KotlinLogging
import ru.zeburek.saberh.views.JobsView
import ru.zeburek.saberh.views.MasterView
import tornadofx.*

private val logger = KotlinLogging.logger {}
class LayoutContoller: Controller() {
    val masterView: MasterView by inject()
    val jobsTable: JobsView by inject()
    var jobsVisible: Boolean by property(false)

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
}