import com.gtnewhorizons.retrofuturagradle.minecraft.RunMinecraftTask
import com.gtnewhorizons.retrofuturagradle.util.Distribution

plugins {
    alias(libs.plugins.accesstransformers)
    alias(libs.plugins.retrofuturaGradle)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.ideaExt)
}

val modernJavaExtraRuntimeClasspath = configurations.create("modernJavaExtraRuntimeClasspath") {
    isCanBeConsumed = false
}
val modernJavaAsmBootstrap = configurations.create("modernJavaAsmBootstrap") {
    isCanBeConsumed = false
}

// Most RFG configuration lives here, see the Javadoc for com.gtnewhorizons.retrofuturagradle.MinecraftExtension
minecraft {
    mcVersion = project.mcVersion

    // Username for client run configurations
    username = devUserName

    if (useLwjgl3ify) {
        lwjgl3Version = libs.versions.lwjgl3.get()
    }

    extraRunJvmArguments.apply {
        // Enable assertions in the mod's package when running the client or server
        add("-ea:${project.group}")
        add("-Dterminal.jline=true")

        if (useMixin) {
            addAll(
                "-Dmixin.hotSwap=true",
                "-Dmixin.checks.interfaces=true",
                "-Dmixin.debug.export=true",
            )
        }

        if (useCoreMod) {
            add("-Dfml.coreMods.load=$modGroup.$coreModClass")
        }

        if (enableCoreModDebug) {
            addAll(
                "-Dlegacy.debugClassLoading=true",
                "-Dlegacy.debugClassLoadingFiner=true",
                "-Dlegacy.debugClassLoadingSave=true",
            )
        }

        if (extJavaArgs.isNotEmpty()) addAll(extJavaArgs.split(";"))
    }

    // If needed, add extra tweaker classes like for mixins.
    // extraTweakClasses.add("org.spongepowered.asm.launch.MixinTweaker")
}

// Automatic constants generation with BuildConfig
if (generateTags) {
    buildConfig {
        className("Tags")
        packageName(modGroup)
        useJavaOutput()
        buildConfigField("MOD_ID", modId)
        buildConfigField("MOD_NAME", modName)
        buildConfigField("MOD_VERSION", modVersion)
        buildConfigField("MC_VERSION", "[$mcVersion]")
    }
}

// AccessTransformers
if (accessTransformers.isNotEmpty()) {

    // This will apply ATs to both minecraft & forge sources
    tasks.applyJST {
        accessTransformerFiles.from(
            accessTransformers.split(";")
            .map { file("src/main/resources/$it") }
            .onEach { if (!it.exists()) throw GradleException("Could not find accessTransformer file \"$it\"!") })
    }
}

tasks.processResources {
    if (!useMixin) exclude("*mixin*.json")

    val templateTokens = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version" to modVersion,
        "mc_version" to mcVersion,
        "mod_group" to modGroup,
        "mixin_package" to mixinPackage,
        "mixin_refmap" to mixinRefmap,
        "mixin_min_version" to libs.versions.mixin.get(),
        "mixinextras_min_version" to libs.versions.mixinExtras.get(),
    )

    val refmap = mixinRefmap

    // Template files
    filesMatching(listOf("mcmod.info", "pack.mcmeta", "*mixin*.json")) {
        if (name != refmap) {
            expand(templateTokens)
        }
    }

    // Copy AT files to where it should be
    rename("(.+_at.cfg)", "META-INF/$1")
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(buildMap {
            if (useCoreMod) {
                put("FMLCorePlugin", "${modGroup}.${coreModClass}")
            }
            if (useMixin || useCoreMod) {
                put("FMLCorePluginContainsFMLMod", true)
                put("ForceLoadAsMod", forceLoadAsMod)
            }
            if (accessTransformers.isNotEmpty()) {
                put("FMLAT", accessTransformers.replace(";", " "))
            }
        })
    }
}

dependencies {
    runtimeOnly(libs.osxNarratorBlocker) { isTransitive = false }
    runtimeOnly(libs.stripLatestForgeRequirements) { isTransitive = false }
    runtimeOnly(libs.mixinbooter) { isTransitive = false }
    patchedMinecraft(libs.launchWrapper) { isTransitive = false }

    compileOnly(libs.java8UnsupportedShim)

    if (useLwjgl3ify) {
        patchedMinecraft(libs.java8UnsupportedShim)
        modernJavaAsmBootstrap(libs.asm) { isTransitive = false }
        modernJavaAsmBootstrap(libs.asm.tree) { isTransitive = false }
        modernJavaAsmBootstrap(libs.asm.commons) { isTransitive = false }
        modernJavaAsmBootstrap(libs.asm.util) { isTransitive = false }
        modernJavaAsmBootstrap(libs.asm.analysis) { isTransitive = false }
        modernJavaExtraRuntimeClasspath(libs.lwjgl3ify) { isTransitive = false }
        modernJavaExtraRuntimeClasspath(
            variantOf(libs.lwjgl3ify) { classifier("forgePatches") }
        ) { isTransitive = false }
        modernJavaExtraRuntimeClasspath(libs.forgePatchesExtra) { isTransitive = false }
    }

    if (useMixin) {
        annotationProcessor(libs.asmDebug)
        annotationProcessor(libs.guava)
        annotationProcessor(libs.gson)
        api(libs.mixinbooter) { isTransitive = false }
        modUtils.enableMixins(libs.mixinbooter, mixinRefmap)
    }
}

// Interface injection
val interfaceFilePath = "src/injectedInterfaces/interfaces.json"
tasks.applyJST.configure {
    if (file(interfaceFilePath).exists()) {
        interfaceInjectionConfigs.setFrom(interfaceFilePath)
    }
}

if (useLwjgl3ify) {

    // The JetBrains Runtime 21 toolchain both run tasks launch with (required for hotswap support).
    val modernJavaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(25)
        @Suppress("UnstableApiUsage")
        vendor.set(JvmVendorSpec.JETBRAINS)
    }

    tasks.register<RunMinecraftTask>("runClientModernJava", Distribution.CLIENT) {
        description = "Runs the modded client using modern Java and lwjgl3ify"
        configureModernJava()
        javaLauncher = modernJavaLauncher
    }

    tasks.register<RunMinecraftTask>("runServerModernJava", Distribution.DEDICATED_SERVER) {
        description = "Runs the modded server using modern Java and lwjgl3ify"
        configureModernJava()
        javaLauncher = modernJavaLauncher
    }
}

// Have to be private to avoid ambiguity
@Suppress("TaskMissingDescription")
private inline fun <reified T : Task> TaskContainer.register(
    name: String,
    vararg arguments: Any,
    noinline configurationAction: T.() -> Unit
): TaskProvider<T> = register<T>(name, *arguments).apply { configure(configurationAction) }

private fun RunMinecraftTask.configureModernJava() {
    group = "Modded Minecraft"
    lwjglVersion.set(3)
    setup(project)

    username.set(minecraft.username)
    userUUID.set(minecraft.userUUID)

    classpath(
        modernJavaAsmBootstrap,
        modernJavaExtraRuntimeClasspath,
        tasks.packageMcLauncher,
        tasks.packagePatchedMc,
        tasks.jar,
        configurations.runtimeClasspath,
    )

    mainClass = if (side == Distribution.CLIENT) "GradleStart" else "GradleStartServer"

    extraJvmArgs.addAll(modernJavaJvmArgs)
    if (enableHotswap) extraJvmArgs.addAll(hotswapJvmArgs)

    systemProperty("gradlestart.bouncerClient", "com.gtnewhorizons.retrofuturabootstrap.Main")
    systemProperty("gradlestart.bouncerServer", "com.gtnewhorizons.retrofuturabootstrap.Main")

    if (useMixin && enableHotswap) {
        configurations.detachedConfiguration(libs.mixinbooter.get()).apply {
            isTransitive = false

            extraJvmArgs.addAll(
                elements.map {
                    listOf("-javaagent:${it.single().asFile.absolutePath}")
                }
            )
        }
    }
}

// JVM args toggled on only when hotswapping.
val hotswapJvmArgs = listOf(
    "-XX:+AllowEnhancedClassRedefinition",
    "-XX:HotswapAgent=fatjar",
)

// JVM args required to run the 1.7.10/1.12.2-era stack on a modern (Java 17+/21) JVM via lwjgl3ify
// and RetroFuturaBootstrap: encoding, the RFB system classloader, and a pile of --add-opens that
// re-open JDK internals the old code reflects into.
val modernJavaJvmArgs = listOf(
    "-Dfile.encoding=UTF-8",
    "-Djava.system.class.loader=com.gtnewhorizons.retrofuturabootstrap.RfbSystemClassLoader",
    // No "-Djava.security.manager=allow" here. Java 24+ (JEP 486) permanently disabled the
    // Security Manager, so that flag now hard-fails at VM init ("Enabling a Security Manager is
    // not supported"). It's safe to omit on the modern-Java toolchain.
    "--enable-native-access=ALL-UNNAMED",

    "--add-opens", "java.base/jdk.internal.loader=ALL-UNNAMED",
    "--add-opens", "java.base/java.net=ALL-UNNAMED",
    "--add-opens", "java.base/java.nio=ALL-UNNAMED",
    "--add-opens", "java.base/java.io=ALL-UNNAMED",
    "--add-opens", "java.base/java.lang=ALL-UNNAMED",
    "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
    "--add-opens", "java.base/java.text=ALL-UNNAMED",
    "--add-opens", "java.base/java.util=ALL-UNNAMED",
    "--add-opens", "java.base/jdk.internal.reflect=ALL-UNNAMED",
    "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
    "--add-opens", "jdk.naming.dns/com.sun.jndi.dns=ALL-UNNAMED,java.naming",
    "--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
    "--add-opens", "java.desktop/sun.awt.image=ALL-UNNAMED",
    "--add-opens", "java.desktop/com.sun.imageio.plugins.png=ALL-UNNAMED",
    "--add-opens", "jdk.dynalink/jdk.dynalink.beans=ALL-UNNAMED",
    "--add-opens", "java.sql.rowset/javax.sql.rowset.serial=ALL-UNNAMED",
    "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED",
    "--add-opens", "java.base/java.lang.ref=ALL-UNNAMED",
    "--add-opens", "java.base/java.net.spi=ALL-UNNAMED",
    "--add-opens", "java.base/java.nio.channels=ALL-UNNAMED",
    "--add-opens", "java.base/java.nio.charset=ALL-UNNAMED",
    "--add-opens", "java.base/java.nio.file=ALL-UNNAMED",
    "--add-opens", "java.base/java.time.chrono=ALL-UNNAMED",
    "--add-opens", "java.base/java.time.format=ALL-UNNAMED",
    "--add-opens", "java.base/java.time.temporal=ALL-UNNAMED",
    "--add-opens", "java.base/java.time.zone=ALL-UNNAMED",
    "--add-opens", "java.base/java.time=ALL-UNNAMED",
    "--add-opens", "java.base/java.util.concurrent.atomic=ALL-UNNAMED",
    "--add-opens", "java.base/java.util.concurrent.locks=ALL-UNNAMED",
    "--add-opens", "java.base/java.util.jar=ALL-UNNAMED",
    "--add-opens", "java.base/java.util.zip=ALL-UNNAMED",
    "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED",
    "--add-opens", "java.base/jdk.internal.ref=ALL-UNNAMED"
)

