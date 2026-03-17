import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.myopenclaw")
            
            // Fix for iOS crashes in Compose UI trace and assertions
            freeCompilerArgs += listOf(
                "-Xdisable-phases=RemoveRedundantCallsToStaticInitializersPhase",
                "-Xbinary=enableSafepointSignalHandling=false",
                "-Xoverride-konan-properties=minSupportedOs=17.0",
                "-linker-option", "-ld_classic"
            )

            // Required for SQLDelight native driver
            linkerOpts("-lsqlite3")
        }
    }

    sourceSets {
        // Apply opt-ins to all source sets
        all {
            languageSettings {
                optIn("kotlin.time.ExperimentalTime")
            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)

            // Credentials API for Google Sign-In
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services)
            implementation(libs.googleid)
            implementation(libs.play.services.auth)

            // Browser Custom Tabs for in-app browser (Stripe checkout)
            implementation(libs.androidx.browser)

            // SQLDelight Android driver
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)

            // SQLDelight Native driver for iOS
            implementation(libs.sqldelight.native.driver)
        }

        // Opt in to ExperimentalForeignApi for RevenueCat KMP iOS bindings
        named { it.lowercase().startsWith("ios") }.configureEach {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.navigation.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.material.icons.core)
            implementation(libs.material.icons.extended)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)

            // Supabase (KMP) - TODO: Add back when implementation is complete
            // implementation(libs.supabase.core)
            // implementation(libs.supabase.gotrue.kt)

            // RevenueCat (KMP)
            implementation(libs.revenuecat.purchases.kmp)

            // SQLDelight - Local database
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
    }
}

sqldelight {
    databases {
        create("MyOpenClawDatabase") {
            packageName.set("com.myopenclaw.data.local.cache")
        }
    }
}

android {
    namespace = "com.myopenclaw"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        targetSdk = 36
        versionCode = 18
        versionName = "3.0"
    }

    flavorDimensions += "product"
    productFlavors {
        create("myopenclaw") {
            dimension = "product"
            applicationId = "com.myopenclaw"
        }
        /*
        create("profitai") {
            dimension = "product"
            applicationId = "com.profitai.pro"
        }
        */
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("local.properties")
            val keystoreProperties = Properties()
            if (keystorePropertiesFile.exists()) {
                keystoreProperties.load(keystorePropertiesFile.inputStream())
            }

            storeFile = file("keyz.jks")
            storePassword = keystoreProperties.getProperty("KEYSTORE_PASSWORD") ?: System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = keystoreProperties.getProperty("KEY_ALIAS") ?: System.getenv("KEY_ALIAS") ?: ""
            keyPassword = keystoreProperties.getProperty("KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Configure source sets for product flavors
    sourceSets {
        getByName("myopenclaw") {
            kotlin.srcDirs("src/myopenclaw/kotlin")
            res.srcDirs("src/myopenclaw/res")
        }
        /*
        getByName("profitai") {
            kotlin.srcDirs("src/profitai/kotlin")
            res.srcDirs("src/profitai/res")
        }
        */
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.tooling)
}
