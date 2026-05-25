plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    `signing`
    id("org.jetbrains.dokka") version "1.9.10"
}

group = "dev.wilhelms.gradle"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    testImplementation(gradleApi())
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.test {
    useJUnitPlatform()
}

// State of the Art: Use internal plugin ONLY for tests
gradlePlugin {
    plugins {
        create("progressLoggerTestHelper") {
            id = "dev.wilhelms.gradle.progress-logger-test-helper"
            implementationClass = "dev.wilhelms.gradle.progress.internal.ProgressLoggerTestHelper" 
        }
    }
}

// Crucial: Point TestKit to the test source set classes as well!
tasks.pluginUnderTestMetadata {
    pluginClasspath.from(sourceSets.test.get().output)
}

// ...but we EXCLUDE the plugin metadata from the published JAR!
tasks.jar {
    exclude("META-INF/gradle-plugins/dev.wilhelms.gradle.progress-logger-test-helper.properties")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "gradle-progress-logger"
            
            pom {
                name.set("Gradle Progress Logger")
                description.set("A robust, version-agnostic wrapper for Gradle's internal ProgressLogger.")
                url.set("https://github.com/jpwilhelms/gradle-progress-logger")
                
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                
                developers {
                    developer {
                        id.set("jpwilhelms")
                        name.set("Jan-Peter Wilhelms")
                        email.set("jan-peter@familie-wilhelms.de")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/jpwilhelms/gradle-progress-logger.git")
                    developerConnection.set("scm:git:ssh://github.com:jpwilhelms/gradle-progress-logger.git")
                    url.set("https://github.com/jpwilhelms/gradle-progress-logger")
                }
            }
        }
    }
}

signing {
    setRequired({
        (project.extra.has("isRelease") && project.extra.get("isRelease") == "true") ||
        gradle.taskGraph.hasTask("publish")
    })
    sign(publishing.publications["maven"])
}
