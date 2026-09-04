plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val pingwinStoreFile = providers.gradleProperty("PINGWIN_STORE_FILE")
val pingwinStorePassword = providers.gradleProperty("PINGWIN_STORE_PASSWORD")
val pingwinKeyAlias = providers.gradleProperty("PINGWIN_KEY_ALIAS")
val pingwinKeyPassword = providers.gradleProperty("PINGWIN_KEY_PASSWORD")

android {
    namespace = "com.pingwin.vpn"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.pingwin.vpn"
        minSdk = 24
        targetSdk = 37
        versionCode = 4
        versionName = "0.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (pingwinStoreFile.isPresent && pingwinStorePassword.isPresent && pingwinKeyAlias.isPresent && pingwinKeyPassword.isPresent) {
            create("release") {
                storeFile = file(pingwinStoreFile.get())
                storePassword = pingwinStorePassword.get()
                keyAlias = pingwinKeyAlias.get()
                keyPassword = pingwinKeyPassword.get()
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            optimization {
                enable = false
            }
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include(
                "arm64-v8a",
                "armeabi-v7a"
            )
            isUniversalApk = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.work:work-runtime:2.11.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.joaomgcd:taskerpluginlibrary:0.4.10")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
	implementation(files("libs/libbox.aar"))
}
