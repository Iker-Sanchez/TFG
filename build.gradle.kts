plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.tfg"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.tfg"
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
    // Retrofit: La herramienta que descarga los datos de la API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Converter GSON: El traductor que pasa de JSON (texto) a objetos Java
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Glide: La librería que descarga y muestra los escudos de los equipos
    implementation("github.com.bumptech.glide:glide:4.15.1")
}