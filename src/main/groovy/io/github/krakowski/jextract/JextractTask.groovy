package io.github.krakowski.jextract

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.internal.jvm.Jvm

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class JextractTask extends DefaultTask {

    /**
     * Arguments which should be passed to clang.
     */
    @Optional
    @Input
    final Property<String> clangArguments = project.objects.property(String)

    /**
     * The library which should be used for generating native bindings.
     */
    @Input
    final Property<String> library = project.objects.property(String)

    /**
     * The package under which all bindings will be generated.
     */
    @Input
    final Property<String> targetPackage = project.objects.property(String)

    /**
     * Whether to generate sources or precompiled class files
     */
    @Input
    final Property<Boolean> sourceMode = project.objects.property(Boolean)
        .convention(false)

    @Optional
    @Input
    final Property<String> javaHome = project.objects.property(String)
        .convention(Jvm.current().javaHome.absolutePath)

    /**
     * Directories which should be included during code generation.
     */
    @Optional
    @Input
    final ListProperty<String> includes = project.objects.listProperty(String)

    /**
     * The header file which should be parsed by jextract.
     */
    @InputFile
    final Property<String> header = project.objects.property(String)

    /**
     * The output directory in which the generated code will be placed.
     */
    @OutputDirectory
    final DirectoryProperty outputDir = project.objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("generated/sources/jextract/main/java"))

    JextractTask() { group = 'build' }

    @TaskAction
    def action() {

        List<String> arguments = new ArrayList<>()

        // Add source mode flag if it was enabled by the user
        if (sourceMode.get()) {
            arguments.add("--source")
        }

        // Add clang arguments if they are present
        if (clangArguments.isPresent()) {
            arguments.add("-C")
            arguments.add(clangArguments.get())
        }

        // Add include paths if they are present
        if (includes.isPresent()) {
            includes.get().forEach { include ->
                arguments.add("-I")
                arguments.add(include)
            }
        }

        // Add library name if it is present
        if (library.isPresent()) {
            arguments.add("-l")
            arguments.add(library.get())
        }

        // Add target package if it is present
        if (targetPackage.isPresent()) {
            arguments.add("-t")
            arguments.add(targetPackage.get())
        }

        // Set output directory
        arguments.add("-d")
        arguments.add(outputDir.get().toString())

        // Check if jextract is present

        String javaPath = javaHome.get()
        Path jextractPath = Paths.get(javaPath, "bin/jextract")
        if (!Files.exists(jextractPath)) {
            throw new GradleException("jextract binary could not be found (JVM_HOME=${javaPath})")
        }

        execute("${jextractPath.toAbsolutePath()} ${arguments.join(" ")} ${header.get()}")
    }

    private static void execute(String command) {

        // Create buffers for stdout and stderr streams
        StringBuffer stdout = new StringBuffer()
        StringBuffer stderr = new StringBuffer()
        Process result = command.execute()

        // Wait until the process finishes and check if it suceeded
        result.waitForProcessOutput(stdout, stderr)
        if (result.exitValue() != 0) {
            throw new GradleException("jextract: stdout: ${stdout}. stderr: ${stderr}")
        }
    }
}
