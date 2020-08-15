[![Build Status](https://github.com/zeburek/saberh/workflows/JavaFX%20package/badge.svg)](https://github.com/zeburek/saberh/actions)
![Cut up your Android](./docs/img/saberh.png)

# What is it?

It is a reincarnation of Zero Helper with brand new code (now it uses Kotlin + TornadoFX), but yet is not full-functional.

# How to install?

You can download installers for you platform (MacOS and Linux yet) from [Actions](https://github.com/zeburek/saberh/actions),
just open latest workflow and download yours artifact.

If you need it on Windows you could now do the following:
- Install WiX Toolset on Windows (https://github.com/wixtoolset/wix3/releases/tag/wix3112rtm)
- Clone this repo
- Run: `INSTALLER_TYPE=msi ./gradlew.bat -D"download.jpackage"=true jpackage`
and you will get bundled installer in `./build/jpackage` dir.

# Contribution

Feel free to contribute to the project, You Are Welcome.
Actual course is to implement:
- Video recording and screenshots
- Apps management
- Implement copying rows from table into normal Plain Text/Markdown
- Intent actions
- _Looking towards to integrate with Android Studio as a plugin_