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
            useSupportLibrary = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
        debug {
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
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)

    implementation(libs.gson)

//    implementation("io.rebble.libpebblecommon:libpebble3")
    implementation("com.github.pebble-dev.PebbleKitAndroid2:client:510b3ead6e")

}
