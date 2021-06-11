plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.4.31"

    `kotlin-dsl`

    id("com.gradle.plugin-publish") version "0.15.0"
}

group = "io.github.krakowski"
version = "0.1.7"

repositories {
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform(kotlin("bom")))

    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))

//    // Use the Kotlin test library.
//    testImplementation(kotlin("test"))
//
//    // Use the Kotlin JUnit integration.
//    testImplementation(kotlin("test-junit"))
}

gradlePlugin.plugins.create("greeting") {
    id = "io.github.krakowski.jextract"
    implementationClass = "io.github.krakowski.jextract.JextractPlugin"
    displayName = "jextract gradle plugin"
}

pluginBundle {
    website = "https://github.com/krakowski/gradle-jextract"
    vcsUrl = "https://github.com/krakowski/gradle-jextract.git"
    description = "Integrates jextract with the Gradle build system"
    tags = listOf("native", "panama", "jextract")
}