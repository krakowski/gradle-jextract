package io.github.krakowski.jextract

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class JextractPluginFunctionalTest {
    @TempDir
    private lateinit var testProjectDir: File

    private lateinit var settingsFile: File

    private lateinit var buildFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle").apply {
            writeText("""
                rootProject.name = 'jextract-sample'
            """.trimIndent())
        }
        buildFile = File(testProjectDir, "build.gradle").apply {
            writeText("""
            plugins {
                id 'io.github.krakowski.jextract'
            }    
            """.trimIndent())
        }
    }

    @Test
    fun `test whether jextract task succeeds`() {
        val result = GradleRunner.create()
            // can be enabled for debugging, alternatively set `org.gradle.testkit.debug` System property to `true`
//            .withDebug(true)
            .withProjectDir(testProjectDir)
            .withArguments("jextract")
            .withPluginClasspath()
            .build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":jextract")?.outcome)
    }
}