import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.clinician"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.clinician"
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
    packagingOptions{
        exclude ("META-INF/*")
    }

}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.0.3")
    implementation("com.google.android.material:material:1.11.0")
    implementation ("androidx.emoji:emoji:1.1.0")
    implementation ("androidx.emoji:emoji-bundled:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.amazonaws:aws-android-sdk-s3:2.17.1")
    implementation("com.amazonaws:aws-android-sdk-mobile-client:2.6.+@aar")
    implementation("com.amazonaws:aws-android-sdk-s3:2.7.4")
    implementation ("com.amazonaws:aws-android-sdk-cognito:2.6.17")
    implementation ("com.amazonaws:aws-android-sdk-core:2.16.2")
    implementation ("com.amazonaws:aws-android-sdk-s3:2.16.12")
    testImplementation("junit:junit:4.13.2")
    implementation ("com.github.AnyChart:AnyChart-Android:1.1.5")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}