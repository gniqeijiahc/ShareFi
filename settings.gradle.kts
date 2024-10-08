pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")

        }
        mavenCentral()

        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ShareFi"
include(":app")
include(":direct_share")
include(":chat")
