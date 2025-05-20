plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.androidx.navigation.safeargs)
}

val (major, minor, patch) = "0.5.6".split(".").map { it.toInt() }

android {
    namespace = "fr.mastersd.sime.scanlib"
    compileSdk = 35

    defaultConfig {
        applicationId = "fr.mastersd.sime.scanlib"
        minSdk = 24
        targetSdk = 35
        versionName = "$major.$minor.$patch"
        versionCode = major * 10_000 + minor * 100 + patch

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(libs.coil)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.exifinterface)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.fragment)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.truth)
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    testImplementation(libs.mockito.core)
    testImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.core)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.gson)

    //ML Kit OCR : reconnaissance de texte avec TextRecognition
    implementation("com.google.mlkit:text-recognition:16.0.0")

    //Coroutines Kotlin pour await() et Dispatchers
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")



}