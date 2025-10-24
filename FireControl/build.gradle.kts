plugins {
    `java-library`
}

group = "com.firecontrol"
version = "1.0.0"
description = "Lightweight plugin to control fire spread on Spigot servers"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.18.1-R0.1-SNAPSHOT")
}

tasks {
    jar {
        archiveFileName.set("FireControl-${project.version}.jar")
    }
    
    processResources {
        val props = mapOf(
            "version" to version,
            "description" to description
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
} 