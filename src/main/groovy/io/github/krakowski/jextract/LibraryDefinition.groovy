package io.github.krakowski.jextract

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

abstract class LibraryDefinition {

    /**
     * The library which should be used for generating native bindings.
     */
    @Input
    abstract ListProperty<String> getLibraries()

    /**
     * The package under which all bindings will be generated.
     */
    @Input
    abstract Property<String> getTargetPackage()

    /**
     * The name of the generated header class.
     */
    @Optional
    @Input
    abstract Property<String> getClassName()

    /**
     * The header file which should be parsed by jextract.
     */
    @InputFile
    abstract Property<String> getHeader()

    /**
     * Directories which should be included during code generation.
     */
    @Optional
    @Input
    abstract ListProperty<String> getInlcudes()

    /**
     * Whitelist of functions.
     */
    @Optional
    @Input
    abstract ListProperty<String> getFunctions()

    /**
     * Whitelist of macros.
     */
    @Optional
    @Input
    abstract ListProperty<String> getMacros()

    /**
     * Whitelist of structs.
     */
    @Optional
    @Input
    abstract ListProperty<String> getStructs()

    /**
     * Whitelist of typedefs.
     */
    @Optional
    @Input
    abstract ListProperty<String> getTypedefs()

    /**
     * Whitelist of unions.
     */
    @Optional
    @Input
    abstract ListProperty<String> getUnions()

    /**
     * Whitelist of global variables.
     */
    @Optional
    @Input
    abstract ListProperty<String> getVariables()
}
