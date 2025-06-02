plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    }

android {
    namespace = "com.example.neurology_project_android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.neurology_project_android"
        minSdk = 24
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

            buildConfigField("String", "BASE_API_URL", "\"https://videochat-signaling-app.ue.r.appspot.com\"")
            buildConfigField("String", "BASE_WS_API_URL", "\"wss://videochat-signaling-app.ue.r.appspot.com\"")
            buildConfigField("int", "PORT", "443")
            buildConfigField("boolean", "SECURE", "true") // yes use HTTPS
            buildConfigField("String", "API_POST_URL","\"https://videochat-signaling-app.ue.r.appspot.com/key=peerjs/post\"")
            buildConfigField("String", "API_GET_PEERS_URL","\"https://videochat-signaling-app.ue.r.appspot.com/key=peerjs/peers\"")
            signingConfig = signingConfigs.getByName("debug")
        }

        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "BASE_API_URL", "\"https://videochat-signaling-app.ue.r.appspot.com\"")
            buildConfigField("String", "BASE_WS_API_URL", "\"wss://videochat-signaling-app.ue.r.appspot.com\"")
            buildConfigField("int", "PORT", "443")
            buildConfigField("boolean", "SECURE", "true") // yes use HTTPS
            buildConfigField("String", "API_POST_URL","\"https://videochat-signaling-app.ue.r.appspot.com/key=peerjs/post\"")
            buildConfigField("String", "API_GET_PEERS_URL","\"https://videochat-signaling-app.ue.r.appspot.com/key=peerjs/peers\"")
            signingConfig = signingConfigs.getByName("debug")
        }

        // Your custom 'localDebug' build type
        create("localDebug") {
            // Inherit all properties from the 'debug' build type first
            // This is equivalent to 'initWith debug' in Groovy DSL
            // You can also manually set properties if you don't want to inherit all
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".local" // e.g., com.example.myapp.local
            versionNameSuffix = "-local"

            // Override BASE_API_URL to point to your local development server
            // 10.0.2.2 is the special IP for your host machine's loopback on Android Emulator
            buildConfigField("String", "BASE_API_URL", "\"localhost\"")
            buildConfigField("String", "BASE_WS_API_URL", "\"ws://localhost:9000\"")
            buildConfigField("int", "PORT", "9000")
            buildConfigField("boolean", "SECURE", "false") // no use HTTPS
            buildConfigField("String", "API_POST_URL","\"https://localhost:9000/key=peerjs/post\"")
            buildConfigField("String", "API_GET_PEERS_URL","\"http://localhost:9000/key=peerjs/peers\"")

            // If using a physical device on your local network, replace with your actual local IP:
            // buildConfigField("String", "BASE_API_URL", "\"http://192.168.1.XX:8080\"")

            // Add a custom flag specific to local development
            buildConfigField("Boolean", "ENABLE_LOCAL_MOCK_DATA", "true")

            // Override the app name for local debug
            resValue("string", "app_name", "My App (Local Debug)")
            signingConfig = signingConfigs.getByName("debug")

            // You can also set a different signing config if needed, though less common for local debug
            // signingConfig = signingConfigs.getByName("debug") // assuming you have a debug signing config
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("io.github.webrtc-sdk:android:125.6422.06.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.room:room-runtime:2.5.0")
    implementation(libs.androidx.camera.core)
    ksp("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    implementation ("com.github.zjupure:webpdecoder:2.6.4.16.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.media3.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}