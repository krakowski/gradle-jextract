This is a Gradle plugin for integrating Project Panama's [`jextract`](http://cr.openjdk.java.net/~mcimadamore/panama/jextract_distilled.html) tool in the build process.

## :bulb: &nbsp; Example

Since the plugin is available on [Gradle's Plugin Portal](https://plugins.gradle.org/) it can be applied within the build script's `plugins` block.

```gradle
plugins {
  id "io.github.krakowski.jextract" version "0.2.1"
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
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
```

If your JDK is not installed in one of the default locations, Gradle can be instructed to search in a custom location.
To enable this feature the `org.gradle.java.installations.paths` property has to be set within your global `gradle.properties`
file usually located inside `${HOME}/.gradle`.

```
org.gradle.java.installations.paths=/custom/path/jdk17
```

There is also a [full demo project](https://github.com/krakowski/jextract-demo) showcasing the `gradle-jextract` plugin.

## :triangular_ruler: &nbsp; Configuration Options

The `jextract` task exposes the following configuration options.

|       Name       |               Type              |    Required    | Description                                                                |
|:----------------:|:-------------------------------:|:--------------:|----------------------------------------------------------------------------|
| `clangArguments` |        `java.lang.String`       |                | Arguments which should be passed to clang                                  |
|    `libraries`   |       `java.lang.String[]`      | :black_circle: | The libraries against which the native code will link                      |
|    `includes`    |       `java.lang.String[]`      |                | A list of directories which should be included during code generation      |
|  `targetPackage` |        `java.lang.String`       | :black_circle: | The package under which all bindings will be generated                     |
|    `className`   |        `java.lang.String`       |                | The generated class file's name                                            |
|    `functions`   |       `java.lang.String[]`      |                | Whitelist of function symbols                                              |
|     `macros`     |       `java.lang.String[]`      |                | Whitelist of macro symbols                                                 |
|     `structs`    |       `java.lang.String[]`      |                | Whitelist of struct symbols                                                |
|    `typedefs`    |       `java.lang.String[]`      |                | Whitelist of typedef symbols                                               |
|     `unions`     |       `java.lang.String[]`      |                | Whitelist of union symbols                                                 |
|    `variables`   |       `java.lang.String[]`      |                | Whitelist of global variable symbols                                       |
|   `sourceMode`   |       `java.lang.Boolean`       |                | Generate source files instead of class files (default: `true`)             |
|    `outputDir`   | `org.gradle.api.file.Directory` |                | The output directory under which the generated source files will be placed |

## :wrench: &nbsp; Requirements

  * [OpenJDK 17 + Project Panama](https://github.com/openjdk/panama-foreign/tree/foreign-jextract)
  * [LLVM 9+](https://releases.llvm.org/download.html)
  
## :scroll: &nbsp; License

This project is licensed under the GNU GPLv3 License - see the [LICENSE](LICENSE) file for details.