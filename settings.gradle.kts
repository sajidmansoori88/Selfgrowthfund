pluginManagement {
     repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SelfGrowthFund"
include(":app")