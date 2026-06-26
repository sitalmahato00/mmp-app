plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // Pure Kotlin logic - no Android dependencies
    implementation(libs.kotlinx.serialization.json)
    implementation("com.google.code.gson:gson:2.11.0")
    // Add Coroutines if needed for flows in domain
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

kotlin {
    jvmToolchain(17)
}

