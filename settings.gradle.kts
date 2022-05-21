pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    plugins {
        val kotlinVersion: String by settings
        val kspVersion: String by settings
        val ktLintVersion: String by settings

        kotlin("jvm") version kotlinVersion
        id("com.google.devtools.ksp") version "$kotlinVersion-$kspVersion"
        id("org.jlleitschuh.gradle.ktlint") version ktLintVersion
    }
}

rootProject.name = "KoinSP"

include(":api")
include(":demo")
include(":processor")
