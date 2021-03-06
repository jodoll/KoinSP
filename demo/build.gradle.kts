import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("org.jlleitschuh.gradle.ktlint")
}

version = "0.1"

repositories {
    mavenCentral()
}

val kotlinVersion: String by project
val koTestVersion: String by project

dependencies {
    implementation(project(":api"))
    ksp(project(":processor"))

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:$koTestVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/ksp/main/kotlin"),
    )
}
