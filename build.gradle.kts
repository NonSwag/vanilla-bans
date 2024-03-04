import io.papermc.hangarpublishplugin.model.Platforms
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
}

group = "net.thenextlvl.bans"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

repositories {
    mavenCentral()
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly("net.thenextlvl.core:annotations:2.0.1")
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    implementation("net.thenextlvl.core:files:1.0.3")
    implementation("net.thenextlvl.core:i18n:1.0.13")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.shadowJar {
    relocate("org.bstats", "net.thenextlvl.bans.bstats")
    minimize()

}


paper {
    name = "VanillaBans"
    main = "net.thenextlvl.bans.BanPlugin"
    apiVersion = "1.19"
    website = "https://thenextlvl.net"
    authors = listOf("NonSwag")

    foliaSupported = true

    permissions {
        register("bans.commands.admin") {
            this.children = listOf(
                    "tweaks.command.ban",
                    "tweaks.command.ban-ip",
                    "tweaks.command.banlist",
                    "tweaks.command.pardon",
                    "tweaks.command.pardon-ip"
            )
        }
    }
}

val versionString: String = project.version as String
val isRelease: Boolean = !versionString.contains("-pre")

hangarPublish { // docs - https://docs.papermc.io/misc/hangar-publishing
    publications.register("plugin") {
        id.set("VanillaBans")
        version.set(project.version as String)
        channel.set(if (isRelease) "Release" else "Snapshot")
        if (extra.has("HANGAR_API_TOKEN"))
            apiKey.set(extra["HANGAR_API_TOKEN"] as String)
        else apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                val versions: List<String> = (property("paperVersion") as String)
                        .split(",")
                        .map { it.trim() }
                platformVersions.set(versions)
                dependencies {
                    url("LuckPerms", "https://luckperms.net/") {
                        required.set(false)
                    }
                }
            }
        }
    }
}