# Gradle Progress Logger Library

A robust, version-agnostic wrapper for Gradle's internal `ProgressLogger`.

> [!IMPORTANT]
> This library was developed with **AI assistance** to ensure high-fidelity reflection logic and cross-version compatibility.

## Features
- **Version Compatibility:** Supports Gradle 7.x, 8.x, and **Gradle 9.x**.
- **Robust Reflection:** Safely accesses internal APIs without breaking the build, handling classloader isolation and visibility restrictions.
- **JUnit-style Fallback:** Automatically switches to in-place terminal updates (`\r`) if the native status bar is unavailable or non-interactive.
- **Configuration Cache Ready:** Designed to be used within tasks without breaking serialization.

## Why this exists?
Gradle does not provide a stable public API for updating the status bar during task execution. This library encapsulates the complex reflection required to use the internal `ProgressLogger` safely across different Gradle versions.

## Usage

Add the dependency to your plugin project:

```kotlin
dependencies {
    implementation("dev.wilhelms.gradle:gradle-progress-logger:0.1.0-SNAPSHOT")
}
```

Use it in your task:

```kotlin
val progress = ProgressLogger(project, javaClass)
progress.start("Task Description", "Short Name")
// ...
progress.progress("Analyzing item 15/100")
// ...
progress.completed("Done!")
```

## License
Licensed under the [Apache License, Version 2.0](LICENSE).
