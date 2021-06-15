package io.github.krakowski.jextract

import org.gradle.api.*
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.*
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
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

            // Query configured toolchain
            val service = target.extensions.getByType<JavaToolchainService>()
            val tool = target.extensions.getByType<JavaPluginExtension>().toolchain

            // Wire up the selected toolchain with the jextract task or fall back to
            // the current JVM if no toolchain has been specified
            jextractTask.toolchain.convention(
                    service.compilerFor(tool)
                           .map { it.metadata.installationPath.asFile.absolutePath.toString() }
                           .orElse(Jvm.current().javaHome.absolutePath.toString())
            )

            // To make the generated classes available for our code,
            // we need to add the output directory to the list of source directories
            target.sourceSets {
                main {
                    java.srcDirs += jextractTask.outputDir.asFile.get()

                    // This is necessary since jextract generates a compiled class file containing constants
                    compileClasspath += target.files(jextractTask.outputDir)
                    runtimeClasspath += target.files(jextractTask.outputDir)
                }
            }

            // This is necessary in case we use class file mode
            target.dependencies {
                implementation(target.files(jextractTask.outputDir))
            }

            // Include all generated classes inside our jar archive
            target.withType<Jar> {
                from(jextractTask.outputDir) {
                    include("**/*.class")
                }
            }

            // We need to add the foreign module, so the compiler sees its classes and
            // the java compiler should only be invoked after jextract generated its source files
            target.withType<JavaCompile> {
                dependsOn(jextractTask)
                options.compilerArgs.add("--add-modules")
                options.compilerArgs.add("jdk.incubator.foreign")
            }

            target.withType<Test> {
                jvmArgs.add("--enable-native-access=ALL-UNNAMED")
                jvmArgs.add("--add-modules")
                jvmArgs.add("jdk.incubator.foreign")
            }
        }

        // Configure application plugin if it was applied
        target.plugins.withType<ApplicationPlugin> {
            // We need to add the foreign module, so that the classes are visible at runtime
            target.application.applicationDefaultJvmArgs = target.application.applicationDefaultJvmArgs +
                    "--enable-native-access=ALL-UNNAMED" + "--add-modules" + "jdk.incubator.foreign"
        }
    }
}

// dsl helpers, we don't have the nice dsl kotlin available here
fun Project.sourceSets(configure: Action<SourceSetContainer>) = (this as ExtensionAware).extensions.configure("sourceSets", configure)
val SourceSetContainer.main: NamedDomainObjectProvider<SourceSet> get() = named<SourceSet>("main")
operator fun <T> NamedDomainObjectProvider<T>.invoke(action: T.() -> Unit) = configure(action)
fun DependencyHandler.implementation(dependencyNotation: Any) = add("implementation", dependencyNotation)
inline fun <reified S : Task> Project.withType(noinline configuration: S.() -> Unit): DomainObjectCollection<in S> = tasks.withType(S::class.java, configuration)
val Project.application: JavaApplication get() = (this as ExtensionAware).extensions.getByName<JavaApplication>("application")

