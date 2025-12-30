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


// TODO: comment these modules in libs/libpebble3/settings.gradle.kts - they are core app components
//include(":composeApp")
//include(":pebble")
//include(":util")
//include(":experimental")

//includeBuild("libs/libpebble3") {
//    dependencySubstitution {
//        substitute(module("io.rebble.libpebblecommon:libpebble3"))
//            .using(project(":libpebble3"))
//    }
//}