plugins {
  kotlin("jvm")
}

dependencies {
  // Kotlin
  implementation(KotlinX.coroutines.jdk9)

  // Logging
  implementation("org.jraf.klibnanolog:klibnanolog:_")

  // Date time
  implementation(KotlinX.datetime)

  // Library
  implementation(project(":klibfitbit"))
}

// See https://github.com/BoD/k2o/pull/4
configurations.named { it == "mainSourceElements" }.configureEach {
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "sources"))
  }
}
