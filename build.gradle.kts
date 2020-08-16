import java.lang.Boolean.getBoolean
import de.undercouch.gradle.tasks.download.DownloadAction
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    kotlin("jvm") version "1.4.0-rc"
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("org.beryx.jlink") version "2.16.3"
    id("de.undercouch.download") version "4.0.0"
}

repositories {
    jcenter()
    mavenCentral()
}

val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
compileJava.destinationDir = compileKotlin.destinationDir


group = "ru.zeburek"
version = "1.0.0"

application{
    mainClassName = "ru.zeburek.saberh.MainKt"
    applicationDefaultJvmArgs = moduleAdditionalParams()
}

repositories {
    mavenCentral()
}

val jfxVersion = "11.0.2"

javafx {
    version = jfxVersion
    modules("javafx.controls", "javafx.graphics", "javafx.fxml")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.3.8") {
        exclude("org.openjfx")
    }
    implementation("no.tornado:tornadofx:1.7.20") {
        exclude("org.jetbrains.kotlin")
    }
    implementation("org.controlsfx:controlsfx:11.0.2") {
        exclude("org.openjfx")
    }
    implementation("no.tornado:tornadofx-controlsfx:0.1") {
        exclude("org.openjfx")
    }
    implementation("io.github.microutils:kotlin-logging:1.8.3")
    implementation("org.slf4j:slf4j-simple:1.7.29")
}

tasks {
    withType<KotlinCompile> {
        doFirst {
            val versionPropsFile = file("$buildDir/../src/main/resources/ru/zeburek/saberh/version.properties")

            if (versionPropsFile.canRead()) {
                logger.info("Version file: $versionPropsFile")
                val versionProps = Properties()

                versionProps.load(versionPropsFile.inputStream())

                val major = versionProps["VERSION_MAJOR"]
                val minor = versionProps["VERSION_MINOR"]
                val code = versionProps["VERSION_PATCH"].toString().toInt() + 1

                versionProps["VERSION_PATCH"] = code.toString()
                versionProps.store(versionPropsFile.writer(), null)

                val newVer = "$major.$minor.$code"
                logger.info("New version: $newVer")
                project.version = newVer
            }
        }
        kotlinOptions.jvmTarget = "11"
    }
}

val os = org.gradle.internal.os.OperatingSystem.current()

jlink {
    addExtraDependencies("javafx")
    addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    launcher {
        name = "SaberH"
        jvmArgs = moduleAdditionalParams("ru.zeburek.merged.module")
    }
    jpackage {
        if (getBoolean("download.jpackage")) {
            jpackageHome = downloadJPackage()
        }
        skipInstaller = false
        
        installerOptions = arrayListOf("--copyright", "Copyrigth 2016-2020 Parviz Khavari")
        installerType = System.getenv("INSTALLER_TYPE") // we will pass this from the command line (example: -PinstallerType=msi)
        if (installerType == "msi") {
            imageOptions.addAll(listOf("--icon", "src/main/resources/ru/zeburek/saberh/icon.ico"))
            installerOptions.addAll(
                listOf(
                    "--win-per-user-install", "--win-dir-chooser",
                    "--win-menu", "--win-shortcut"
                )
            )
        }
        if (installerType == "pkg") {
            imageOptions.addAll(listOf("--icon", "src/main/resources/ru/zeburek/saberh/icon.icns"))
        }
        if (installerType in listOf("deb", "rpm")) {
            imageOptions.addAll(listOf("--icon", "src/main/resources/ru/zeburek/saberh/icon.png"))
            installerOptions.addAll(
                listOf(
                    "--linux-menu-group", "Tools",
                    "--linux-shortcut"
                )
            )
        }
        if (installerType == "deb") {
            installerOptions.addAll(listOf("--linux-deb-maintainer", "me@parviz.pw"))
        }
        if (installerType == "rpm") {
            installerOptions.addAll(listOf("--linux-rpm-license-type", "ASL 2.0"))
        }
    }
}

// #### The code below is needed only if you use the downloadJPackage() method to install the jpackage tool ####
// Code copied from build.gradle in https://github.com/beryx/fxgl-sliding-puzzle/

/** @return  [url, extension, directory] */
fun getJPackageCoordinates(): List<String> {
    val jpackageBaseUrl = "https://s3-us-west-2.amazonaws.com/static.msgilligan.com/jpackage"
    val jpackageVersionString = "openjdk-14-jpackage%2B1-49"
    
    if (os.isMacOsX)
        return listOf(
            "${jpackageBaseUrl}/${jpackageVersionString}_osx-x64_bin.tar.gz",
            "tar.gz",
            "jdk-14.jdk/Contents/Home"
        )
    if(os.isWindows)
        return listOf(
            "${jpackageBaseUrl}/${jpackageVersionString}_windows-x64_bin.zip",
            "zip",
            "jdk-14"
        )
    return listOf(
        "${jpackageBaseUrl}/${jpackageVersionString}_linux-x64_bin.tar.gz",
        "tar.gz",
        "jdk-14"
    )
}

fun downloadJPackage(): String {
    val (url, extension, directory) = getJPackageCoordinates()
    val downloadDir = "$buildDir/download"
    tasks.jpackageImage.configure {
        doFirst {
            val execExt = if (os.isWindows) ".exe" else ""
            if(!file("$downloadDir/$directory/bin/jpackage$execExt").isFile) {
                val jdkArchivePath = "$downloadDir/jdk-jpackage.$extension"
                download.configure(
                    delegateClosureOf<DownloadAction> {
                        src(url)
                        dest(jdkArchivePath)
                        overwrite(false)
                    }
                )
                copy {
                    from(if (extension == "tar.gz") tarTree(resources.gzip(jdkArchivePath)) else zipTree(jdkArchivePath))
                    into(downloadDir)
                }
            }
        }
    }
    return "$downloadDir/$directory"
}

fun moduleAdditionalParams(onePackage: String? = null): List<String> = listOf(
    "--add-opens=javafx.base/com.sun.javafx.runtime=${onePackage ?: "org.controlsfx.controls"}",
    "--add-opens=javafx.base/com.sun.javafx.collections=${onePackage ?: "org.controlsfx.controls"}",
    "--add-opens=javafx.graphics/com.sun.javafx.css=${onePackage ?: "org.controlsfx.controls"}",
    "--add-opens=javafx.graphics/com.sun.javafx.scene=${onePackage ?: "org.controlsfx.controls"}",
    "--add-opens=javafx.graphics/com.sun.javafx.scene.traversal=${onePackage ?: "org.controlsfx.controls"}",
    "--add-opens=javafx.graphics/javafx.scene=${onePackage ?: "org.controlsfx.controls"}",
    "--add-opens=javafx.controls/com.sun.javafx.scene.control=${onePackage ?: "org.controlsfx.controls"}",
    "--add-opens=javafx.controls/com.sun.javafx.scene.control.behavior=${onePackage ?: "org.controlsfx.controls"}",
    "--add-opens=javafx.controls/javafx.scene.control.skin=${onePackage ?: "org.controlsfx.controls"}",
    "--add-opens=javafx.graphics/com.sun.javafx.css=${onePackage ?: "ALL-UNNAMED"}",
    "--add-opens=javafx.controls/javafx.scene=${onePackage ?: "tornadofx"}",
    "--add-opens=javafx.graphics/javafx.scene=${onePackage ?: "tornadofx"}",
    "--add-opens=javafx.controls/javafx.scene.control=${onePackage ?: "tornadofx"}"
)

