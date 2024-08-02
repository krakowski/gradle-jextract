![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/krakowski/gradle-jextract/publish.yml?label=GitHub%20Workflow)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.krakowski.jextract?label=Gradle%20Plugin%20Portal&color=1e81b0)](https://plugins.gradle.org/plugin/io.github.krakowski.jextract)


This is a Gradle plugin for integrating Project Panama's [`jextract`](https://github.com/openjdk/jextract) tool in the build process.
There is also a [full demo project](https://github.com/krakowski/jextract-demo) showcasing the `gradle-jextract` plugin.
## :bulb: &nbsp; Example

Since the plugin is available on [Gradle's Plugin Portal](https://plugins.gradle.org/) it can be applied within the build script's `plugins` block.

```gradle
plugins {
  id "io.github.krakowski.jextract" version "0.5.0"
}
```

Applying the plugin adds the `jextract` task which can be configured by the build script.

```gradle
jextract {

    header("${project.projectDir}/src/main/c/stdio.h") {
        // The library name
        libraries = [ 'stdc++' ]
    
        // The package under which all source files will be generated
        targetPackage = 'org.unix'
        
        // The generated class name
        className = 'Linux'
    }
}
```

If the Gradle [Java Plugin](https://docs.gradle.org/current/userguide/java_plugin.html) or
[Application plugin](https://docs.gradle.org/current/userguide/application_plugin.html) is applied, the `gradle-jextract`
plugin configures them and uses the configured toolchain for its task, which can be set as follows.

```
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }
}
```

If your JDK is not installed in one of the default locations, Gradle can be instructed to search in a custom location.
To enable this feature the `org.gradle.java.installations.paths` property has to be set within your global `gradle.properties`
file usually located inside `${HOME}/.gradle`.

```
org.gradle.java.installations.paths=/custom/path/jdk22
```

The plugin will first try to find `jextract` inside `PATH` and then fall back to `${JAVA_HOME}/bin`.

## :triangular_ruler: &nbsp; Configuration Options

The `jextract` task exposes the following configuration options.

|          Name          |               Type              |    Required    | Description                                                                   |
|:----------------------:|:-------------------------------:|:--------------:|-------------------------------------------------------------------------------|
|      `libraries`       |       `java.lang.String[]`      |                | The libraries against which the native code will link [¹](#green_book--notes) |
|       `includes`       |       `java.lang.String[]`      |                | A list of directories which should be included during code generation         |
|    `targetPackage`     |        `java.lang.String`       | :black_circle: | The package under which all bindings will be generated                        |
|      `className`       |        `java.lang.String`       |                | The generated class file's name                                               |
|      `functions`       |       `java.lang.String[]`      |                | Whitelist of function symbols                                                 |
|      `constants`       |       `java.lang.String[]`      |                | Whitelist of macro and enum constant symbols                                  |
|       `structs`        |       `java.lang.String[]`      |                | Whitelist of struct symbols                                                   |
|       `typedefs`       |       `java.lang.String[]`      |                | Whitelist of typedef symbols                                                  |
|        `unions`        |       `java.lang.String[]`      |                | Whitelist of union symbols                                                    |
|      `variables`       |       `java.lang.String[]`      |                | Whitelist of global variable symbols                                          |
|    `definedMacros`     |       `java.lang.String[]`      |                | List of additional defined C preprocessor macros                              |
| `useSystemLoadLibrary` |       `java.lang.Boolean`       |                | Load libraries into the loader symbol lookup                                  |
|      `outputDir`       | `org.gradle.api.file.Directory` |                | The output directory under which the generated source files will be placed    |

## :green_book: &nbsp; Notes

> ¹ *libraries* option can use simple library name "stdc++" or full path to the shared libraries.
> Full path need to be prefixed with ":" character.
> For example ":/usr/lib/libstdc++.so" 
> [doc](https://github.com/openjdk/jextract/blob/master/doc/GUIDE.md#library-loading)

## :wrench: &nbsp; Requirements

  * [OpenJDK 22](https://openjdk.org/projects/jdk/22/)
  * [Project Jextract](https://jdk.java.net/jextract/)
  
## :scroll: &nbsp; License

This project is licensed under the GNU GPLv3 License - see the [LICENSE](LICENSE) file for details.

