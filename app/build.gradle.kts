plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.taskmanagement"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.taskmanagement"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Add room database
    implementation("androidx.room:room-runtime:2.5.0")
    annotationProcessor("androidx.room:room-compiler:2.5.0")

    // Add Gson to convert a list of string to a string and reverse
    implementation("com.google.code.gson:gson:2.8.6")

    //Picasso for loading and displaying image from external url
    implementation ("com.squareup.picasso:picasso:2.8")

    //Cloudinary for upload file
    implementation("com.cloudinary:cloudinary-android:2.3.1")

    //Google map
    implementation("com.google.android.gms:play-services-maps:18.0.1")
}