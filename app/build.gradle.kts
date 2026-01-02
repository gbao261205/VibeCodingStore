import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.vibecoding.flowerstore"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.vibecoding.flowerstore"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- CẤU HÌNH ĐỌC KEY TỪ LOCAL.PROPERTIES ---
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        // Lấy giá trị từ local.properties, nếu không có thì dùng giá trị mặc định (hoặc chuỗi rỗng)
        val partnerCode = properties.getProperty("MOMO_PARTNER_CODE", "")
        val accessKey = properties.getProperty("MOMO_ACCESS_KEY", "")
        val secretKey = properties.getProperty("MOMO_SECRET_KEY", "")

        // Tạo biến BuildConfig để dùng trong Java
        buildConfigField("String", "MOMO_PARTNER_CODE", "\"$partnerCode\"")
        buildConfigField("String", "MOMO_ACCESS_KEY", "\"$accessKey\"")
        buildConfigField("String", "MOMO_SECRET_KEY", "\"$secretKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Bật tính năng BuildConfig
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("io.socket:socket.io-client:2.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // MoMo Payment
    implementation(project(":momo_partner_sdk"))
}