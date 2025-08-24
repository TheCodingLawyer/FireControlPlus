import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}



repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/repository/maven-public/")
    }
}

dependencies {
    implementation(gradleApi())
    implementation("com.github.jengelman.gradle.plugins:shadow:6.1.0")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.21.0")
    implementation("org.spongepowered:spongegradle-plugin-development:2.0.0")
}