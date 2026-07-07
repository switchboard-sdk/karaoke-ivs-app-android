plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.synervoz.switchboardsampleapp.karaokewithivs"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.synervoz.switchboardsampleapp.karaokewithivs"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.synervoz.switchboard.switchboardsdk)
    implementation(libs.synervoz.switchboard.extensions.superpowered)
    implementation(libs.synervoz.switchboard.extensions.audioeffects)
    implementation(libs.synervoz.switchboard.extensions.amazonivs)
    // Amazon IVS broadcast SDK, "stages" artifact for the real-time (Stage) example.
    implementation("com.amazonaws:ivs-broadcast:${libs.versions.amazonIvs.get()}:stages@aar")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
