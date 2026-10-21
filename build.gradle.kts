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

repositories {
    maven("https://maven.khazoda.com/releases") {
        content { includeGroup("com.khazoda") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    implementation("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    compileOnly("com.khazoda:khazodacore-common:${prop("deps.khazodacore")}+${sc.current.version}")
    runtimeOnly("com.khazoda:khazodacore-fabric:${prop("deps.khazodacore")}+${sc.current.version}")
    for (module in listOf("fabric-api-base", "fabric-resource-loader-v1")) {
        runtimeOnly(fabricApi.module(module, prop("deps.fabric_api")))
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    runs.configureEach {
        displayName.set("Fabric ${name.replaceFirstChar(Char::uppercase)} - ${sc.current.version}")
        appendProjectPathToDisplayName.set(false)
        generateRunConfig = true
        preferGradleTask = true
        runDirectory = rootProject.file("run/fabric/${sc.current.version}")
    }
}

tasks.processResources {
    val icon = prop("mod.icon")
    val banner = prop("mod.banner")
    val props = mapOf(
        "id" to prop("mod.id"),
        "name" to prop("mod.name"),
        "version" to prop("mod.version"),
        "description" to prop("mod.description"),
        "authors" to prop("mod.authors"),
        "license" to prop("mod.license"),
        "homepage" to prop("mod.homepage"),
        "sources" to prop("mod.sources"),
        "issues" to prop("mod.issues"),
        "discord" to prop("mod.discord"),
        "icon" to icon,
        "banner" to banner,
        "background" to prop("mod.background"),
        "mixins" to prop("mod.mixins"),
        "environment" to prop("mod.environment"),
        "neoforge_side" to prop("mod.environment").uppercase(),
        "fabric_minecraft" to prop("mod.fabric_compat"),
        "neoforge_minecraft" to prop("mod.neo_minecraft_compat"),
        "neoforge" to prop("mod.neo_compat"),
        "fabric_loader" to prop("deps.fabric_loader"),
        "khazodacore" to prop("deps.khazodacore"),
        "java" to requiredJava,
        "neoforge_branding" to if (sc.current.parsed >= "26.2")
            "iconFile = \"$icon\"\n" +
                    "bannerFile = \"$banner\"\n" +
                    "iconBlur = false"
        else ""
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
