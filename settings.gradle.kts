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
//        maven {
//            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
//            authentication
//            {
//                basic(BasicAuthentication)
//            }
//            credentials
//            {
//                username = "mapbox"
//                password = System.getenv('MAPBOX_DOWNLOADS_TOKEN') ?: ""
//            }
//
//        }
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = "sk.eyJ1IjoicWluZzI3MTgiLCJhIjoiY20wOWU1eTB4MWdiZTJrc2h3YmY0YnByNCJ9.EiOXBXZu1D0UXUNAAtWF_g"
                    //project.properties["MAPBOX_DOWNLOADS_TOKEN"] as? String ?: ""
            }
        }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ShareFi"
include(":app")
include(":direct_share")
include(":chat")
