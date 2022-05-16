pluginManagement {
    plugins {
        val kotlinVersion : String by settings

        kotlin("jvm") version kotlinVersion
    }
}

rootProject.name = "KoinSP"

include(":processor")