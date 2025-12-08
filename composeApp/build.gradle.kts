import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildkonfig)
}

// =============================================================================
// Local Properties - Single load for reuse across the entire build script
// =============================================================================
val localProperties: Properties by lazy {
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }
}

// Helper function to get property with Debug/Release fallback
fun Properties.getPropertyWithFallback(baseName: String, isRelease: Boolean): String {
    val suffix = if (isRelease) "_RELEASE" else "_DEBUG"
    return getProperty("$baseName$suffix") ?: getProperty(baseName) ?: ""
}

// Determine if this is a release build based on Gradle task names
val isReleaseBuild: Boolean by lazy {
    gradle.startParameter.taskNames.any { task ->
        task.contains("release", ignoreCase = true) ||
                task.contains("Release")
    }
}

kotlin {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)

            // Ktor Client for Android (OkHttp engine supports WebSockets)
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            // Ktor Client for iOS
            implementation(libs.ktor.client.darwin)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended) // Material Icons
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)

            // Navigation
            implementation(libs.navigation.compose)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.websockets)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.compose.auth)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.realtime)

            // Secure Storage
            implementation(libs.kvault)

            // Coil - Image Loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
        }
    }

    sourceSets.configureEach {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}

android {
    namespace = "com.apptolast.spaindecides"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    // Signing configurations for release builds
    signingConfigs {
        create("release") {
            // Read signing credentials from local.properties
            val storeFilePath = localProperties.getProperty("signing.storeFile")
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = localProperties.getProperty("signing.storePassword")
                keyAlias = localProperties.getProperty("signing.keyAlias")
                keyPassword = localProperties.getProperty("signing.keyPassword")
            } else {
                // Log warning if signing properties are not found
                logger.warn("⚠️  Signing properties not found in local.properties. Release builds will not be signed.")
            }
        }
    }

    defaultConfig {
        applicationId = "com.apptolast.spaindecides"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.0.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Disable Android BuildConfig generation - we use BuildKonfig instead
    buildFeatures {
        buildConfig = false
    }

    buildTypes {
        getByName("debug") {
            // Debug-specific configuration (no buildConfigField - handled by BuildKonfig)
        }

        getByName("release") {
            // Enable code shrinking, obfuscation, and optimization
            isMinifyEnabled = true
            isShrinkResources = true

            // Apply ProGuard rules
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Apply signing configuration
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

// =============================================================================
// BuildKonfig Configuration
// Centralizes all API keys and secrets for commonMain (cross-platform)
// =============================================================================
buildkonfig {
    packageName = "com.apptolast.spaindecides"

    defaultConfigs {
        // Supabase Configuration
        buildConfigField(
            STRING,
            "SUPABASE_URL",
            localProperties.getPropertyWithFallback("SUPABASE_URL", isReleaseBuild)
        )
        buildConfigField(
            STRING,
            "SUPABASE_ANON_KEY",
            localProperties.getPropertyWithFallback("SUPABASE_ANON_KEY", isReleaseBuild)
        )

        // Google OAuth Configuration
        buildConfigField(
            STRING,
            "GOOGLE_WEB_CLIENT_ID",
            localProperties.getPropertyWithFallback("GOOGLE_WEB_CLIENT_ID", isReleaseBuild)
        )

        // EmailJS Configuration (for content reporting)
        buildConfigField(
            STRING,
            "EMAILJS_SERVICE_ID",
            localProperties.getProperty("EMAILJS_SERVICE_ID", "")
        )
        buildConfigField(
            STRING,
            "EMAILJS_TEMPLATE_ID",
            localProperties.getProperty("EMAILJS_TEMPLATE_ID", "")
        )
        buildConfigField(
            STRING,
            "EMAILJS_PUBLIC_KEY",
            localProperties.getProperty("EMAILJS_PUBLIC_KEY", "")
        )
    }
}
