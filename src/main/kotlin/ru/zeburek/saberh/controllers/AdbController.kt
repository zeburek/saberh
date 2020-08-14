package ru.zeburek.saberh.controllers

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control.ComboBox
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableView
import kotlinx.coroutines.*
import mu.KotlinLogging
import ru.zeburek.saberh.models.Device
import ru.zeburek.saberh.models.DeviceStatus
import ru.zeburek.saberh.models.JobInfo
import ru.zeburek.saberh.models.JobsModel
import ru.zeburek.saberh.models.Package
import ru.zeburek.saberh.utils.ActionObservableList
import ru.zeburek.saberh.views.JobsView
import ru.zeburek.saberh.views.MainToolbar
import ru.zeburek.saberh.views.OutputTabPane
import tornadofx.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class OutputLine(lineNo: Int, string: String) {
    var lineNo by property(lineNo)
    var string by property(string)
}

private val logger = KotlinLogging.logger {}

class AdbController : Controller() {
    val jobsTable: JobsView by inject()
    val mainToolbar: MainToolbar by inject()
    val outputTabPane: OutputTabPane by inject()

    val devicesController: DevicesController by inject()

    val jobs = JobsModel()

    // COMMANDS
    val cmdDevices
        get() = listOf("adb", "devices")
    val cmdLogcat
        get() = if (devicesController.currentDevice.isNotEmpty) {
            listOf("adb", "-s", devicesController.currentDevice.id.value, "logcat")
        } else {
            listOf("adb", "logcat")
        }
    fun cmdPackages(listType: String): List<String> = if (devicesController.currentDevice.isNotEmpty) {
        listOf("adb", "-s", devicesController.currentDevice.id.value, "shell", "pm", "list", "packages", listType, "-i", "-U")
    } else {
        listOf("adb", "shell", "pm", "list", "packages", listType, "-i", "-U")
    }
    // END

    private fun getParse(regexp: Regex): (String, Any, String) -> String {
        return fun(line: String, field: Any, default: String): String {
            val parsed = regexp.find(line)
            if (parsed != null) {
                if (field is Int && parsed.groups[field] != null) {
                    return parsed.groups[field]!!.value
                } else if (field is String && parsed.groups[field] != null) {
                    return parsed.groups[field]!!.value
                }
            }
            return default
        }
    }

    private fun TabPane.tabContainer(job: JobInfo, tableViewData: TableView<OutputLine>.() -> Unit): Tab = tab(job.command) {
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

    private fun <T> selectFirstInComboBox(combobox: ComboBox<T>) {
        GlobalScope.launch(Dispatchers.Main) {
            combobox.selectionModel.selectFirst()
        }
    }

    fun adbLogcat() {
        val job = runPerLineAction(cmdLogcat)
        val parseRegex = """^(?<date>[\d-]+)\s+(?<time>[\d:.]+)\s+(?<pid>[\d]+)\s+[\d]+\s+(?<level>[\w])\s+(?<message>.*)${'$'}""".toRegex()
        val parse = getParse(parseRegex)
        outputTabPane.outputTabPane.apply {
            val newTab = tabContainer(job) {
                smartResize()
                readonlyColumn("#", OutputLine::lineNo)
                readonlyColumn("Date", OutputLine::string) { value { parse(it.value.string, "date", "") } }
                readonlyColumn("Time", OutputLine::string) { value { parse(it.value.string, "time", "") } }
                readonlyColumn("PID", OutputLine::string) { value { parse(it.value.string, "pid", "") } }
                readonlyColumn("Level", OutputLine::string) { value { parse(it.value.string, "level", "") } }.cellDecorator {
                    style {
                        backgroundColor += when (it) {
                            "D" -> c(0.0, 0.0, 0.0, 0.3)
                            "I" -> c(0.0, 0.0, 1.0, 0.3)
                            "W" -> c(0.0, 1.0, 1.0, 0.3)
                            "E" -> c(1.0, 0.0, 0.0, 0.3)
                            "F" -> c(1.0, 0.0, 0.0, 0.6)
                            else -> c(0.0, 0.0, 0.0, 0.0)
                        }
                    }
                }
                readonlyColumn("Message", OutputLine::string) { value { parse(it.value.string, "message", it.value.string) } }
            }
            newTab.select()
        }
    }

    fun loadDevices() {
        val parseRegex = """^(?<code>[0-9\w]+)[\s\t]+(?<status>\w+)${'$'}""".toRegex()
        val parse = getParse(parseRegex)
        runResultsAction(cmdDevices, listOf(fun(elements) {
            devicesController.devices.clear()
            elements.forEach {
                val code = parse(it.string, "code", "")
                val status = DeviceStatus.valueOfStatus(parse(it.string, "status", ""))
                if (code.isNotBlank()) {
                    logger.info { "Found device: $code $status" }
                    if (status == DeviceStatus.DEVICE) {
                        getPropertyByName(code, "ro.product.manufacturer") {
                            val manuf = it
                            if (it.isNotBlank())
                                getPropertyByName(code, "ro.product.model") {
                                    devicesController.devices.add(Device(code, "$manuf $it", status))
                                    if (devicesController.devices.isNotEmpty()) {
                                        selectFirstInComboBox(mainToolbar.devicesComboBox)
                                    }
                                }
                        }
                    } else {
                        devicesController.devices.add(Device(code, "UNKNOWN", status))
                    }
                }
            }
        }))
    }

    fun loadPackages(listType: String = "-3") {
        val parseRegex = """package:(?<package>\S+)\s+installer=(?<installer>\S+)\s+uid:(?<uid>\d+)""".toRegex()
        val parse = getParse(parseRegex)
        logger.info { "Load packages on device ${devicesController.currentDevice.id}" }
        runResultsAction(cmdPackages(listType), listOf(fun(elements) {
            devicesController.packages.clear()
            elements.forEach {
                val pkg = Package(
                        pkgName = parse(it.string, "package", ""),
                        installer = parse(it.string, "installer", ""),
                        uid = parse(it.string, "uid", "").toInt()
                )
                devicesController.packages.add(pkg)
            }
            devicesController.packages.sortBy { it.pkgName }
            if (devicesController.packages.isNotEmpty()) {
                selectFirstInComboBox(mainToolbar.packagesComboBox)
            }
        }))
    }

    fun adbDevices() {
        val job = runResultsAction(listOf("adb", "devices"))
        outputTabPane.outputTabPane.apply {
            val newTab = tabContainer(job) {
                smartResize()
                readonlyColumn("#", OutputLine::lineNo)
                readonlyColumn("Message", OutputLine::string)
            }
            newTab.select()
        }
    }

    fun adbShowPackages(listType: String = "-3") {
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

    fun runPerLineAction(command: List<String?>, onAddCommands: List<(OutputLine) -> OutputLine> = listOf()): JobInfo {
        return runCommand(command, onAddCommands as List<(Any) -> Any>, ActionObservableList::class.java)
    }

    fun runResultsAction(command: List<String?>, onAddCommands: List<(ObservableList<OutputLine>) -> Any?> = listOf()): JobInfo {
        return runCommand(command, onAddCommands as List<(Any) -> Any>, ObservableList::class.java)
    }

    private fun getPropertyByName(deviceSerialNo: String, property: String, callback: (String) -> Unit = {}) {
        val job = runResultsAction(listOf("adb", "-s", deviceSerialNo, "shell", "getprop", property), listOf(fun(elements) {
            val result = elements.joinToString { outputLine -> outputLine.string }
            logger.info { "Got property $property for device $deviceSerialNo: $result" }
            callback(result)
        }))
    }

    private fun <E> runCommand(
            command: List<String?>,
            onAddCommands: List<(Any) -> Any>,
            arrayType: Class<E>
    ): JobInfo {
        val list: ObservableList<OutputLine>
        if (arrayType == ActionObservableList::class) {
            list = ActionObservableList.create<OutputLine>()
            onAddCommands.forEach {
                list.onAdd(it as (OutputLine) -> OutputLine)
            }
        } else {
            list = FXCollections.observableArrayList<OutputLine>()
        }
        logger.info { "Process started: ${command.joinToString(" ")}" }
        val ps = ProcessBuilder(*command.toTypedArray())
        ps.redirectErrorStream(true)
        val pr = ps.start()

        val job = GlobalScope.launch(Dispatchers.IO) {
            try {
                val inputStream = BufferedReader(InputStreamReader(pr.inputStream!!))
                var line: String?
                var lineNo = 0
                while (inputStream.readLine().also { line = it } != null && isActive) {
                    lineNo += 1
                    if (line != null) {
                        list.add(OutputLine(lineNo, line!!))
                    }
                }
            } finally {
                withContext(NonCancellable) {
                    pr.destroyForcibly().waitFor()
                    logger.info { "Process exited: ${command.joinToString(" ")}, code ${pr.exitValue()}" }
                    logger.info { "Job $this is stopped/finished" }
                    if (arrayType != ActionObservableList::class) {
                        onAddCommands.forEach {
                            it(list)
                        }
                    }
                    jobsTable.jobsTable.refresh()
                }
            }
        }
        job.invokeOnCompletion {
            jobsTable.jobsTable.refresh()
        }
        val jobInfo = JobInfo(command.joinToString(" "), job, Date(), pr, list)
        jobs.jobs.value.add(jobInfo)
        return jobInfo
    }
}