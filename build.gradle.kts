plugins {
    // Apply the Java Gradle Plugin Development plugin
    `java-gradle-plugin`

    // Apply the Kotlin DSL Plugin for enhanced IDE support
    `kotlin-dsl`

    // Apply the Plugin Publishing Plugin to publish plugins to the Gradle Plugins Portal
    id("com.gradle.plugin-publish") version "0.20.0"
}

group = "io.github.krakowski"
version = "0.3.1"

repositories {
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform(kotlin("bom")))

    // Use the Kotlin JDK 8 standard library
    implementation(kotlin("stdlib-jdk8"))

    // Gradle test kit using JUnit
    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

java {
    // explicitly target Java 8 (this is not updated when the kotlin jvmTarget is set)
    // this can be removed when https://youtrack.jetbrains.com/issue/KT-35003 is fixed
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    target {
        compilations.configureEach {
            kotlinOptions {
                // explicitly target Java 8
                jvmTarget = "1.8"
            }
        }
    }
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
    gradleVersion = "7.5.1"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}