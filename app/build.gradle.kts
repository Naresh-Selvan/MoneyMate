plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    kotlin("android")
}

android {
    namespace = "com.moneymate.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.moneymate.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.2"

        buildConfigField("String", "LICENSE_SERVER_URL", "\"https://your-license-server.com/api/verify\"")
        buildConfigField("String", "SUPPORT_PHONE", "\"+910000000000\"")
        buildConfigField("String", "UPI_ID", "\"moneymate@upi\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("moneymate-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: "moneymate"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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

    kotlinOptions {
        jvmTarget = "11"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("com.google.dagger:hilt-android:2.55")
    ksp("com.google.dagger:hilt-compiler:2.55")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // ── Phase 5: Export dependencies ──
    implementation("com.itextpdf:itext7-core:7.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5") {
        exclude(group = "org.apache.xmlbeans", module = "xmlbeans")
        exclude(group = "com.github.virtuald", module = "curvesapi")
        exclude(group = "org.apache.commons", module = "commons-math3")
    }
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.slf4j:slf4j-nop:2.0.9")
    implementation("androidx.core:core-ktx:1.15.0")

    // ── Phase 6: Notifications & WorkManager ──
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ── Settings Enhancements: Ktor HTTP client for license verification ──
    implementation("io.ktor:ktor-client-android:2.3.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
}