// settings.gradle.kts
pluginManagement {
    repositories {
        google() // Resolves Android and Google dependencies
        mavenCentral() // Resolves other libraries like Dialogflow
        jcenter() // If needed, resolve other dependencies from JCenter
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Make sure repositories are taken from settings
    repositories {
        google() // Resolves Android and Google dependencies
        mavenCentral() // Resolves other libraries like Dialogflow
    }
}


rootProject.name = "Triviapp"
include(":app")
 