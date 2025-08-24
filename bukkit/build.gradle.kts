import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	`java-library`
	`maven-publish`
	signing
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

	sourceSets {
		main {
			resources.setSrcDirs(listOf(
				file("${rootDir}/common/src/main/resources"),
				file("${projectDir}/src/main/resources")
			))
		}
	}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])

			pom {
				name.set("BanManagerBukkit")
				description.set("BanManager for Bukkit")
				url.set("https://github.com/BanManagement/BanManager/")
				licenses {
					license {
						name.set("Creative Commons Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales")
						url.set("https://github.com/BanManagement/BanManager/blob/master/LICENCE")
					}
				}
				developers {
					developer {
						id.set("confuser>")
						name.set("James Mortemore")
						email.set("jamesmortemore@gmail.com")
					}
				}
				scm {
					connection.set("scm:git:git://github.com/BanManagement/BanManager.git")
					developerConnection.set("scm:git:ssh://git@github.com/BanManagement/BanManager.git")
					url.set("https://github.com/BanManagement/BanManager/")
				}
			}
		}
	}
}

signing {
	if (project.findProperty("signingKey")?.toString()?.toBoolean() == true) {
		useInMemoryPgpKeys(findProperty("signingKey")?.toString(), findProperty("signingPassword")?.toString())

		sign(publishing.publications["mavenJava"])
	}
}

repositories {
	maven {
		name = "paper"
		url = uri("https://repo.papermc.io/repository/maven-public/")
	}
	maven {
		name = "ryzr-repo"
		url = uri("https://cdn.rawgit.com/Rayzr522/maven-repo/master/")
	}
	maven {
		name = "jitpack"
		url = uri("https://jitpack.io/")
	}
	maven {
		name = "Scarsz-Nexus"
		url = uri("https://nexus.scarsz.me/content/groups/public/")
	}
	maven {
		name = "placeholderapi"
		url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
	}
}

configurations {
	compileClasspath.get().extendsFrom(create("shadeOnly"))
}

dependencies {
	"api"(project(":BanManagerCommon")) {
		isTransitive = true
	}
	"compileOnly"("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT") {
		exclude("junit", "junit")
	}
	"compileOnly"("me.clip:placeholderapi:2.10.9")
	"compileOnly"("com.github.MilkBowl:VaultAPI:1.7")
	"shadeOnly"("org.bstats:bstats-bukkit:2.2.1")
	"shadeOnly"("org.slf4j:slf4j-simple:1.7.36")
	"shadeOnly"("redis.clients:jedis:5.1.4")
	"shadeOnly"("com.google.code.gson:gson:2.10.1")
}

// Expand placeholders in shared plugin.yml

tasks.named<Copy>("processResources") {
	val internalVersion = project.ext["internalVersion"]
	inputs.property("internalVersion", internalVersion)
	filesMatching("plugin.yml") {
		expand(
			mapOf(
				"internalVersion" to internalVersion,
				"mainPath" to "me.confuser.banmanager.bukkit.BMBukkitPlugin"
			)
		)
	}
}

tasks.named<Jar>("jar") {
	val projectVersion = project.version
	inputs.property("projectVersion", projectVersion)
	manifest {
		attributes("Implementation-Version" to projectVersion)
	}
}

tasks.named<ShadowJar>("shadowJar") {
	configurations = listOf(project.configurations["shadeOnly"], project.configurations["runtimeClasspath"])

	archiveBaseName.set("BanManagerBukkit")
	archiveClassifier.set("")
	archiveVersion.set("")

	dependencies {
		include(dependency(":BanManagerCommon"))
		include(dependency(":BanManagerLibs"))

		include(dependency("org.bstats:.*:.*"))
		include(dependency("org.slf4j:.*:.*"))

		relocate("org.bstats", "me.confuser.banmanager.common.bstats")
		relocate("org.slf4j", "me.confuser.banmanager.common.slf4j")
	}

	exclude("GradleStart**")
	exclude(".cache");
	exclude("LICENSE*")
	exclude("META-INF/services/**")
	exclude("META-INF/maven/**")
	exclude("org/intellij/**")
	exclude("org/jetbrains/**")
	exclude("bungeecord.yml")
	exclude("velocity.yml")

	minimize {
		exclude(dependency("org.bstats:.*:.*"))
		exclude(dependency("org.slf4j:.*:.*"))
	}
}

tasks.named("assemble").configure {
	dependsOn("shadowJar")
}
