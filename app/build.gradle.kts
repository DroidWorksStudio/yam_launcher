plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-android")
    alias(libs.plugins.ksp)
}

// Top of build.gradle.kts
val major = 1
val minor = 8
val patch = 0
val build = 1

val type = 0 // 1=beta, 2=alpha else=production

val baseVersionName = "$major.$minor.$patch.$build"

val versionCodeInt =
    (String.format("%02d", major) + String.format("%02d", minor) + String.format(
        "%02d",
        patch
    ) + String.format("%02d", build)).toInt()

val versionNameStr = when (type) {
    1 -> "$baseVersionName-beta"
    2 -> "$baseVersionName-alpha"
    else -> baseVersionName
}

android {
    namespace = "eu.ottop.yamlauncher"
    compileSdk = 36

    defaultConfig {
        applicationId = "eu.ottop.yamlauncher"
        minSdk = 28
        targetSdk = 36
        versionCode = versionCodeInt
        versionName = versionNameStr
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_name", "YAM Launcher Dev")
            resValue("string", "app_version", versionNameStr)
            resValue("string", "empty", "")
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_name", "YAM Launcher")
            resValue("string", "app_version", versionNameStr)
            resValue("string", "empty", "")
        }
    }

    applicationVariants.all {
        if (buildType.name == "release") {
            outputs.all {
                val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
                if (output?.outputFileName?.endsWith(".apk") == true) {
                    output.outputFileName =
                        "${defaultConfig.applicationId}_v${defaultConfig.versionName}-Signed.apk"
                }
            }
        }
        if (buildType.name == "debug") {
            outputs.all {
                val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
                if (output?.outputFileName?.endsWith(".apk") == true) {
                    output.outputFileName =
                        "${defaultConfig.applicationId}_v${defaultConfig.versionName}-Debug.apk"
                }
            }
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
    }

    packaging {
        // Keep debug symbols for specific native libraries
        // found in /app/build/intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib
        jniLibs {
            keepDebugSymbols.add("libandroidx.graphics.path.so") // Ensure debug symbols are kept
        }
    }
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // Core libraries
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.preference.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.biometric.ktx)

    // UI Components
    implementation(libs.constraintlayout.compose)
    implementation(libs.activity.compose)

    // Jetpack Compose
    implementation(libs.compose.material) // Compose Material Design
    implementation(libs.compose.android) // Android
    implementation(libs.compose.animation) // Animations
    implementation(libs.compose.ui) // Core UI library
    implementation(libs.compose.foundation) // Foundation library
    implementation(libs.compose.ui.tooling) // UI tooling for previews
}