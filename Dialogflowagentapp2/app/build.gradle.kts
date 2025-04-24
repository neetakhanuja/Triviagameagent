plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.triviagame.dialogflowagentapp2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.triviagame.dialogflowagentapp2"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        packagingOptions {
            exclude("META-INF/INDEX.LIST")
            exclude("META-INF/*")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

    dependencies {
        // AndroidX libraries
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)

        // Dialogflow SDK and gRPC dependencies
        implementation("com.google.cloud:google-cloud-dialogflow:4.4.0")
        implementation("io.grpc:grpc-okhttp:1.40.0")
        implementation("io.grpc:grpc-protobuf:1.40.0")
        implementation("io.grpc:grpc-stub:1.40.0")

        // Google Auth Library for Dialogflow authentication
        implementation("com.google.auth:google-auth-library-oauth2-http:0.27.0")
    }
}

