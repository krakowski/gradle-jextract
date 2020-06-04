package io.github.krakowski.jextract


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaPlugin

class JextractPlugin implements Plugin<Project> {

    void apply(Project project) {

        // Create and register jextract task
        JextractTask jextractTask = project.tasks.create('jextract', JextractTask)

        // Configure Java plugin if it was applied
        project.plugins.withType(JavaPlugin) {

            // To make the generated classes available for our code, we
            // need to add the output directory to the list of source directories
            project.sourceSets {
                main {
                    java.srcDirs += jextractTask.outputDir
                }
            }

            // We need to add the foreign module, so the compiler sees its classes
            project.compileJava.options.compilerArgs += [ '--add-modules', 'jdk.incubator.foreign' ]

            project.dependencies {
                // This is necessary since jextract generates a
                // compiled class file containing constants
                implementation project.files(jextractTask.outputDir)
            }

            // The java compiler should only be invoked after
            // jextract generated its source files
            project.compileJava.dependsOn(jextractTask)
        }

        // Configure application plugin if it was applied
        project.plugins.withType(ApplicationPlugin) {
            // We need to add the foreign module, so that the classes are visible at runtime
            project.application.applicationDefaultJvmArgs += [
                    '-Dforeign.restricted=permit',
                    '--add-modules', 'jdk.incubator.foreign'
            ]
        }
    }
}
