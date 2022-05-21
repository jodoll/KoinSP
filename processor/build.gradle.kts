import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

version = "0.1"

repositories {
    mavenCentral()
}

val koinVersion: String by project
val kotlinPoetVersion: String by project
val kotlinVersion: String by project
val kspVersion: String by project

dependencies {
    implementation(project(":api"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kotlinVersion-$kspVersion")
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("com.squareup:kotlinpoet-ksp:$kotlinPoetVersion")
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")

    testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile>().configureEach {
    @Suppress("SuspiciousCollectionReassignment")
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
