plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    //To use Kotlin JSON serialization library.
    //https://kotlinlang.org/docs/serialization.html#add-plugins-and-dependencies
    kotlin("plugin.serialization") version "2.1.0"
}

android {
    namespace = "com.github.mydeardoctor.yandexmusicalarmclock"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.github.mydeardoctor.yandexmusicalarmclock"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //To use Yandex OAuth.
        //https://yandex.ru/dev/id/doc/ru/mobileauthsdk/android/3.1.3/sdk-android-use
        manifestPlaceholders["YANDEX_CLIENT_ID"] = "7c19e068996e42ec9762e9e3986706bb"
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
        compose = true
    }
}

dependencies {
    //Default generated dependencies.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Manually added dependencies.
    //To use ViewModel.
    //https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel
    //https://developer.android.com/develop/ui/compose/libraries#viewmodel
    //https://developer.android.com/jetpack/androidx/releases/lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    //To use Yandex OAuth.
    //https://yandex.ru/dev/id/doc/ru/mobileauthsdk/android/3.1.3/sdk-android-install
    implementation("com.yandex.android:authsdk:3.1.3")

    //To use Kotlin coroutines.
    //https://developer.android.com/kotlin/coroutines
    //https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    //To use HTTP client library.
    //https://square.github.io/okhttp/#requirements
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    //To use Kotlin JSON serialization library.
    //https://kotlinlang.org/docs/serialization.html#add-plugins-and-dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    //To use ExoPlayer.
    //https://developer.android.com/media/media3/exoplayer/hello-world
    implementation("androidx.media3:media3-exoplayer:1.5.1")
}


//Creates custom Gradle task called "getGitVersion".
//This task runs "get_git_version.ps1" PowerShell script.
tasks.register<Exec>("getGitVersion")
{
    commandLine(
        "powershell",
        "-ExecutionPolicy",
        "Bypass",
        "-File",
        "${projectDir}/get_git_version.ps1")
}

//Makes "preBuild" Gradle task dependent on "getGitVersion" Gradle task.
//"getGitVersion" Gradle task runs before "preBuild" Gradle task.
tasks.named("preBuild")
{
    dependsOn("getGitVersion")
}