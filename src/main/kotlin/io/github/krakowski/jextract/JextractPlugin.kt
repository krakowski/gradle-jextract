package io.github.krakowski.jextract

//import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.*
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import java.io.File

class JextractPlugin : Plugin<Project> {

    companion object {
        private val PROPERTY_JAVA_HOME = "javaHome"
    }

    override fun apply(target: Project) {

        // Create and register jextract task
        val jextractTask = target.tasks.create<JextractTask>("jextract")

        if (target.hasProperty(PROPERTY_JAVA_HOME))
            jextractTask.javaHome.set(target.property(PROPERTY_JAVA_HOME) as String)

        // Configure Java plugin if it was applied
        target.plugins.withType<JavaPlugin> {

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
            target.dependencies.implementation(target.files(jextractTask.outputDir))

            // Include all generated classes inside our jar archive
            target.jar {
                from(jextractTask.outputDir) {
                    include("**/*.class")
                }
            }

            // We need to add the foreign module, so the compiler sees its classes
            target.withType<JavaCompile> {
                options.compilerArgs.add("--add-modules")
                options.compilerArgs.add("jdk.incubator.foreign")
            }
            target.test {
                jvmArgs.add("--enable-native-access=ALL-UNNAMED")
                jvmArgs.add("--add-modules")
                jvmArgs.add("jdk.incubator.foreign")
            }

            // The java compiler should only be invoked after jextract generated its source files
            target.compileJava { dependsOn(jextractTask) }


            // Set custom java home for compiling sources in case Gradle is not compatible with the current JDK.
            // https://docs.gradle.org/current/userguide/building_java_projects.html#example_configure_java_7_build
            if (target.hasProperty(PROPERTY_JAVA_HOME)) {

                val javaExecutablesPath = File(jextractTask.javaHome.get(), "bin")
                fun javaExecutables(execName: String) = File(javaExecutablesPath, execName).also {
                    assert(it.exists()) { "There is no $execName executable in $javaExecutablesPath" }
                }

                // Set java home path
                target.tasks.withType<JavaCompile> {
                    options.apply {
                        isFork = true
                        forkOptions.javaHome = target.file(jextractTask.javaHome.get())
                    }
                }

                // Set javadoc executable
                target.tasks.withType<Javadoc> {
                    executable = javaExecutables("javadoc").toString()
                }

                // Set java executable for tests
                target.tasks.withType<Test> {
                    executable(javaExecutables("java"))
                }

                // Set java executable for execution
                target.tasks.withType<JavaExec> {
                    executable(javaExecutables("java"))
                }
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
val Project.jar get() = tasks.named<Jar>("jar")
val Project.compileJava: TaskProvider<JavaCompile> get() = tasks.named<JavaCompile>("compileJava")
inline fun <reified S : Task> Project.withType(noinline configuration: S.() -> Unit): DomainObjectCollection<in S> = tasks.withType(S::class.java, configuration)
val Project.test: TaskProvider<Test> get() = tasks.named<Test>("test")
val Project.application: JavaApplication get() = (this as ExtensionAware).extensions.getByName<JavaApplication>("application")

