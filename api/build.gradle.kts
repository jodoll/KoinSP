import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

version = "0.1"

repositories {
    mavenCentral()
}

val kotlinVersion : String by project
val koinVersion : String by project

dependencies {
    api("io.insert-koin:koin-core:$koinVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}