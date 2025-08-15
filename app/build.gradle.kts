import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.compose.compiler)
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
    ndkVersion = "27.0.12077973"
    buildToolsVersion = "36.0.0"

    kotlin {
        jvmToolchain(17)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17) // ✅ Correct enum usage
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    // ✅ Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.compose.tooling.preview)
    implementation(libs.room.testing)
    implementation(libs.androidx.junit.ktx)
    debugImplementation(libs.compose.tooling)

    // ✅ AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // ✅ Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.ksp)

    // ✅ Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.fragment)
    implementation(libs.hilt.navigation.compose)

    // ✅ Lifecycle
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)

    // ✅ Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // ✅ Networking
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // ✅ Gson
    implementation(libs.gson)

    // ✅ Logging
    implementation(libs.timber)

    // ✅ Material (classic)
    implementation(libs.material)

    // ✅ Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // ✅ Unit Tests
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)

    // ✅ Instrumentation Tests
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.room.testing)

    // ✅ Firebase BOM
    implementation(platform(libs.firebase.bom))

    // ✅Firestore
    implementation(libs.firebase.firestore)

    // ✅ Optional: Firebase Auth
    implementation(libs.firebase.auth)

    androidTestImplementation ("androidx.room:room-testing:2.7.2") // or your Room version
    androidTestImplementation ("androidx.test:core:1.7.0")
    androidTestImplementation ("androidx.test.ext:junit:1.3.0")
    androidTestImplementation (libs.junit)
}
