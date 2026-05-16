plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.compose) // Kotlin 2.0+ için bu şart
}

android {
    namespace = "com.example.finmathai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.finmathai"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val geminiKey = providers.gradleProperty("GEMINI_API_KEY").orNull ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Kotlin 2.0+ ile composeOptions (kotlinCompilerExtensionVersion) bloğu SİLİNDİ.
    // Artık 'kotlin-compose' plugin'i bu işi otomatik hallediyor.

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Material 3 & UI
    implementation(libs.androidx.material3)
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended")

    // Hilt (Dependency Injection)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler) // Metadata hatası alırsan burayı ksp yapabiliriz
    implementation(libs.hilt.navigation.compose)

    // Retrofit & Room
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // AI & Charts
    implementation(libs.google.generativeai)
    implementation(libs.mp.android.chart)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// KRİTİK: Hilt ve Kotlin 2.0 uyuşmazlığını gideren zorlama
configurations.all {
    resolutionStrategy {
        // Hilt'in eski metadata bağımlılıklarını güncel olanla zorla değiştiriyoruz
        force("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
    }
}