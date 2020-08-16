package ru.zeburek.saberh.utils

import ru.zeburek.saberh.MainApp
import java.io.IOException
import java.util.*


fun getVersion(): String {
    try {
        MainApp::class.java.getResourceAsStream("version.properties").use { stream ->
            val verProp = Properties()
            verProp.load(stream)
            val major: String = verProp.getProperty("VERSION_MAJOR")
            val minor: String = verProp.getProperty("VERSION_MINOR")
            val subminor: String = verProp.getProperty("VERSION_PATCH")
            return "$major.$minor.$subminor"
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return "1.0"
    }
}