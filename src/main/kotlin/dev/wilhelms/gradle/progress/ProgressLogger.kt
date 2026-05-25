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
            
            // Try known ProgressLoggerFactory locations (Gradle 7.x vs 8.x)
            val factoryClass = listOf(
                "org.gradle.internal.logging.progress.ProgressLoggerFactory",
                "org.gradle.internal.logging.ProgressLoggerFactory"
            ).firstNotNullOfOrNull { 
                try { Class.forName(it) } catch (e: Exception) { null }
            } ?: throw IllegalStateException("ProgressLoggerFactory not found")
            
            val getServiceMethod = services.javaClass.getMethod("get", Class::class.java)
            val factory = getServiceMethod.invoke(services, factoryClass)
            
            // Create a new operation
            val newOpMethod = factoryClass.getMethod("newOperation", Class::class.java)
            loggerInstance = newOpMethod.invoke(factory, taskClass)
        } catch (e: Exception) {
            // Logger remains null
        }
    }

    fun start(description: String, shortDescription: String = "") {
        if (isStarted) return
        try {
            if (loggerInstance != null) {
                loggerInstance!!.javaClass.getMethod("setDescription", String::class.java).invoke(loggerInstance, description)
                if (shortDescription.isNotBlank()) {
                    loggerInstance!!.javaClass.getMethod("setShortDescription", String::class.java).invoke(loggerInstance, shortDescription)
                }
                loggerInstance!!.javaClass.getMethod("started").invoke(loggerInstance)
                isStarted = true
                return
            }
        } catch (e: Exception) { }
        
        // Fallback
        println("> $description")
        isStarted = true
    }

    fun progress(message: String) {
        try {
            if (loggerInstance != null) {
                loggerInstance!!.javaClass.getMethod("progress", String::class.java).invoke(loggerInstance, message)
                return
            }
        } catch (e: Exception) { }
        
        // In-place console fallback
        print("\r\u001B[K> $message")
        System.out.flush()
    }

    fun completed(status: String? = null) {
        if (!isStarted) return
        try {
            if (loggerInstance != null) {
                if (status != null) {
                    loggerInstance!!.javaClass.getMethod("completed", String::class.java).invoke(loggerInstance, status)
                } else {
                    loggerInstance!!.javaClass.getMethod("completed").invoke(loggerInstance)
                }
                return
            }
        } catch (e: Exception) { }
        
        // Clear the in-place line
        if (status != null) {
            println("\r\u001B[K> Completed: $status")
        } else {
            print("\r\u001B[K")
            System.out.flush()
        }
        isStarted = false
    }
}
