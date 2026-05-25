package dev.wilhelms.gradle.progress

import org.gradle.api.Plugin
import org.gradle.api.Project

class ProgressPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // This plugin just provides the ProgressLogger capability.
        // Users can instantiate ProgressLogger manually or we could add a factory here.
        project.extensions.create("progressLogger", ProgressLoggerFactory::class.java, project)
    }
}

open class ProgressLoggerFactory(private val project: Project) {
    fun getLogger(taskClass: Class<*>): ProgressLogger {
        return ProgressLogger(project, taskClass)
    }
}
