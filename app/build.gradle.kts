plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
}

android {
    namespace = "com.selfgrowthfund.sgf"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.selfgrowthfund.sgf"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // ✅ Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")

    // ✅ Tooling for @Preview
    debugImplementation(libs.compose.tooling)
    implementation(libs.compose.tooling.preview)

    // ✅ Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.ksp)

    // ✅ Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ✅ Networking
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // ✅ Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // ✅ AndroidX Core
    implementation(libs.androidx.core.ktx)

    // ✅ Gson
    implementation(libs.gson)

    // ✅ Unit Tests (JVM)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)

    // ✅ Instrumentation tests (Android)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.room.testing)
}