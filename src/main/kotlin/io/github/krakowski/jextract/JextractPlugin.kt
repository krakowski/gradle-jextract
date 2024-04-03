package io.github.krakowski.jextract

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.internal.jvm.Jvm
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.*

class JextractPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        // Create and register jextract task
        val jextractTask = target.tasks.create<JextractTask>("jextract")

        // Use current JVM as default toolchain for jextract task
        jextractTask.toolchain.convention(Jvm.current().javaHome.absolutePath.toString())

        // Configure Java plugin if it was applied
        target.plugins.withType<JavaPlugin> {

            // Query java plugin extensions
            val extension = target.extensions.getByType<JavaPluginExtension>()
            val service = target.extensions.getByType<JavaToolchainService>()

            // Wire up the selected toolchain with the jextract task or fall back to
            // the current JVM if no toolchain has been specified
            jextractTask.toolchain.convention(
                    service.compilerFor(extension.toolchain)
                           .map { it.metadata.installationPath.asFile.absolutePath.toString() }
                           .orElse(Jvm.current().javaHome.absolutePath.toString())
            )

            // To make the generated classes available for our code,
            // we need to add the output directory to the list of source directories
            extension.sourceSets {
                named("main") {

                    // Add generated sources to source set
                    java.srcDir(jextractTask)

                    // This is necessary since jextract generates a compiled class file containing constants
                    compileClasspath += target.files(jextractTask)
                    runtimeClasspath += target.files(jextractTask)
                }
            }
        }
    }
}
