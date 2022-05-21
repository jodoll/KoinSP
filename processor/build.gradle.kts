import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

version = "0.1"

repositories {
    mavenCentral()
}

val kotlinVersion: String by project
val kspVersion: String by project
val koinVersion: String by project

dependencies {
    implementation(project(":api"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kotlinVersion-$kspVersion")
    implementation("io.insert-koin:koin-core:$koinVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
