package io.github.krakowski.jextract

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test

class JextractPlugin implements Plugin<Project> {

    private static final String PROPERTY_JAVA_HOME = 'javaHome'

    void apply(Project project) {

        // Create and register jextract task
        JextractTask jextractTask = project.tasks.create('jextract', JextractTask)
        if (project.hasProperty(PROPERTY_JAVA_HOME)) {
            jextractTask.javaHome = project.property(PROPERTY_JAVA_HOME)
        }

        // Configure Java plugin if it was applied
        project.plugins.withType(JavaPlugin) {

            // To make the generated classes available for our code, we
            // need to add the output directory to the list of source directories
            project.sourceSets {
                main {
                    java.srcDirs += jextractTask.outputDir

                    // This is necessary since jextract generates a
                    // compiled class file containing constants
                    compileClasspath += project.files(jextractTask.outputDir)
                    runtimeClasspath += project.files(jextractTask.outputDir)
                }
            }

            // This is necessary in case we use class file mode
            project.dependencies {
                implementation project.files(jextractTask.outputDir)
            }

            // Include all generated classes inside our jar archive
            project.jar {
                from(jextractTask.outputDir) {
                    include '**/*.class'
                }
            }

            // We need to add the foreign module, so the compiler sees its classes
            project.compileJava.options.compilerArgs += [ '--add-modules', 'jdk.incubator.foreign' ]

            // The java compiler should only be invoked after
            // jextract generated its source files
            project.compileJava.dependsOn(jextractTask)

            // Set custom java home for compiling sources in case Gradle
            // is not compatible with the current JDK.
            // https://docs.gradle.org/current/userguide/building_java_projects.html#example_configure_java_7_build
            if (project.hasProperty(PROPERTY_JAVA_HOME)) {

                def javaExecutablesPath = new File(jextractTask.javaHome.get(), 'bin')
                def javaExecutables = [:].withDefault { execName ->
                    def executable = new File(javaExecutablesPath, execName)
                    assert executable.exists(): "There is no ${execName} executable in ${javaExecutablesPath}"
                    executable
                }

                // Set java home path
                project.tasks.withType(AbstractCompile) {
                    options.with {
                        fork = true
                        forkOptions.javaHome = project.file(jextractTask.javaHome.get())
                    }
                }

                // Set javadoc executable
                project.tasks.withType(Javadoc) {
                    executable = javaExecutables.javadoc
                }

                // Set java executable for tests
                project.tasks.withType(Test) {
                    executable = javaExecutables.java
                }

                // Set java executable for execution
                project.tasks.withType(JavaExec) {
                    executable = javaExecutables.java
                }
            }
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
