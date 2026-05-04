pluginManagement {
    repositories {
        maven { name = "custom"; url = uri("https://maven.myket.ir") }
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
        maven { name = "custom"; url = uri("https://maven.myket.ir") }
        google()
        mavenCentral()
    }
}

rootProject.name = "My Car Services"
include(":app")
