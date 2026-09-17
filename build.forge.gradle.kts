@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.neoforge.moddev.legacyforge)
    alias(libs.plugins.jsonlang)
    alias(libs.plugins.modpublish)
}

val (minVersion, maxVersion) = (property("mod.minecraft") as String).split('-')

val requiredJava = when {
    stonecutter.eval(minVersion, ">=1.18") -> JavaVersion.VERSION_17
    stonecutter.eval(minVersion, ">=1.17") -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

val packFormat = when {
    stonecutter.eval(minVersion, ">=1.20") -> 15
    stonecutter.eval(minVersion, ">=1.19") -> 9
    else -> 8
}
val dataFormat = when {
    stonecutter.eval(minVersion, ">=1.20") -> 15
    stonecutter.eval(minVersion, ">=1.19") -> 10
    else -> 8
}

tasks.named<ProcessResources>("processResources") {
    fun prop(name: String) = project.property(name) as String

    val props = HashMap<String, String>().apply {
        this["version"] = prop("mod.version")
        this["loaderVersion"] = prop("deps.forge-loader")
        this["forgeVersion"] = prop("deps.forge")
        this["minecraftVersionRange"] = "[$minVersion,$maxVersion]"
        this["packFormat"] = packFormat.toString()
        this["dataFormat"] = dataFormat.toString()
    }

    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(props)
    }
}

version = "${property("mod.version")}+${property("mod.minecraft")}-forge"
base.archivesName = property("mod.id") as String

jsonlang {
    languageDirectories = listOf("assets/${property("mod.id")}/lang")
    prettyPrint = true
}

legacyForge {
    version = "${stonecutter.current.version}-${property("deps.forge")}"
    validateAccessTransformers = true

    runs {
        register("client") {
            client()
            gameDirectory = file("../../run/")
        }
    }

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    compileOnly(libs.mixinextras.common)
    annotationProcessor(libs.mixinextras.common)
    implementation(jarJar("io.github.llamalad7:mixinextras-forge:${libs.versions.mixinextras.get()}")!!)
}

tasks {
    processResources {
        exclude("**/fabric.mod.json", "**/*.classtweaker", "**/neoforge.mods.toml")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

java {
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
}

publishMods {
    file = tasks.jar.map { it.archiveFile.get() }

    type = STABLE
    displayName = "${property("mod.name")} v${property("mod.version")} for ${property("mod.minecraft")} Forge"
    version = "${property("mod.version")}+${property("mod.minecraft")}-forge"
    changelog = provider { rootProject.file("CHANGELOG-LATEST.md").readText() }
    modLoaders.add("forge")

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersionRange {
            start = minVersion
            end = maxVersion
        }
    }

    val curseforgeId = property("publish.curseforge") as String
    if (curseforgeId.isNotBlank()) {
        curseforge {
            projectId = curseforgeId
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            minecraftVersionRange {
                start = minVersion
                end = maxVersion
            }
        }
    }
}
