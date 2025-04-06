plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")

}

android {
    namespace = "com.example.freshmarket"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.freshmarket"
        minSdk = 28
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}


dependencies {
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.material.v190)
    //navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
//анимация лотти
    implementation(libs.lottie)
    // room
    implementation(libs.androidx.room.ktx)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation ("com.google.firebase:firebase-firestore-ktx:25.1.3")
    //google auth
    implementation(libs.play.services.auth)
    implementation ("com.google.firebase:firebase-messaging-ktx:24.1.1")
//воркер
    implementation ("androidx.work:work-runtime-ktx:2.10.0")


    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.google.android.gms:play-services-maps:19.1.0")
    // Для HTTP-запросов (OkHttp)
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    // Для парсинга JSON (можно и Moshi/Gson, тут просто покажу пример с org.json)
    // Но можно использовать Gson:
    implementation ("com.google.code.gson:gson:2.11.0")
    // Или org.json, который уже встроен в Android, тогда отдельная зависимость не нужна
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")

}