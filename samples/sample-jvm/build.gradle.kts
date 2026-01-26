plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  // Kotlin
  implementation(libs.kotlinx.coroutines.jdk9)

  // Logging
  implementation(libs.klibnanolog)

  // Date time
  implementation(libs.kotlinx.datetime)

  // Library
  implementation(project(":klibfitbit"))
}

// See https://github.com/BoD/k2o/pull/4
configurations.named { it == "mainSourceElements" }.configureEach {
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "sources"))
  }
}
