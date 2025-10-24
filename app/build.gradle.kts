import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.selfgrowthfund.sgf"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.selfgrowthfund.sgf"
        minSdk = 26
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
        dataBinding = false
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

    // ✅ Fix duplicate META-INF conflicts
    packaging {
        resources {
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

// ✅ Global excludes (removes unwanted Jakarta XML Bind)
configurations.all {
    exclude(group = "jakarta.xml.bind", module = "jakarta.xml.bind-api")
}

dependencies {

    // ---------- Compose ----------
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.compose.tooling.preview)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    debugImplementation(libs.compose.tooling)

    // ---------- AndroidX Core ----------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.foundation.layout)

    // ---------- Room ----------
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.ksp)

    // ---------- Hilt ----------
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.fragment)
    implementation(libs.hilt.navigation.compose)

    // ---------- Lifecycle ----------
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)

    // ---------- Coroutines ----------
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // ---------- Networking ----------
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)

    // ---------- Firebase ----------
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    // ---------- Logging ----------
    implementation(libs.timber)

    // ---------- UI / Material ----------
    implementation(libs.material)
    implementation(libs.material.icons.extended)
    implementation(libs.accompanist.systemuicontroller)

    // ---------- Security / Auth ----------
    implementation(libs.androidx.security)
    implementation(libs.androidx.biometric)
    implementation(libs.google.auth)

    // ---------- PDF / Documents ----------
    implementation(libs.itext7.kernel)
    implementation(libs.itext7.layout)
    implementation(libs.itext7.core)

    // ---------- Desugaring ----------
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // ---------- Testing ----------
    // ✅ Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.room:room-testing:2.7.2")

    // ✅ Instrumentation Tests
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("com.google.truth:truth:1.4.2")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation("androidx.room:room-testing:2.7.2")
}