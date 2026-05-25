package dev.wilhelms.gradle.progress

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import kotlin.test.assertTrue

class ProgressLoggerVersionTest {
    @TempDir
    lateinit var testProjectDir: File

    @ParameterizedTest
    @ValueSource(strings = ["7.6", "8.0", "8.5", "8.12", "9.0"])
    fun `logger works across different Gradle versions`(gradleVersion: String) {
        testProjectDir.resolve("settings.gradle.kts").writeText("rootProject.name = \"version-test-$gradleVersion\"")
        testProjectDir.resolve("build.gradle.kts").writeText("""
            import dev.wilhelms.gradle.progress.ProgressLogger

            plugins {
                id("dev.wilhelms.gradle.progress-logger-dummy")
            }

            tasks.register("testProgress") {
                doLast {
                    val logger = ProgressLogger(project, javaClass, true)
                    logger.start("Testing Version ${gradleVersion}", "test")
                    logger.progress("Step 1")
                    logger.completed("Done")
                }
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("testProgress", "-Dprogress.debug=true")
            .withGradleVersion(gradleVersion)
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL"), "Build with Gradle $gradleVersion should succeed")
        assertTrue(result.output.contains("Using mechanism: native"), "Native ProgressLogger should be detected in Gradle $gradleVersion")
    }
}
