rootProject.name = "klibfitbit-root"

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://storage.googleapis.com/gradleup/m2")
  }
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    mavenLocal()
    mavenCentral()
    maven("https://storage.googleapis.com/gradleup/m2")
  }
}

plugins {
  // See https://splitties.github.io/refreshVersions/
  id("de.fayard.refreshVersions") version "0.60.6"
}

include(":library")
project(":library").name = "klibfitbit"

// Include all the sample modules from the "samples" directory
file("samples").listFiles()!!.forEach { dir ->
  include(dir.name)
  project(":${dir.name}").apply {
    projectDir = dir
    name = dir.name
  }
}
