pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    versionCatalogs {
        create("libpebble") {
            from(files("libs/libpebble3/gradle/libs.versions.toml"))
        }
    }
}
rootProject.name = "FossilNotify"
include(":app")


// TODO:  must comment `include(":composeApp")` in libs/libpebble3/settings.gradle.kts to compile on windows
includeBuild("libs/libpebble3") {
    dependencySubstitution {
        // "project(':libpebble3')" refers to the module path inside libpebbleroot
        substitute(module("io.rebble.libpebblecommon:libpebble3"))
            .using(project(":libpebble3"))
    }
}