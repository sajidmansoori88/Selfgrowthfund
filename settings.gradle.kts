pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    // Allow settings repositories to take precedence
    repositoriesMode.set(org.gradle.api.initialization.resolve.RepositoriesMode.PREFER_SETTINGS)

    repositories {
        google()
        mavenCentral()
    }

    // Optional: You can add a version catalog import here if you use libs.versions.toml
    // versionCatalogs {
    //     create("libs") {
    //         from(files("gradle/libs.versions.toml"))
    //     }
    // }
}

rootProject.name = "SelfGrowthFund"
include(":app")
