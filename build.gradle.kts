import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    application
}

group = "com.nineone"
version = "0.0.1"

application {
    mainClass.set("com.nineone.smev.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    api(fileTree("libs") { include("*.jar") })
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
