    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.android)
        alias(libs.plugins.ksp)
        alias(libs.plugins.dagger.hilt.android)
    }

    android {
        namespace = "com.selfgrowthfund.sgf"
        compileSdk = 34

        defaultConfig {
            applicationId = "com.selfgrowthfund.sgf"
            minSdk = 24
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            vectorDrawables.useSupportLibrary = true
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
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        kotlinOptions {
            jvmTarget = "17"
        }

        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.11"
        }
    }

    dependencies {
        // Compose
        implementation(platform(libs.composeBom))
        implementation(libs.compose.ui)
        implementation(libs.compose.material3)
        implementation(libs.compose.foundation)
        implementation(libs.compose.runtime)
        implementation(libs.compose.ui.text)
        implementation(libs.compose.tooling.preview)
        debugImplementation(libs.compose.tooling)

        // AndroidX Core
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.activity.ktx)
        implementation(libs.androidx.fragment.ktx)


        // Room
        implementation(libs.room.runtime)
        implementation(libs.room.ktx)
        ksp(libs.room.compiler)
        testImplementation(libs.androidx.room.testing)

        // Lifecycle
        implementation(libs.androidx.lifecycle.viewmodel.ktx)
        implementation(libs.androidx.lifecycle.livedata.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.lifecycle.runtime.compose)

        // Coroutines
        implementation(libs.kotlinx.coroutines.android)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.kotlinx.coroutines.core)

        // Networking
        implementation(libs.retrofit)
        implementation(libs.okhttp)
        implementation(libs.okhttp.logging.interceptor)

        // Hilt
        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)

        // Desugaring
        coreLibraryDesugaring(libs.android.desugar.jdk.libs)

        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
    }