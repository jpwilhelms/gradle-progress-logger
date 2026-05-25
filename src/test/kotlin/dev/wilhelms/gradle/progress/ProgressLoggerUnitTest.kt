package dev.wilhelms.gradle.progress

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class ProgressLoggerUnitTest {
    @Test
    fun `can instantiate logger with ProjectBuilder`() {
        val project = ProjectBuilder.builder().build()
        val logger = ProgressLogger(project, javaClass)
        assertNotNull(logger)
        
        // This should use the fallback logic since internal APIs might not be fully linked in ProjectBuilder
        logger.start("Test Unit", "unit")
        logger.progress("Doing stuff")
        logger.completed("Done")
    }
}
