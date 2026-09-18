plugins {
    alias(libs.plugins.neoforge.moddev)
    alias(libs.plugins.jsonlang)
    alias(libs.plugins.modpublish)
}

val (minVersion, maxVersion) = (property("mod.minecraft") as String).split('-')

val requiredJava = when {
    stonecutter.eval(minVersion, ">=1.20.5") -> JavaVersion.VERSION_21
    stonecutter.eval(minVersion, ">=1.18") -> JavaVersion.VERSION_17
    stonecutter.eval(minVersion, ">=1.17") -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

tasks.named<ProcessResources>("processResources") {
    fun prop(name: String) = project.property(name) as String

    val props = HashMap<String, String>().apply {
        this["version"] = prop("mod.version")
        this["java"] = "[${requiredJava},)"
    }

    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
        expand(props)
    }
}

version = "${property("mod.version")}+${property("mod.minecraft")}-neoforge"
base.archivesName = property("mod.id") as String

jsonlang {
    languageDirectories = listOf("assets/${property("mod.id")}/lang")
    prettyPrint = true
}

neoForge {
    version = property("deps.neoforge") as String
    validateAccessTransformers = true

    runs {
        register("client") {
            jvmArgument("-Dmixin.debug.export=true")
            gameDirectory = file("../../run/")
            client()
        }
    }

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
    sourceSets["main"].resources.srcDir("src/main/generated")
}

repositories {
    maven("https://maven.isxander.dev/releases") { name = "IsXander" }
    maven("https://thedarkcolour.github.io/KotlinForForge/") { name = "KotlinForForge" }
}

dependencies {
    compileOnly(libs.mixinextras.common)
    annotationProcessor(libs.mixinextras.common)

    findProperty("deps.yacl")?.let {
        implementation("dev.isxander:yet-another-config-lib:$it")
    }
}

tasks {
    processResources {
        exclude("**/fabric.mod.json", "**/*.classtweaker", "**/mods.toml", "**/pack.mcmeta")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
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
    displayName = "${property("mod.name")} v${property("mod.version")} for ${property("mod.minecraft")} NeoForge"
    version = "${property("mod.version")}+${property("mod.minecraft")}-neoforge"
    changelog = provider { rootProject.file("CHANGELOG-LATEST.md").readText() }
    modLoaders.add("neoforge")

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        environment = CLIENT_ONLY
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
            client = true
            server = false
            minecraftVersionRange {
                start = minVersion
                end = maxVersion
            }
        }
    }
}
