import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("java")
    id("com.intershop.gradle.jaxb") version "5.1.0"
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

    api("org.glassfish.jaxb:jaxb-runtime:3.0.1")
    api("jakarta.activation:jakarta.activation-api:2.0.1")

    implementation("io.streamthoughts:kafka-clients-kotlin:0.2.0") {
        exclude("ch.qos.logback", "logback-classic")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

jaxb {
    javaGen {
        register("kafka") {
            schema = file("src/main/resources/schemas/schema.xsd")
            outputDir = file("src/main/java/")
            packageName = "com.nineone.smev.schemas"
            extension = true
        }
    }
}
