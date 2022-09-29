plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("net.kyori.indra") version "2.1.1"
    id("net.kyori.indra.git") version "2.1.1"
    id("io.freefair.lombok") version "6.5.1"
}

group = "cc.mewcraft.villagedefense"
version = "1.0-SNAPSHOT".decorateVersion()
description = "An addon for the plugin - VillageDefense"

repositories {
    mavenLocal {
        content {
            includeGroup("plugily.projects")
            includeGroup("me.tigerhix")
        }
    }
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        content {
            includeGroup("net.kyori")
        }
    }
    maven("https://repo.purpurmc.org/snapshots") {
        content {
            includeGroup("org.purpurmc.purpur")
        }
    }
    maven("https://jitpack.io") {
        content {
            includeGroup("com.github.MilkBowl")
            includeGroup("com.github.sgtcaze")
        }
    }
    maven("https://repo.minebench.de") {
        content {
            includeGroup("de.themoep.utils")
        }
    }
    maven("https://maven.plugily.xyz/releases") {
        content {
            includeGroup("me.tigerhix.lib")
        }
    }
}

dependencies {
    // API
    compileOnly("org.purpurmc.purpur", "purpur-api", "1.17.1-R0.1-SNAPSHOT")

    // Plugin libraries
    compileOnly("me.lucko", "helper", "5.6.10")
    compileOnly("plugily.projects", "villagedefense", "4.6.1")
    compileOnly("com.github.sgtcaze", "NametagEdit", "master-SNAPSHOT") {
        isTransitive = false
    }

    // Libraries that needs to be shaded
    implementation("de.themoep.utils", "lang-bukkit", "1.3-SNAPSHOT")
    implementation("org.spongepowered", "configurate-yaml", "4.1.2")
    implementation("org.jetbrains", "annotations", "23.0.0")
    val cloudVersion = "1.7.1"
    implementation("cloud.commandframework", "cloud-paper", cloudVersion)
    implementation("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)
}

indra {
    javaVersions().target(17)
}

bukkit {
    main = "cc.mewcraft.villagedefense.VillageDefenseAddon"
    name = project.name
    apiVersion = "1.17"
    authors = listOf("Nailm")
    depend = listOf("VillageDefense", "helper")
    softDepend = listOf("Vault")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        minimize()
        archiveFileName.set("${project.name}-${project.version}.jar")
        sequenceOf(
            "org.slf4j",
            "org.jetbrains",
            "cloud.commandframework",
            "io.leangen.geantyref",
            "de.themoep.utils",
            "org.spongepowered"
        ).forEach {
            relocate(it, "cc.mewcraft.villagedefense.lib.$it")
        }
    }
    processResources {
        val tokens = mapOf(
            "project.version" to project.version
        )
        inputs.properties(tokens)
    }
    task("deploy") {
        dependsOn(build)
        doLast {
            exec {
                workingDir("build/libs")
                commandLine("scp", jar.get().archiveFileName.get(), "mc-act:mc117_vda/plugins")
            }
        }
    }
}

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this