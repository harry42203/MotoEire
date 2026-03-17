plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
    jacoco
}

// Detekt configuration
detekt {
    config = files("detekt.yml")
    buildUponDefaultConfig = true
}

// JaCoCo for Android test coverage
jacoco {
    toolVersion = "0.8.10"
}

android {
    namespace = "com.example.motoeire"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.motoeire"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
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
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.02.02")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // Testing - Unit Tests
    testImplementation(libs.junit)
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-core:5.3.0")
    testImplementation("org.robolectric:robolectric:4.11.1")

    // Testing - Instrumented Tests (Android)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Code Analysis
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.5")
}

// JaCoCo coverage report for Android
tasks.register("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    doLast {
        val jacocoMem = "-Xmx512m"
        exec {
            executable = "java"
            args = listOf(
                jacocoMem,
                "-jar",
                "${project.extensions.getByType<JacocoPluginExtension>().toolVersion}",
                "report",
                "${buildDir}/outputs/unit_test_code_coverage/debugUnitTest/coverage.ec",
                "--classfiles", "${buildDir}/intermediates/classes/debug",
                "--sourcefiles", "src/main/kotlin",
                "--html", "${buildDir}/reports/jacoco"
            )
        }
    }
}