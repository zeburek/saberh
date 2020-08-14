import java.lang.Boolean.getBoolean
import de.undercouch.gradle.tasks.download.DownloadAction
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.0-rc"
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("org.beryx.jlink") version "2.16.3"
    id("de.undercouch.download") version "4.0.0"
}

extra.apply {
    set("java11Args", listOf(
        "--add-opens=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED",
        // For various usages of TraversalEngine
        "--add-exports=javafx.graphics/com.sun.javafx.scene=org.controlsfx.controls",
        "--add-exports=javafx.graphics/com.sun.javafx.scene.traversal=org.controlsfx.controls",
        // For various behaviors across controls
        "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=org.controlsfx.controls",
        // For ReadOnlyUnbackedObservableList across files
        "--add-exports=javafx.controls/com.sun.javafx.scene.control=org.controlsfx.controls",
        // For InputMap used in behavior classes
        "--add-exports=javafx.controls/com.sun.javafx.scene.control.inputmap=org.controlsfx.controls",
        // For EventHandlerManager in AutoCompletionBinding
        "--add-exports=javafx.base/com.sun.javafx.event=org.controlsfx.controls",
        // For MappingChange, NonIterableChange across files
        "--add-exports=javafx.base/com.sun.javafx.collections=org.controlsfx.controls",
        // For VersionInfo in VersionChecker
        "--add-exports=javafx.base/com.sun.javafx.runtime=org.controlsfx.controls"
    ))
    set("java11RuntimeArgs", listOf(
        // For accessing VirtualFlow field from the base class in GridViewSkin
        "--add-opens=javafx.controls/javafx.scene.control.skin=org.controlsfx.controls",
        // For accessing getChildren in ImplUtils
        "--add-opens=javafx.graphics/javafx.scene=org.controlsfx.controls"
    ))
}

val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
compileJava.destinationDir = compileKotlin.destinationDir


group = "ru.zeburek"
version = "1.0.0"

application{
    mainClassName = "ru.zeburek.saberh.MainKt"
    applicationDefaultJvmArgs = listOf(
        "--add-opens=javafx.base/com.sun.javafx.runtime=org.controlsfx.controls",
        "--add-opens=javafx.base/com.sun.javafx.collections=org.controlsfx.controls",
        "--add-opens=javafx.graphics/com.sun.javafx.css=org.controlsfx.controls",
        "--add-opens=javafx.graphics/com.sun.javafx.scene=org.controlsfx.controls",
        "--add-opens=javafx.graphics/com.sun.javafx.scene.traversal=org.controlsfx.controls",
        "--add-opens=javafx.graphics/javafx.scene=org.controlsfx.controls",
        "--add-opens=javafx.controls/com.sun.javafx.scene.control=org.controlsfx.controls",
        "--add-opens=javafx.controls/com.sun.javafx.scene.control.behavior=org.controlsfx.controls",
        "--add-opens=javafx.controls/javafx.scene.control.skin=org.controlsfx.controls",
        "--add-opens=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED"
    )
}

repositories {
    mavenCentral()
}

javafx {
    version = "11.0.2"
    modules("javafx.controls", "javafx.graphics", "javafx.fxml")
    configuration = "compileOnly"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.3.8")
    implementation("no.tornado:tornadofx:1.7.20") {
        exclude("org.jetbrains.kotlin")
    }
    implementation("org.controlsfx:controlsfx:11.0.2")
    implementation("no.tornado:tornadofx-controlsfx:0.1")
    implementation("io.github.microutils:kotlin-logging:1.8.3")
    implementation("org.slf4j:slf4j-simple:1.7.29")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

val os = org.gradle.internal.os.OperatingSystem.current()

jlink {
    addExtraDependencies("javafx")
    addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    launcher {
        name = "saberh"
    }
//    mergedModule {
//        requires("org.slf4j")
//        requires("java.logging")
//    }
    jpackage {
        if (getBoolean("download.jpackage")) {
            jpackageHome = downloadJPackage()
        }
        skipInstaller = false
        
        installerOptions = listOf("--copyright", "Copyrigth 2016-2020 Parviz Khavari")
        installerType = System.getenv("INSTALLER_TYPE") // we will pass this from the command line (example: -PinstallerType=msi)
        if (installerType == "msi") {
//            imageOptions.plusAssign(listOf("--icon", "src/main/resources/ru/zeburek/zerohelper/icon.ico"))
            installerOptions.plusAssign(
                listOf(
                    "--win-per-user-install", "--win-dir-chooser",
                    "--win-menu", "--win-shortcut"
                )
            )
        }
        if (installerType == "pkg") {
//            imageOptions.plusAssign(listOf("--icon", "src/main/resources/ru/zeburek/zerohelper/icon.icns"))
        }
        if (installerType in listOf("deb", "rpm")) {
//            imageOptions.plusAssign(listOf("--icon", "src/main/resources/ru/zeburek/zerohelper/icon.png"))
            installerOptions.plusAssign(
                listOf(
                    "--linux-menu-group", "Tools",
                    "--linux-shortcut"
                )
            )
        }
        if (installerType == "deb") {
            installerOptions.plusAssign(listOf("--linux-deb-maintainer", "me@parviz.pw"))
        }
        if (installerType == "rpm") {
            installerOptions.plusAssign(listOf("--linux-rpm-license-type", "ASL 2.0"))
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

