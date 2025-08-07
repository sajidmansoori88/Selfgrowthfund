pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Critical fix
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SelfGrowthFund"
include(":app")