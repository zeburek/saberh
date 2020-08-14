package ru.zeburek.saberh.models

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.Job
import ru.zeburek.saberh.controllers.OutputLine
import tornadofx.*
import java.util.*


class JobInfo(command: String, job: Job, createdAt: Date, process: Process, outputList: ObservableList<OutputLine>) {
    var command by property(command)
    var job by property(job)
    var process by property(process)
    var createdAt by property(createdAt)
    var outputList by property(outputList)
}

class Jobs() {
    var jobs by property(FXCollections.observableArrayList<JobInfo>())
    fun jobsProperty() = getProperty(Jobs::jobs)
}

class JobsModel : ItemViewModel<Jobs>() {
    val jobs = bind { item?.jobsProperty() }
}