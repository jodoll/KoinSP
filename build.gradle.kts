buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin"))
    }
}

plugins {
    kotlin("jvm") apply false
}

allprojects {
    group = "com.johannesdoll.koin"
}