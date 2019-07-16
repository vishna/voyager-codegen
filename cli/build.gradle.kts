import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript {
    dependencies {
        classpath ("com.github.jengelman.gradle.plugins:shadow:5.1.0")
    }
}

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

kotlinProject()

application {
    mainClassName = "cli.Main"
}

tasks.withType<ShadowJar> {
    baseName = "voyager-codegen"
    archiveName = "voyager-codegen.jar"
    version = version
}

dependencies {
    compile(project(":voyager-codegen"))
    compile("com.github.vishna:patrol:master-SNAPSHOT")
}
