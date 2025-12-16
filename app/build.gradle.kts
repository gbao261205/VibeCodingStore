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
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Gson Converter - Chuyển đổi JSON sang Java Object
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 2. TẢI VÀ HIỂN THỊ HÌNH ẢNH
    // Glide - Thư viện tải ảnh
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // 3. KIẾN TRÚC ỨNG DỤNG (KHUYẾN NGHỊ)
    // ViewModel - Giữ dữ liệu UI không bị ảnh hưởng bởi vòng đời (lifecycle)
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    // LiveData - Tạo data có thể quan sát được
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.cloudinary:cloudinary-android:2.2.0")
}