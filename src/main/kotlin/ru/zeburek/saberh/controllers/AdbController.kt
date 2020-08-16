package ru.zeburek.saberh.controllers

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.*
import mu.KotlinLogging
import ru.zeburek.saberh.controllers.LayoutContoller.Companion.tabContainer
import ru.zeburek.saberh.models.*
import ru.zeburek.saberh.utils.ActionObservableList
import ru.zeburek.saberh.utils.getParse
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
    val layoutContoller: LayoutContoller by inject()

    val jobs = JobsModel()

    // COMMANDS
    private fun preCommand(vararg params: String, useDevice: Boolean = true): List<String> =
        if (devicesController.currentDevice.isNotEmpty && useDevice) {
            listOf("adb", "-s", devicesController.currentDevice.id.value, *params)
        } else {
            listOf("adb", *params)
        }

    val cmdStartServer
        get() = preCommand("start-server", useDevice = false)
    val cmdKillServer
        get() = preCommand("kill-server", useDevice = false)
    val cmdDevices
        get() = preCommand("devices", useDevice = false)
    val cmdLogcat
        get() = preCommand("logcat")

    fun cmdPackages(listType: String): List<String> =
        preCommand("shell", "pm", "list", "packages", listType, "-i", "-U")
    // END

    fun adbLogcat() {
        val job = runPerLineAction(cmdLogcat)
        val parseRegex =
            """^(?<date>[\d-]+)\s+(?<time>[\d:.]+)\s+(?<pid>[\d]+)\s+[\d]+\s+(?<level>[\w])\s+(?<message>.*)${'$'}""".toRegex()
        val parse = getParse(parseRegex)
        outputTabPane.outputTabPane.apply {
            val newTab = tabContainer(job) {
                smartResize()
                readonlyColumn("#", OutputLine::lineNo)
                readonlyColumn("Date", OutputLine::string) { value { parse(it.value.string, "date", "") } }
                readonlyColumn("Time", OutputLine::string) { value { parse(it.value.string, "time", "") } }
                readonlyColumn("PID", OutputLine::string) { value { parse(it.value.string, "pid", "") } }
                readonlyColumn("Level", OutputLine::string) {
                    value {
                        parse(
                            it.value.string,
                            "level",
                            ""
                        )
                    }
                }.cellDecorator {
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
                readonlyColumn("Message", OutputLine::string) {
                    value {
                        parse(
                            it.value.string,
                            "message",
                            it.value.string
                        )
                    }
                }
            }
            newTab.select()
        }
    }

    fun loadDevices() {
        val parseRegex = """^(?<code>[0-9\w]+)[\s\t]+(?<status>\w+)${'$'}""".toRegex()
        val parse = getParse(parseRegex)
        runResultsAction(cmdDevices, listOf(fun(elements) {
            devicesController.devices.clear()
            elements.forEach { line ->
                val code = parse(line.string, "code", "")
                val status = DeviceStatus.valueOfStatus(parse(line.string, "status", ""))
                if (code.isNotBlank()) {
                    logger.info { "Found device: $code $status" }
                    if (status == DeviceStatus.DEVICE) {
                        getPropertyByName(code, "ro.product.manufacturer") { manuf ->
                            if (manuf.isNotBlank())
                                getPropertyByName(code, "ro.product.model") { model ->
                                    devicesController.devices.add(Device(code, "$manuf $model", status))
                                    if (devicesController.devices.isNotEmpty()) {
                                        layoutContoller.selectFirstInComboBox(mainToolbar.devicesComboBox)
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
                layoutContoller.selectFirstInComboBox(mainToolbar.packagesComboBox)
            }
        }))
    }

    fun runPerLineAction(command: List<String?>, onAddCommands: List<(OutputLine) -> OutputLine> = listOf()): JobInfo {
        return runCommand(command, onAddCommands as List<(Any) -> Any>, ActionObservableList::class.java)
    }

    fun runResultsAction(
        command: List<String?>,
        onAddCommands: List<(ObservableList<OutputLine>) -> Any?> = listOf()
    ): JobInfo {
        return runCommand(command, onAddCommands as List<(Any) -> Any>, ObservableList::class.java)
    }

    private fun getPropertyByName(deviceSerialNo: String, property: String, callback: (String) -> Unit = {}) {
        runResultsAction(listOf("adb", "-s", deviceSerialNo, "shell", "getprop", property), listOf(fun(elements) {
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
        val finishProgress = layoutContoller.setIndeterminateProgress(command.joinToString(" "))
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
                    finishProgress()
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