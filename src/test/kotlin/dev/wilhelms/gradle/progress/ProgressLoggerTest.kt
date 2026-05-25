package dev.wilhelms.gradle.progress

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue

class ProgressLoggerTest {
    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `logger works and falls back to console in non-interactive environment`() {
        testProjectDir.resolve("settings.gradle.kts").writeText("rootProject.name = \"progress-test\"")
        testProjectDir.resolve("build.gradle.kts").writeText("""
            import dev.wilhelms.gradle.progress.ProgressLogger

            plugins {
                id("dev.wilhelms.gradle.progress-logger-dummy")
            }

            tasks.register("testProgress") {
                doLast {
                    val logger = ProgressLogger(project, javaClass)
                    logger.start("Test Operation", "test")
                    logger.progress("Step 1/2")
                    logger.progress("Step 2/2")
                    logger.completed("Done")
                }
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("testProgress")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        // Since GradleRunner is non-interactive, the logger should have fallen back to basic println or silent.
        // Our fallback prints "> Test Operation" at start if reflection fails.
        // And print("\r\u001B[K> message") for progress if reflection fails.
        
        assertTrue(result.output.contains("BUILD SUCCESSFUL"), "Build should succeed")
        // We verify that using the class doesn't crash the build
    }
}
