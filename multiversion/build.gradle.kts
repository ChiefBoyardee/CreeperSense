plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.loom)
    alias(libs.plugins.publishing)
    alias(libs.plugins.blossom)
    alias(libs.plugins.ksp)
    alias(libs.plugins.fletchingtable.fabric)
    alias(libs.plugins.fletchingtable.neoforge)
}

class ModData {
    val id = property("mod.id").toString()
    val name = property("mod.name")
    val version = property("mod.version")
    val group = property("mod.group").toString()
    val description = property("mod.description")
    val source = property("mod.source")
    val issues = property("mod.issues")
    val license = property("mod.license").toString()
}

class Dependencies {
    val neoforgeVersion = providers.gradleProperty("deps.neoforge_version").orNull
    val fabricLoaderVersion = property("deps.fabric_loader_version").toString()
    val fabricApiVersion = providers.gradleProperty("deps.fabric_api_version").orNull
}

class LoaderData {
    val loader = loom.platform.get().name.lowercase()
    val isFabric = loader == "fabric"
    val isNeoforge = loader == "neoforge"
}

class McData {
    val version = property("mod.mc_version")
    val dep = property("mod.mc_dep").toString()
}

val mc = McData()
val mod = ModData()
val deps = Dependencies()
val loader = LoaderData()

version = "${mod.version}+${mc.version}-${loader.loader}"
group = mod.group
base { archivesName.set(mod.id) }

stonecutter {
    constants["fabric"] = loader.isFabric
    constants["neoforge"] = loader.isNeoforge
}

blossom {
    replaceToken("@MODID@", mod.id)
}

loom {
    silentMojangMappingsLicense()
    runConfigs.all {
        ideConfigGenerated(stonecutter.current.isActive)
        runDir = "../../run"
    }
    runConfigs.remove(runConfigs["server"])
}

fletchingTable {
    mixins.create("main") {
        mixin("default", "${mod.id}.mixins.json")
    }
}

repositories {
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    minecraft("com.mojang:minecraft:${mc.version}")
    mappings(loom.officialMojangMappings())

    if (loader.isFabric) {
        requireNotNull(deps.fabricApiVersion) { "deps.fabric_api_version must be set for fabric variants" }
        modImplementation("net.fabricmc:fabric-loader:${deps.fabricLoaderVersion}")!!
        modImplementation("net.fabricmc.fabric-api:fabric-api:${deps.fabricApiVersion}+${mc.version}")
    } else if (loader.isNeoforge) {
        requireNotNull(deps.neoforgeVersion) { "deps.neoforge_version must be set for neoforge variants" }
        "neoForge"("net.neoforged:neoforge:${deps.neoforgeVersion}")
    }
}

java {
    val lang = if (mc.version.toString().startsWith("26.1")) 25 else 21
    toolchain.languageVersion.set(JavaLanguageVersion.of(lang))
}

tasks.processResources {
    val props = mapOf(
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "mcdep" to mc.dep,
        "description" to mod.description,
        "source" to mod.source,
        "issues" to mod.issues,
        "license" to mod.license,
        "fabric_loader_version" to deps.fabricLoaderVersion,
        "forge_version" to (deps.neoforgeVersion ?: ""),
    )

    props.forEach(inputs::property)

    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
        expand(props)
    }
}

