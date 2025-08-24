import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

group = "com.rabbitcompany"
version = "5.15.0-Enhanced"
description = "AdminGUI Premium with Enhanced BanManager Integration"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
    maven("https://hub.spigotmc.org/nexus/content/groups/public/") // Spigot
    maven("https://oss.sonatype.org/content/groups/public/") // Sonatype
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") // BanManager
    maven("https://jitpack.io") // JitPack
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://nexus.scarsz.me/content/groups/public/") // DiscordSRV
}

dependencies {
    // Use Paper API only for 1.21+ compatibility
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    
    // Essential plugin dependencies with conflict exclusions
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.md-5:bungeecord-api:1.21-R0.1-SNAPSHOT")
    
    // Use local BanManager dependencies if available, otherwise use remote
    if (project.findProject(":BanManagerBukkit") != null) {
        compileOnly(project(":BanManagerBukkit"))
        compileOnly(project(":BanManagerCommon"))
    } else {
        compileOnly("me.confuser.banmanager:BanManagerBukkit:7.10.0-SNAPSHOT")
        compileOnly("me.confuser.banmanager:BanManagerCommon:7.10.0-SNAPSHOT")
    }
    
    // Shaded dependencies
    implementation("com.zaxxer:HikariCP:5.1.0")
}

tasks.named<Copy>("processResources") {
    val props = mapOf(
        "version" to project.version,
        "description" to project.description
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("unshaded")
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}

// Apply shadow plugin and configure existing shadowJar task
apply(plugin = "com.gradleup.shadow")

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    archiveFileName.set("AdminGUI-Premium-${project.version}.jar")
    
    // Relocate shaded dependencies
    relocate("com.zaxxer.hikari", "com.rabbitcompany.libraries.hikari")
    
    // Include only necessary dependencies
    dependencies {
        include(dependency("com.zaxxer:HikariCP"))
    }
    
    exclude("GradleStart**")
    exclude(".cache")
    exclude("LICENSE*")
    exclude("META-INF/services/**")
    exclude("META-INF/maven/**")
    
    minimize()
}

tasks.named("build") {
    dependsOn("shadowJar")
}

tasks.named("assemble") {
    dependsOn("shadowJar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
} 