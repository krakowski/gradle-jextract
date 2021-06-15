plugins {
    // Apply the Java Gradle Plugin Development plugin
    `java-gradle-plugin`

    // Apply the Kotlin DSL Plugin for enhanced IDE support
    `kotlin-dsl`

    // Apply the Kotlin JVM plugin to add support for Kotlin
    id("org.jetbrains.kotlin.jvm") version "1.4.31"

    // Apply the Plugin Publishing Plugin to publish plugins to the Gradle Plugins Portal
    id("com.gradle.plugin-publish") version "0.15.0"
}

group = "io.github.krakowski"
version = "0.2.0"

repositories {
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform(kotlin("bom")))

    // Use the Kotlin JDK 8 standard library
    implementation(kotlin("stdlib-jdk8"))
}

gradlePlugin {
    plugins {
        create("jextract") {
            id = "io.github.krakowski.jextract"
            displayName = "jextract gradle plugin"
            description = "Integrates jextract with the Gradle build system"
            implementationClass = "io.github.krakowski.jextract.JextractPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/krakowski/gradle-jextract"
    vcsUrl = "https://github.com/krakowski/gradle-jextract.git"
    tags = listOf("native", "panama", "jextract")
}

tasks.withType<Wrapper> {
    gradleVersion = "7.1"
}