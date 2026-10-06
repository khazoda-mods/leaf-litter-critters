plugins {
    id("net.fabricmc.fabric-loom")
}

fun prop(name: String): String = sc.properties[name]

group = prop("mod.group")
version = "${prop("mod.version")}+${sc.current.version}"
base.archivesName = prop("mod.id")

require(sc.current.parsed >= "26.1") { "This project targets Minecraft 26.1+" }
val requiredJava = prop("deps.java").toInt()

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(requiredJava)
}
tasks.withType<JavaCompile>().configureEach {
    options.release = requiredJava
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    implementation("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    for (module in listOf("fabric-api-base", "fabric-resource-loader-v1")) {
        val dependency = fabricApi.module(module, prop("deps.fabric_api"))
        runtimeOnly(dependency)
        include(dependency)
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    runs.configureEach {
        generateRunConfig = true
        preferGradleTask = true
        runDirectory = rootProject.file("run/fabric/${sc.current.version}")
    }
}

tasks.processResources {
    val icon = prop("mod.icon")
    val props = mapOf(
        "id" to prop("mod.id"),
        "name" to prop("mod.name"),
        "version" to prop("mod.version"),
        "description" to prop("mod.description"),
        "authors" to prop("mod.authors"),
        "license" to prop("mod.license"),
        "homepage" to prop("mod.homepage"),
        "sources" to prop("mod.sources"),
        "icon" to icon,
        "mixins" to prop("mod.mixins"),
        "environment" to prop("mod.environment"),
        "neoforge_side" to prop("mod.environment").uppercase(),
        "fabric_minecraft" to prop("mod.fabric_compat"),
        "neoforge_minecraft" to prop("mod.neo_minecraft_compat"),
        "neoforge" to prop("mod.neo_compat"),
        "fabric_loader" to prop("deps.fabric_loader"),
        "java" to requiredJava,
        "neoforge_icon" to if (sc.current.parsed >= "26.2")
            "iconFile = \"$icon\"\nbannerFile = \"assets/llc/banner.png\"\niconBlur = false"
        else "logoFile = \"$icon\"\nlogoBlur = false"
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) { expand(props) }
    val mixinProps = mapOf("java" to "JAVA_$requiredJava")
    filesMatching("*.mixins.json") { expand(mixinProps) }
}

tasks.withType<Jar>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
