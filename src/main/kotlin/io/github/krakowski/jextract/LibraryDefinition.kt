package io.github.krakowski.jextract

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

abstract class LibraryDefinition {

    /** The library which should be used for generating native bindings. */
    @get:Input
    @get:Optional
    abstract val libraries: ListProperty<String>

    /** The package under which all bindings will be generated. */
    @get:Input
    abstract val targetPackage: Property<String>

    /** The name of the generated header class. */
    @get:Optional @get:Input
    abstract val className: Property<String>

    /** The header file which should be parsed by jextract. */
    @get:InputFile
    abstract val header: Property<String>

    /** Directories which should be included during code generation. */
    @get:Optional @get:Input
    abstract val includes: ListProperty<String>

    /** Whitelist of functions. */
    @get:Optional @get:Input
    abstract val functions: ListProperty<String>

    /** Whitelist of macros. */
    @get:Optional @get:Input
    abstract val macros: ListProperty<String>

    /** Whitelist of structs. */
    @get:Optional @get:Input
    abstract val structs: ListProperty<String>

    /** Whitelist of typedefs. */
    @get:Optional @get:Input
    abstract val typedefs: ListProperty<String>

    /** Whitelist of unions. */
    @get:Optional @get:Input
    abstract val unions: ListProperty<String>

    /** Whitelist of global variables. */
    @get:Optional @get:Input
    abstract val variables: ListProperty<String>

    /** List of additional defined C preprocessor macros. */
    @get:Optional @get:Input
    abstract val definedMacros: ListProperty<String>
}
