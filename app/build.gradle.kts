plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "nodomain.freeyourgadget.fossilnotify"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "nodomain.freeyourgadget.fossilnotify"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf("VERSION_${libs.versions.jvm.toolchain.get()}")
        targetCompatibility = JavaVersion.valueOf("VERSION_${libs.versions.jvm.toolchain.get()}")
    }
    kotlin {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material)

    implementation(libs.gson)

    implementation ("com.getpebble:pebblekit:4.0.1")
    implementation("io.rebble.libpebblecommon:libpebble3")
}
