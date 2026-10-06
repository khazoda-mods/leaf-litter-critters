pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases/") { name = "KikuGie Releases" }
    }
}

plugins {
    // https://stonecutter.kikugie.dev/blog/changes/0.9
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        versions("26.1", "26.2", "26.3")
        vcsVersion = "26.1"
    }
}

rootProject.name = "Leaf Litter Critters"

if (providers.gradleProperty("neoforge").isPresent) {
    include(":neoforge")
    project(":neoforge").projectDir = file("dev/neoforge")
}
