plugins {
    id("net.neoforged.moddev") version "2.0.148"
}

val minecraft = providers.gradleProperty("neoforge").get()
val controller = rootProject.extensions.getByType<dev.kikugie.stonecutter.controller.StonecutterControllerExtension>()
require(minecraft in controller.tree.nodes.map { it.metadata.version }) {
    "Unsupported NeoForge test version: $minecraft"
}
val target = evaluationDependsOn(":$minecraft")
val sc = target.extensions.getByType<dev.kikugie.stonecutter.build.StonecutterBuildExtension>()
fun prop(name: String): String = sc.properties[name]
val neoVersion = prop("deps.neo_loader")
val modVersion = rootProject.property("mod.version") as String
val modId = rootProject.property("mod.id") as String
val universalJar = rootProject.layout.projectDirectory.file("versions/$minecraft/build/libs/$modId-$modVersion+$minecraft.jar")

java.toolchain.languageVersion = JavaLanguageVersion.of(prop("deps.java").toInt())

repositories {
    maven("https://maven.khazoda.com/releases") {
        content { includeGroup("com.khazoda") }
    }
}

dependencies {
    runtimeOnly("com.khazoda:khazodacore-neoforge:${prop("deps.khazodacore")}+$minecraft")
    runtimeOnly(files(universalJar).builtBy(":$minecraft:jar"))
}

neoForge {
    version = neoVersion
    runs {
        register("client") { client() }
        register("server") {
            server()
            programArgument("--nogui")
        }
        configureEach {
            gameDirectory = rootProject.file("run/neoforge/$minecraft")
            ideName = "NeoForge ${name.replaceFirstChar(Char::uppercase)} - $minecraft"
        }
    }
}

tasks.named<JavaExec>("runServer") { standardInput = System.`in` }
