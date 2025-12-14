import com.gradleup.librarian.gradle.Librarian

plugins {
  kotlin("multiplatform").apply(false)
  id("com.gradleup.librarian").apply(false)
}

Librarian.root(project)

// `./gradlew refreshVersions` to update dependencies
// `./gradlew publishToMavenLocal` to publish locally
