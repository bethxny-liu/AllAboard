import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

/*
 * Tests: `commonTest` (UI state / kotlin.test), `androidUnitTest` (ViewModels + Main dispatcher via kotlinx-coroutines-test).
 * Domain tests live in :shared. No instrumented Compose UI suite in this module.
 */

/** Reads KEY=value from [server/.env] (# comments ignored). */
fun loadEnvValue(f: File, key: String): String? {
    if (!f.exists()) return null
    for (line in f.readLines()) {
        val t = line.trim()
        if (t.isEmpty() || t.startsWith("#")) continue
        val i = t.indexOf('=')
        if (i <= 0 || t.substring(0, i).trim() != key) continue
        return t.substring(i + 1).trim().removeSurrounding("\"").removeSurrounding("'")
    }
    return null
}

val mapsStaticApiKey: String = run {
    val env = rootProject.file("server/.env")
    loadEnvValue(env, "MAPS_STATIC_API_KEY")
        ?: loadEnvValue(env, "GOOGLE_PLACES_API_KEY")
        ?: ""
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()
    listOf(iosArm64, iosSimulatorArm64).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            freeCompilerArgs += listOf(
                "-Xbinary=bundleId=org.allaboard.project"
            )
        }
    }

    sourceSets {
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.0.3")
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("io.ktor:ktor-client-okhttp:3.0.3")
            // Supabase BOM + compose-auth for Android OAuth deep-link handling
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.composeAuth)
            implementation(libs.supabase.auth)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.preview)
            implementation(compose.materialIconsExtended)
            implementation("org.jetbrains.compose.material:material-icons-core")
            implementation("cafe.adriel.voyager:voyager-navigator:1.1.0-beta02")
            implementation("cafe.adriel.voyager:voyager-transitions:1.1.0-beta02")
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.datetime)
            implementation("io.coil-kt.coil3:coil-compose:3.0.4")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.4")
            implementation(projects.shared)
        }
        // kotlin.test — same dependency as slide `testImplementation(kotlin("test"))`, in KMP `commonTest` form
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        // JVM Android unit tests: ViewModelTestBase uses Dispatchers.setMain (kotlinx-coroutines-test; pulls in coroutines-core)
        androidUnitTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "org.allaboard.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "org.allaboard.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        val escaped =
            mapsStaticApiKey
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
        buildConfigField("String", "MAPS_STATIC_API_KEY", "\"$escaped\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation("org.jetbrains.compose.ui:ui-tooling:${libs.versions.composeMultiplatform.get()}")
}
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
tasks.matching { it.name.contains("ios") && it.name.contains("Test") }
    .configureEach {
        enabled = false
    }
