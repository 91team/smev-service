import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    application
}

group = "team.91"
version = "0.0.1"

application {
    mainClass.set("MainKt")
}

//
//tasks.jar {
//    manifest {
//        attributes("Main-Class" to "MainKt")
//    }
////    from { configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }
//}


repositories {
    mavenCentral()
}

dependencies {
//    compile fileTree(dir: "lib", include: ["*.jar"])
    implementation(fileTree("lib"))
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
