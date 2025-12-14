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
