package dev.wilhelms.gradle.progress

import org.gradle.api.Project
import java.io.Serializable
import java.lang.reflect.Method

/**
 * A robust wrapper for Gradle's internal ProgressLogger.
 * Encapsulates reflection logic and provides a clean API with automatic fallbacks.
 */
class ProgressLogger(private val project: Project, private val taskClass: Class<*>) : Serializable {

    private var loggerInstance: Any? = null
    private var isStarted = false

    init {
        initializeLogger()
    }

    private fun initializeLogger() {
        try {
            // Get services from project
            val getServicesMethod = project.javaClass.getMethod("getServices")
            val services = getServicesMethod.invoke(project)
            
            // Try known ProgressLoggerFactory locations
            val factoryClass = try {
                Class.forName("org.gradle.internal.logging.progress.ProgressLoggerFactory")
            } catch (e: Exception) {
                Class.forName("org.gradle.internal.logging.ProgressLoggerFactory")
            }
            
            val getServiceMethod = services.javaClass.getMethod("get", Class::class.java)
            val factory = getServiceMethod.invoke(services, factoryClass)
            
            // Create a new operation
            val newOpMethod = factoryClass.getMethod("newOperation", Class::class.java)
            loggerInstance = newOpMethod.invoke(factory, taskClass)
        } catch (e: Exception) {
            // Logger remains null, fallback to silent or basic console will be used in methods
        }
    }

    fun start(description: String, shortDescription: String = "") {
        if (isStarted) return
        try {
            loggerInstance?.let { logger ->
                logger.javaClass.getMethod("setDescription", String::class.java).invoke(logger, description)
                if (shortDescription.isNotBlank()) {
                    logger.javaClass.getMethod("setShortDescription", String::class.java).invoke(logger, shortDescription)
                }
                logger.javaClass.getMethod("started").invoke(logger)
                isStarted = true
            }
        } catch (e: Exception) {
            // Fallback: Just print start message if interactive logging fails
            println("> $description")
        }
    }

    fun progress(message: String) {
        try {
            loggerInstance?.let { logger ->
                logger.javaClass.getMethod("progress", String::class.java).invoke(logger, message)
            } ?: run {
                // In-place console fallback (JUnit style)
                print("\r\u001B[K> $message")
                System.out.flush()
            }
        } catch (e: Exception) {
            // Silent fallback
        }
    }

    fun completed(status: String? = null) {
        if (!isStarted && loggerInstance == null) return
        try {
            loggerInstance?.let { logger ->
                if (status != null) {
                    logger.javaClass.getMethod("completed", String::class.java).invoke(logger, status)
                } else {
                    logger.javaClass.getMethod("completed").invoke(logger)
                }
            } ?: run {
                // Clear the in-place line
                print("\r\u001B[K")
                System.out.flush()
            }
        } catch (e: Exception) {
            // Silent fallback
        } finally {
            isStarted = false
        }
    }
}
