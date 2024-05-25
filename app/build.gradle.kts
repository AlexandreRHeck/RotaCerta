plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.rotacerta"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rotacerta"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    //FireBase Dependecias
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    implementation("com.google.firebase:firebase-analytics")//Firebase Analytics
    implementation("com.google.firebase:firebase-auth-ktx")//Auth Analytics
    implementation("com.google.firebase:firebase-firestore-ktx")//firestore Firebase
    implementation("com.google.firebase:firebase-storage-ktx")//firestore Firebase
    implementation("com.google.firebase:firebase-database")//FireBaseDataBase


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}