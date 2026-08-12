import com.modrinth.minotaur.dependencies.container.NamedDependencyContainer
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.darkhax.curseforgegradle.Constants as CurseForge

plugins {
    `maven-publish`
    alias(libs.plugins.retrofuturaGradle)
    alias(libs.plugins.curseforgeGradle)
    alias(libs.plugins.minotaur)
}

// Shared values used for publishing
val jarName = modVersion.ifBlank { gitVersion().get() }
val releaseName: String = versionDisplayFormat
    .replace($$"$MOD_NAME", modName)
    .replace($$"$VERSION", modVersion)

require(releaseChannel in CurseForge.VALID_RELEASE_TYPES) {
    "Release type invalid! Found \"$releaseChannel\", allowed: ${CurseForge.VALID_RELEASE_TYPES.joinToString { "\"$it\"" }}."
}

val logicalSide = when (environment.lowercase()) {
    "client" -> arrayOf("Client")
    "server" -> arrayOf("Server")
    "both" -> arrayOf("Client", "Server")
    else -> throw GradleException("Mod environment must be on of \"client\", \"server\", or \"both\", but \"$environment\" was found!")
}

java {
    // Build sources jar for publishing
    withSourcesJar()
}

tasks.withType<AbstractArchiveTask>().configureEach {
    archiveVersion = jarName
}

if (publishToMaven) {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                groupId = artifactGroupId
                artifactId = mavenArtifactId
                version = modVersion
            }
        }
        repositories {
            maven {
                url = uri(customMavenPublishUrl)
                isAllowInsecureProtocol = !customMavenPublishUrl.startsWith("https")
                credentials {
                    username = mavenUser
                    password = mavenPassword
                }
            }
        }
    }
}

// Modrinth
modrinth {
    token = if (deploymentDebug) "DEBUG_TOKEN" else modrinthApiKey
    projectId = modrinthProjectId
    versionName = releaseName
    versionNumber = modVersion
    versionType = releaseChannel
    gameVersions = listOf(mcVersion)
    loaders = listOf("forge")
    detectLoaders = false
    debugMode = deploymentDebug
    uploadFile.set(tasks.reobfJar)
    additionalFiles = listOf(tasks.jar, tasks.named("sourcesJar"))
    changelog = readChangelog()

    modrinthRelations.takeIf { it.isNotBlank() }?.let { str ->
        str.split(";")
            .filter { it.isNotBlank() }
            .forEach {
                val args = it.split(":", limit = 3)
                val (type, slur) = args
                val version = args.getOrNull(2)
                when (type) {
                    in ModRelations.REQ -> required.of(slur, version)
                    in ModRelations.INC -> incompatible.of(slur, version)
                    in ModRelations.OPT -> optional.of(slur, version)
                    in ModRelations.EMB -> embedded.of(slur, version)
                }
            }
    }
}

tasks.modrinth {
    enabled = publishToModrinth || deploymentDebug
}

// CurseForge
val publishToCurseForgeTask = tasks.register<TaskPublishCurseForge>("curseforge") {
    description = "Publishes mod to CurseForge"
    group = "publishing"
    enabled = publishToCurseForge || deploymentDebug

    disableVersionDetection()
    debugMode = deploymentDebug
    apiToken = if (deploymentDebug) "DEBUG_TOKEN" else curseForgeApiKey

    with(upload(curseForgeProjectId, tasks.reobfJar)) {
        displayName = releaseName
        releaseType = releaseChannel
        changelogType = CurseForge.CHANGELOG_MARKDOWN
        changelog = readChangelog()
        addModLoader("Forge")
        addJavaVersion("Java 8")
        addEnvironment(*logicalSide)
        addGameVersion(mcVersion)
//        withAdditionalFile(tasks.jar)
//        withAdditionalFile(tasks.named("sourcesJar"))

        curseForgeRelations.takeIf { it.isNotBlank() }?.let { str ->
            str.split(";")
                .filter { it.isNotBlank() }
                .forEach {
                    val (type, slur) = it.split(':', limit = 2)
                    when (type) {
                        in ModRelations.REQ -> addRequirement(slur)
                        in ModRelations.INC -> addIncompatibility(slur)
                        in ModRelations.OPT -> addOptional(slur)
                        in ModRelations.EMB -> addEmbedded(slur)
                    }
                }
        }
    }
}

private fun readChangelog(): Provider<String> = provider {
    val changelogFile = file(changelogLocation)
    if (changelogFile.exists()) changelogFile.readText(Charsets.UTF_8) else ""
}

private fun NamedDependencyContainer.of(slur: String, version: String? = null) {
    return if (version.isNullOrBlank()) project(slur) else version(slur, version)
}

@Suppress("UnstableApiUsage")
private fun gitVersion(): Provider<String> = providers.exec {
    commandLine(
        "git",
        "describe",
        "--tags",
        "--always",
        "--first-parent",
        "--abbrev=7",
        "--dirty=.dirty",
        "--match=*",
    )
    workingDir(rootDir)
    isIgnoreExitValue = true
}.standardOutput.asText
    .map { it.trim().removePrefix("v") }
    .filter { it.isNotEmpty() }
    .orElse("NO-GIT-VERSION")

private object ModRelations {
    val REQ = arrayOf("req", "required", "requiredDependency")
    val OPT = arrayOf("opt", "optional", "optionalDependency")
    val EMB = arrayOf("emb", "embedded", "embeddedLibrary")
    val INC = arrayOf("incomp", "fail", "incompatible")
}