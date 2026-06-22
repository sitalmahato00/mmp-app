plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Pure Kotlin logic - no Android dependencies
    implementation(libs.kotlinx.serialization.json)
    // Add Coroutines if needed for flows in domain
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}
