package io.github.krakowski.jextract

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList

abstract class JextractTask : DefaultTask() {

    @get:Input
    val toolchain: Property<String> = project.objects.property()

    /** Arguments which should be passed to clang. */
    @get:Optional @get:Input
    val clangArguments: Property<String> = project.objects.property()

    /** Whether to generate sources or precompiled class files */
    @get:Input
    val sourceMode: Property<Boolean> = project.objects.property<Boolean>()
            .convention(true)

    /** The output directory in which the generated code will be placed. */
    @get:OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()
            .convention(project.layout.buildDirectory.dir("generated/sources/jextract/main/java"))

    @get:Nested
    val definitions = ArrayList<LibraryDefinition>()

    init {
        group = "build"
    }

    private fun findExecutable(): Path {

        // Select appropriate executable for operating system
        val operatingSystem = OperatingSystem.current()
        val executable = if (operatingSystem.isWindows) WINDOWS_EXECUTABLE else UNIX_EXECUTABLE

        // Try bundled jextract binary first to ensure compatibility with the currently used JDK
        val bundledExecutable = Paths.get(toolchain.get(), "bin", executable)
        if (Files.exists(bundledExecutable)) {
            return bundledExecutable
        }

        // Search for jextract in PATH if JDK has no bundled binary
        val envPath = System.getenv(ENV_PATH)
        val pathExecutable = envPath
                .split(File.pathSeparator)
                .map { path -> Paths.get(path, executable) }
                .filter { path -> Files.exists(path) }

        try {
            return pathExecutable.first()
        } catch (exception: NoSuchElementException) {
            throw GradleException("jextract binary could not be found in PATH or at ${bundledExecutable}\n\t↳ PATH=${envPath}")
        }
    }

    @TaskAction
    fun action() {

        val jextractBinary = findExecutable()
        if (Files.isDirectory(jextractBinary)) {
            throw GradleException("${jextractBinary} is not a regular file but a directory")
        }

        if (!Files.isExecutable(jextractBinary)) {
            throw GradleException("${jextractBinary} is not executable")
        }

        for (definition in definitions) {

            // Initialize argument list
            val arguments = ArrayList<String>()

            // Add jextract binary as first argument
            arguments += jextractBinary.toString()

            // Add source mode flag if it was enabled by the user
            if (sourceMode.get()) {
                arguments += "--source"
            }

            // Add clang arguments if they are present
            clangArguments.orNull?.let {
                arguments += "-C"
                arguments += it
            }

            // Include specified functions
            definition.functions.orNull?.forEach {
                arguments += "--include-function"
                arguments += it
            }

            // Include specified macros
            definition.macros.orNull?.forEach {
                arguments += "--include-macro"
                arguments += it
            }

            // Include specified structs
            definition.structs.orNull?.forEach {
                arguments += "--include-struct"
                arguments += it
            }

            // Include specified typedefs
            definition.typedefs.orNull?.forEach {
                arguments += "--include-typedef"
                arguments += it
            }

            // Include specified functions
            definition.unions.orNull?.forEach {
                arguments += "--include-union"
                arguments += it
            }

            // Include specified functions
            definition.variables.orNull?.forEach {
                arguments += "--include-var"
                arguments += it
            }

            // Include specified preprocessor macros
            definition.definedMacros.orNull?.forEach {
                arguments += "-D"
                arguments += it
            }

            // Add include paths if they are present
            definition.includes.orNull?.forEach {
                arguments += "-I"
                arguments += it
            }

            // Add library names if they are present
            definition.libraries.orNull?.let {
                for (library in it) {
                    arguments += "-l"
                    arguments += library
                }
            }

            // Add target package if it is present
            definition.targetPackage.orNull?.let {
                arguments += "--target-package"
                arguments += it
            }

            definition.className.orNull?.let {
                arguments += "--header-class-name"
                arguments += it
            }

            // Set output directory
            arguments += "--output"
            arguments += outputDir.get().toString()

            // Set header file
            arguments += definition.header.get()

            // Execute command
            execute(arguments.toTypedArray())
        }
    }

    fun header(header: String, action: Action<LibraryDefinition>) {
        val definition = project.objects.newInstance<LibraryDefinition>()
        definition.header.set(header)
        action.execute(definition)
        definitions += definition
    }

    private companion object {
        const val ENV_PATH = "PATH"
        const val UNIX_EXECUTABLE = "jextract"
        const val WINDOWS_EXECUTABLE = "jextract.bat"

        private fun execute(command: Array<String>) {
            // Create buffers for stdout and stderr streams
            val stdout = StringBuffer()
            val stderr = StringBuffer()
            val result = Runtime.getRuntime().exec(command)

            // Wait until the process finishes and check if it succeeded
            result.await(stdout, stderr)
            if (result.exitValue() != 0) {
                throw GradleException("Invoking jextract failed.\n\n" +
                        " command: ${command}\n stdout: ${stdout}\n stderr: ${stderr}")
            }
        }

        fun Process.await(output: Appendable?, error: Appendable?) {
            val out = ProcessGroovyMethods.consumeProcessOutputStream(this, output)
            val err = ProcessGroovyMethods.consumeProcessErrorStream(this, error)
            try {
                try {
                    out.join()
                    err.join()
                    waitFor()
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            } finally {
                ProcessGroovyMethods.closeStreams(this)
            }
        }
    }
}
