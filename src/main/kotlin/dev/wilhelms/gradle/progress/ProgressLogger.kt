package dev.wilhelms.gradle.progress

import org.gradle.api.Project
import java.io.Serializable
import java.lang.reflect.Method

/**
 * A robust wrapper for Gradle's internal ProgressLogger.
 * Encapsulates reflection logic and provides a clean API with automatic fallbacks.
 */
class ProgressLogger(
    private val project: Project, 
    private val taskClass: Class<*>,
    private val forceNative: Boolean = false
) : Serializable {

    private var loggerInstance: Any? = null
    private var isStarted = false
    private var usedMechanism: String = "none"

    init {
        initializeLogger()
    }

    private fun initializeLogger() {
        try {
            val getServicesMethod = project.javaClass.getMethod("getServices")
            val services = getServicesMethod.invoke(project)
            
            // Comprehensive package list for different Gradle versions
            val factoryClass = listOf(
                "org.gradle.internal.logging.progress.ProgressLoggerFactory",
                "org.gradle.internal.logging.ProgressLoggerFactory"
            ).firstNotNullOfOrNull { name ->
                try { Class.forName(name) } catch (e: Exception) { null }
            }
            
            if (factoryClass != null) {
                val getServiceMethod = services.javaClass.getMethod("get", Class::class.java)
                val factory = getServiceMethod.invoke(services, factoryClass)
                
                val newOpMethod = factoryClass.getMethod("newOperation", Class::class.java)
                loggerInstance = newOpMethod.invoke(factory, taskClass)
                usedMechanism = "native"
            }
        } catch (e: Exception) {
            if (forceNative) {
                println("WARNING: Native ProgressLogger initialization failed: ${e.message}")
            }
        }
        
        if (loggerInstance == null) {
            usedMechanism = "console-fallback"
        }
    }

    private fun invokeMethod(obj: Any, methodName: String, vararg args: Any?) {
        // Special handling for String parameters because they might be GStrings or other CharSequences
        val normalizedArgs = args.map { it?.toString() }.toTypedArray()
        val normalizedTypes = Array(args.size) { String::class.java }

        // Find method on the class or any of its interfaces
        var method: Method? = null
        var currentClass: Class<*>? = obj.javaClass
        
        while (currentClass != null && method == null) {
            try {
                method = currentClass.getDeclaredMethod(methodName, *normalizedTypes)
            } catch (e: Exception) {
                // Try interfaces
                for (iface in currentClass.interfaces) {
                    try {
                        method = iface.getDeclaredMethod(methodName, *normalizedTypes)
                        if (method != null) break
                    } catch (e2: Exception) {}
                }
            }
            currentClass = currentClass.superclass
        }

        if (method == null && args.isEmpty()) {
            // Try no-arg version
             currentClass = obj.javaClass
             while (currentClass != null && method == null) {
                try {
                    method = currentClass.getDeclaredMethod(methodName)
                } catch (e: Exception) {
                    for (iface in currentClass.interfaces) {
                        try {
                            method = iface.getDeclaredMethod(methodName)
                            if (method != null) break
                        } catch (e2: Exception) {}
                    }
                }
                currentClass = currentClass.superclass
            }
        }

        if (method != null) {
            method.isAccessible = true
            if (method.parameterCount == 0) {
                method.invoke(obj)
            } else {
                method.invoke(obj, *normalizedArgs)
            }
        } else {
            throw NoSuchMethodException("Method $methodName not found on ${obj.javaClass}")
        }
    }

    fun start(description: String, shortDescription: String = "") {
        if (isStarted) return
        
        if (forceNative || System.getProperty("progress.debug") == "true") {
            println("[ProgressLogger Debug] Using mechanism: $usedMechanism")
        }

        try {
            loggerInstance?.let { logger ->
                invokeMethod(logger, "setDescription", description)
                if (shortDescription.isNotBlank()) {
                    try { invokeMethod(logger, "setShortDescription", shortDescription) } catch (e: Exception) {}
                }
                
                try {
                    invokeMethod(logger, "started")
                } catch (e: Exception) {
                    // Try older 'start' method
                    val clazz = logger.javaClass
                    val m = clazz.getMethod("start", String::class.java, String::class.java)
                    m.isAccessible = true
                    m.invoke(logger, description, shortDescription)
                }
                
                isStarted = true
                return
            }
        } catch (e: Exception) { 
             if (forceNative) {
                 println("DEBUG: Failed to start native logger: ${e.message}")
             }
        }
        
        // Fallback
        println("> $description")
        isStarted = true
    }

    fun progress(message: String) {
        try {
            loggerInstance?.let { logger ->
                invokeMethod(logger, "progress", message)
                return
            }
        } catch (e: Exception) { 
            if (forceNative) println("DEBUG: Progress update failed: ${e.message}")
        }
        
        // In-place console fallback
        print("\r\u001B[K> $message")
        System.out.flush()
    }

    fun completed(status: String? = null) {
        if (!isStarted) return
        try {
            loggerInstance?.let { logger ->
                if (status != null) {
                    try {
                        invokeMethod(logger, "completed", status)
                    } catch (e: Exception) {
                        invokeMethod(logger, "completed")
                    }
                } else {
                    invokeMethod(logger, "completed")
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
