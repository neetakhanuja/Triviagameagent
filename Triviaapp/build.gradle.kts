// Project-level build.gradle.kts

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

repositories {
    google()  // Resolves Android and Google dependencies
    mavenCentral()  // Resolves other libraries like Dialogflow, etc.
}
