try {
    val pe = Class.forName("java.lang.ProcessEnvironment")
    val field = pe.getDeclaredField("theEnvironment").apply { isAccessible = true }
    val map = field.get(null) as? MutableMap<*, *>
    map?.keys?.removeIf { it.toString() == "ANDROID_PREFS_ROOT" }
} catch (_: Exception) {}

rootProject.name = "Capitular"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":androidApp")
