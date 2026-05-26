package dev.wilhelms.gradle.progress.internal

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Internal helper to satisfy Gradle's withPluginClasspath() in functional tests.
 * This class is NOT intended for public use.
 */
class ProgressLoggerTestHelper : Plugin<Project> {
    override fun apply(project: Project) {
        // No-op, just here for the classpath
    }
}
