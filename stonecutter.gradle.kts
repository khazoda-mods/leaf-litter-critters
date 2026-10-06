import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment

plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.18.2" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
}

stonecutter active "26.1"

stonecutter parameters {
    properties { tags(current.version) }
}

val modId = property("mod.id") as String
val modName = property("mod.name") as String
val modVersion = property("mod.version") as String
val artifactDirectory = layout.buildDirectory.dir("libs/$modVersion")

tasks.register<Sync>("buildAll") {
    group = "build"
    description = "Builds and collects universal jars for every Minecraft version"
    dependsOn(stonecutter.tree.nodes.map { ":${it.metadata.project}:build" })
    stonecutter.tree.nodes.forEach {
        from(layout.projectDirectory.dir("versions/${it.metadata.project}/build/libs")) {
            include("$modId-$modVersion+${it.metadata.version}.jar", "$modId-$modVersion+${it.metadata.version}-sources.jar")
        }
    }
    into(artifactDirectory)
}

val availableVersions = stonecutter.tree.nodes.map { it.metadata.version }.toSet()
val selectedVersions = providers.gradleProperty("publishVersions")
    .orElse(providers.environmentVariable("PUBLISH_VERSIONS")).orNull
    ?.takeIf { it.isNotBlank() }?.split(Regex("[,\\s]+"))?.filter { it.isNotBlank() }?.toSet()
    ?: availableVersions
require(selectedVersions.isNotEmpty() && availableVersions.containsAll(selectedVersions)) {
    "Unknown or empty publish version selection: $selectedVersions. Available: $availableVersions"
}

val targets = stonecutter.tree.nodes.filter { it.metadata.version in selectedVersions }.map { it.metadata.version }
val modrinthId = providers.gradleProperty("modrinthProjectId").orElse(providers.environmentVariable("MODRINTH_PROJECT_ID"))
    .orElse(property("mod.modrinth_id") as String)
val curseforgeId = providers.gradleProperty("curseforgeProjectId").orElse(providers.environmentVariable("CURSEFORGE_PROJECT_ID"))
    .orElse(property("mod.curseforge_id") as String)
val publishModrinth = providers.gradleProperty("publishModrinth").map(String::toBoolean).orElse(true).get()
val publishCurseForge = providers.gradleProperty("publishCurseForge").map(String::toBoolean).orElse(true).get()
val publishGitHub = providers.gradleProperty("publishGitHub").map(String::toBoolean).orElse(true).get()

publishMods {
    version.set(modVersion)
    displayName.set("$modName $modVersion")
    changelog.set(providers.gradleProperty("changelog").orElse(providers.environmentVariable("CHANGELOG")))
    type.set(ReleaseType.valueOf(providers.gradleProperty("releaseType").orElse("STABLE").get().uppercase()))
    dryRun.set(providers.gradleProperty("publishDryRun").map(String::toBoolean).orElse(false))

    targets.forEach { minecraft ->
        val releases = stonecutter.properties.raw(minecraft, "mod", "mc_releases").asList().map { it.toString() }
        val artifact = artifactDirectory.map { it.file("$modId-$modVersion+$minecraft.jar") }
        val suffix = minecraft.replace('.', '_')
        if (publishModrinth) {
            modrinth("modrinth_$suffix") {
                projectId.set(modrinthId)
                accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
                file.set(artifact)
                version.set("$modVersion+$minecraft")
                displayName.set("$modName $modVersion for $minecraft")
                modLoaders.addAll("fabric", "neoforge")
                minecraftVersions.addAll(releases)
                environment.set(ModrinthEnvironment.CLIENT_ONLY)
            }
        }
        if (publishCurseForge) {
            curseforge("curseforge_$suffix") {
                projectId.set(curseforgeId)
                accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
                file.set(artifact)
                version.set("$modVersion+$minecraft")
                displayName.set("$modName $modVersion for $minecraft")
                modLoaders.addAll("fabric", "neoforge")
                minecraftVersions.addAll(releases)
                client.set(true)
                server.set(false)
            }
        }
    }

    if (publishGitHub) {
        github {
            repository.set(providers.gradleProperty("githubRepository").orElse("khazoda-mods/leaf-litter-critters"))
            accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
            commitish.set(providers.environmentVariable("GITHUB_REF_NAME").orElse("main"))
            val releaseVersion = if (selectedVersions == availableVersions) modVersion
                else "$modVersion+${selectedVersions.sorted().joinToString("-")}"
            version.set(releaseVersion)
            tagName.set("v$releaseVersion")
            val artifacts = targets.map { minecraft ->
                artifactDirectory.map { it.file("$modId-$modVersion+$minecraft.jar") }
            }
            file.set(artifacts.first())
            additionalFiles.from(artifacts.drop(1))
        }
    }
}

tasks.matching { it.name.startsWith("publish") }.configureEach {
    dependsOn("buildAll")
}
