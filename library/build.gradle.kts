import com.gradleup.librarian.gradle.Librarian

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

kotlin {
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        // Ktor
        implementation(Ktor.client.core)
        implementation(Ktor.client.contentNegotiation)
        implementation(Ktor.client.auth)
        implementation(Ktor.client.logging)
        implementation(Ktor.plugins.serialization.kotlinx.json)

        // Okio
        implementation(Square.okio)

        // Date time
        implementation(KotlinX.datetime)

        // Serialization
        implementation(KotlinX.serialization.json)

        // Logging
        implementation("org.jraf.klibnanolog:klibnanolog:_")
      }
    }

    jvmMain {
      dependencies {
        // Coroutines
        implementation(KotlinX.coroutines.jdk9)

        // Ktor OkHttp
        implementation(Ktor.client.okHttp)
      }
    }
  }
}

Librarian.module(project)

// Run `./gradlew publishToMavenLocal` to publish to the local maven repo
// Run `./gradlew librarianPublishToMavenCentral` to publish to Maven Central
