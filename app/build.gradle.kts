import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(28)

    defaultConfig {
        applicationId = "tbrs.ocr"
        minSdkVersion(14)
        targetSdkVersion(28)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main").java.srcDir("src/main/kotlin")
        getByName("test").java.srcDir("src/test/kotlin")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles("proguard.cfg")
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(files("libs/google-api-translate-java-0.98-mod2.jar"))
    implementation(files("libs/json_simple-1.1.jar"))
    implementation(files("libs/jtar-1.0.4.jar"))
    implementation(files("libs/microsoft-translator-java-api-0.6-mod.jar"))

    implementation("com.rmtheis:tess-two:9.0.0")
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation("androidx.core:core-ktx:1.1.0-alpha05")

    testImplementation("androidx.test.ext:junit:1.1.0")
    testImplementation("androidx.test:rules:1.1.1")
    testImplementation("androidx.test:runner:1.1.1")
    testImplementation("com.google.truth:truth:0.43")
    testImplementation("org.robolectric:robolectric:4.2.1")
}
