buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin"))
    }
}

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") apply false
    id("org.jlleitschuh.gradle.ktlint")
}

allprojects {
    group = "com.johannesdoll.koin"
}
