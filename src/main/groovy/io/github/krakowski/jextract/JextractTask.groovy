package io.github.krakowski.jextract

import org.gradle.api.Action
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
     * Whether to generate sources or precompiled class files
     */
    @Input
    final Property<Boolean> sourceMode = project.objects.property(Boolean)
        .convention(false)

    /**
     * The JDK home directory containing jextract.
     */
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
     * The output directory in which the generated code will be placed.
     */
    @OutputDirectory
    final DirectoryProperty outputDir = project.objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("generated/sources/jextract/main/java"))

    @Nested
    final List<LibraryDefinition> definitions = new ArrayList<>()

    JextractTask() { group = 'build' }

    @TaskAction
    def action() {

        // Check if jextract is present
        String javaPath = javaHome.get()
        Path jextractPath = Paths.get(javaPath, "bin/jextract")
        if (!Files.exists(jextractPath)) {
            throw new GradleException("jextract binary could not be found (JVM_HOME=${javaPath})")
        }


        for (LibraryDefinition definition : definitions) {

            // Initialize argument list
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

            // Include specified functions
            if (definition.functions.isPresent()) {
                definition.functions.get().forEach { function ->
                    arguments.add("--include-function")
                    arguments.add(function)
                }
            }

            // Include specified macros
            if (definition.macros.isPresent()) {
                definition.macros.get().forEach { macro ->
                    arguments.add("--include-macro")
                    arguments.add(macro)
                }
            }

            // Include specified structs
            if (definition.structs.isPresent()) {
                definition.structs.get().forEach { struct ->
                    arguments.add("--include-struct")
                    arguments.add(struct)
                }
            }

            // Include specified typedefs
            if (definition.typedefs.isPresent()) {
                definition.typedefs.get().forEach { typedef ->
                    arguments.add("--include-typedef")
                    arguments.add(typedef)
                }
            }

            // Include specified functions
            if (definition.unions.isPresent()) {
                definition.unions.get().forEach { union ->
                    arguments.add("--include-union")
                    arguments.add(union)
                }
            }

            // Include specified functions
            if (definition.variables.isPresent()) {
                definition.variables.get().forEach { variable ->
                    arguments.add("--include-var")
                    arguments.add(variable)
                }
            }

            // Add include paths if they are present
            if (includes.isPresent()) {
                includes.get().forEach { include ->
                    arguments.add("-I")
                    arguments.add(include)
                }
            }

            // Add library names if they are present
            if (definition.libraries.isPresent()) {
                if (definition.libraries.get().isEmpty()) {
                    throw new GradleException("At least on library has to be specified")
                }

                definition.libraries.get().forEach { library ->
                    arguments.add("-l")
                    arguments.add(library)
                }
            }

            // Add target package if it is present
            if (definition.targetPackage.isPresent()) {
                arguments.add("--target-package")
                arguments.add(definition.targetPackage.get())
            }

            if (definition.className.isPresent()) {
                arguments.add("--header-class-name")
                arguments.add(definition.className.get())
            }

            // Set output directory
            arguments.add("-d")
            arguments.add(outputDir.get().toString())

            execute("${jextractPath.toAbsolutePath()} ${arguments.join(" ")} ${definition.header.get()}")
        }
    }

    void fromHeader(String header, Action<LibraryDefinition> action) {
        LibraryDefinition definition = project.objects.newInstance(LibraryDefinition)
        definition.header.set(header)
        action.execute(definition)
        definitions.add(definition)
    }

    private static void execute(String command) {

        // Create buffers for stdout and stderr streams
        StringBuffer stdout = new StringBuffer()
        StringBuffer stderr = new StringBuffer()
        Process result = command.execute()

        // Wait until the process finishes and check if it suceeded
        result.waitForProcessOutput(stdout, stderr)
        if (result.exitValue() != 0) {
            throw new GradleException("Invoking jextract failed.\n\n command: ${command}\n stdout: ${stdout}\n stderr: ${stderr}")
        }
    }
}
