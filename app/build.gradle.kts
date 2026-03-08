plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.test_code"
    compileSdk = 36 // Android 16

    defaultConfig {
        applicationId = "com.example.test_code"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // 최신 표준인 Java 17 적용
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true  // BuildConfig 클래스 생성 활성화
    }
}

// Kotlin 2.0+ 현대적 설정 방식
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

dependencies {
    // --- [Hilt / DI] ---
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-android-compiler:2.59.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // --- [Jetpack Compose & Lifecycle] ---
    implementation(platform("androidx.compose:compose-bom:2025.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // --- [Navigation & Serialization] ---
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // --- [Network & Image Loading] ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")  // HTTP 로깅
    // Coil 3.x (Multiplatform 지원 최신 버전)
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

    // --- [DataStore] ---
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // --- [Paging 3] ---
    implementation("androidx.paging:paging-runtime:3.3.5")
    implementation("androidx.paging:paging-compose:3.3.5")

    // --- [Unit Testing (JUnit 5 + MockK + Turbine)] ---
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("app.cash.turbine:turbine:1.2.0")
    testImplementation("com.google.truth:truth:1.4.4")
    // Hilt Testing
    testImplementation("com.google.dagger:hilt-android-testing:2.59.2")
    kspTest("com.google.dagger:hilt-android-compiler:2.59.2")

    // --- [UI/Instrumentation Testing] ---
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.59.2")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.59.2")
}

// JUnit 5를 사용하기 위해 Gradle 엔진에 알림
tasks.withType<Test> {
    useJUnitPlatform()
}