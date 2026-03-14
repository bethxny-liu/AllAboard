plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    // Needed so @Serializable classes (e.g., shared User) have serializers at runtime
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    application
}

group = "org.allaboard.project"
version = "1.0.0"
application {
    mainClass.set("org.allaboard.project.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation("io.ktor:ktor-server-content-negotiation-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${libs.versions.ktor.get()}")

    // JWT authentication (JWKS / ES256)
    implementation("io.ktor:ktor-server-auth:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-auth-jwt:${libs.versions.ktor.get()}")
    implementation("com.auth0:jwks-rsa:0.22.1")

    // .env file loading
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.0")

    // Supabase Postgrest client (server-side, uses service_role key)
    implementation(platform("io.github.jan-tennert.supabase:bom:${libs.versions.supabase.get()}"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")

    // Ktor client engine for the server-side Supabase client
    implementation("io.ktor:ktor-client-cio:${libs.versions.ktor.get()}")

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}