plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.stegasuite.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.stegasuite.app"
        val runNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()
        val localTimestamp = (System.currentTimeMillis() / 1000 % 100000).toInt()
        val buildNumber = runNumber ?: (10000 + localTimestamp)
        versionCode = buildNumber
        versionName = if (runNumber != null) "1.2.0.${runNumber}" else "1.2.0.local.${localTimestamp}"
        minSdk = 26
        targetSdk = 35
    }

    signingConfigs {
        create("release") {
            val ksPath = System.getenv("KEYSTORE_FILE")
            val ksPass = System.getenv("KEYSTORE_PASSWORD")
            val ksAlias = System.getenv("KEY_ALIAS")
            val ksKeyPass = System.getenv("KEY_PASSWORD")
            if (ksPath != null && ksPass != null && ksAlias != null && ksKeyPass != null) {
                val ksFile = file(ksPath)
                if (ksFile.exists()) {
                    storeFile = ksFile
                    storePassword = ksPass
                    keyAlias = ksAlias
                    keyPassword = ksKeyPass
                }
            }
        }
    }

    buildTypes {
        release {
            if (signingConfigs.names.contains("release")) {
                val ksFile = signingConfigs.getByName("release").storeFile
                if (ksFile != null && ksFile.exists()) {
                    signingConfig = signingConfigs.getByName("release")
                }
            }
            isMinifyEnabled = false
            isShrinkResources = false
        }
        debug {
            if (signingConfigs.names.contains("release")) {
                val ksFile = signingConfigs.getByName("release").storeFile
                if (ksFile != null && ksFile.exists()) {
                    signingConfig = signingConfigs.getByName("release")
                }
            }
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.core:core-ktx:1.15.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
