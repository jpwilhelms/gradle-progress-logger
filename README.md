# Gradle Progress Logger Plugin

A robust, version-agnostic wrapper for Gradle's internal `ProgressLogger`.

## Features
- **Version Compatibility:** Supports Gradle 7.x and 8.x internal API changes.
- **Robust Reflection:** Safely accesses internal APIs without breaking the build.
- **JUnit-style Fallback:** Automatically switches to in-place terminal updates (`\r`) if the native status bar is unavailable or non-interactive.
- **Configuration Cache Ready:** Designed to be used within tasks without breaking serialization.

## Usage

Add the dependency to your plugin project:

```kotlin
dependencies {
    implementation("dev.wilhelms.gradle:gradle-progresslogger-plugin:0.1.0-SNAPSHOT")
}
```

Use it in your task:

```kotlin
val progress = ProgressLogger(project, javaClass)
progress.start("Task Description", "Short Name")
// ...
progress.progress("Status Update")
// ...
progress.completed("Done!")
```
