plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

ktlint {
    version.set("1.0.1")
}

detekt {
    config.setFrom(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
}

android {
    namespace = "io.github.victorbezerradev.connectifyflow"
    //noinspection GradleDependency
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.victorbezerradev.connectifyflow"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters +=
                listOf(
                    "armeabi-v7a",
                    "arm64-v8a",
                    "x86",
                    "x86_64",
                )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
    }

    lint {
        abortOnError = true
        warningsAsErrors = true
        checkReleaseBuilds = true
        baseline = file("lint-baseline.xml")
        disable += "AndroidGradlePluginVersion"
        disable += "GradleDependency"
        disable += "NewerVersionAvailable"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material3)
    implementation(libs.material)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.okhttp)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.rive.android)
    implementation(libs.retrofit)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.serialization.converter)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.ui.tooling)
}
