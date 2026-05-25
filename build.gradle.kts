plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "dev.wilhelms.gradle"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

gradlePlugin {
    plugins {
        create("progressLogger") {
            id = "dev.wilhelms.gradle.progress-logger"
            implementationClass = "dev.wilhelms.gradle.progress.ProgressPlugin"
        }
    }
}
