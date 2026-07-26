import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

/** URL API lue depuis mobile_app/local.properties → quest.api.base.url */
fun resolveApiBaseUrl(): String {
    val props = Properties()
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { props.load(it) }
    }
    val raw = props.getProperty("quest.api.base.url")?.trim().orEmpty()
    val url = raw.ifEmpty { "http://10.0.2.2:8000" }
    return if (url.endsWith("/")) url.dropLast(1) else url
}

android {
    namespace = "com.quest.app"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.quest.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.2.0"
        buildConfigField("String", "API_BASE_URL", "\"${resolveApiBaseUrl()}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)

    // Carte OpenStreetMap (lieu du jour)
    implementation(libs.osmdroid.android)

    // Photos Wikipedia
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Position pour trouver la ville et calculer la distance
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)
}
