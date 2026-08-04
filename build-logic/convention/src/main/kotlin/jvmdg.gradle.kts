import com.gtnewhorizons.retrofuturagradle.minecraft.RunMinecraftTask
import xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar
import xyz.wagyourtail.jvmdg.gradle.task.ShadeJar
import xyz.wagyourtail.jvmdg.gradle.task.files.DowngradeFiles
import java.util.*
import java.io.Serializable as JSerializable

plugins {
    alias(libs.plugins.jvmDowngrader)
    alias(libs.plugins.retrofuturaGradle)
}

val shadowDowngrade = configurations.create("shadowDowngrade")

configurations.compileOnly {
    extendsFrom(shadowDowngrade)
}

dependencies {
    testImplementation(variantOf(libs.jvmdowngrader.javaApi) { classifier("downgraded-8") })
}

jvmdg.apply {
    shadePath = ConstantShadePath(jvmdgShadowPath)
    dg(shadowDowngrade)
}

tasks.withType<DowngradeJar>().configureEach { logLevel.set("FATAL") }
tasks.withType<DowngradeFiles>().configureEach { logLevel.set("FATAL") }

val dgTest = downgradeSourceSet("test")
val dgMain = downgradeSourceSet("main")
val dgApi = downgradeSourceSet("api")

val downgradeRunJar = tasks.register<DowngradeJar>("downgradeRunJar") {
    description = "Downgrade the slim project jar for Minecraft run tasks"
    inputFile = tasks.jar.flatMap { it.archiveFile }
    archiveClassifier = "run-downgraded"
}

val shadeRunDowngradedApi = tasks.register<ShadeJar>("shadeRunDowngradedApi") {
    description = "Shade JvmDowngrader API stubs into the downgraded run jar"
    inputFile = downgradeRunJar.flatMap { it.archiveFile }
    archiveClassifier = "run-downgraded-shaded"
}

tasks.reobfJar { inputJar = tasks.shadeDowngradedApi.flatMap { it.archiveFile } }

// RunObf* tasks are intentionally excluded, since they relay on reobfJar
tasks.withType<RunMinecraftTask>().configureEach {
    if (!systemProperties.contains("retrofuturagradle.reobfDev")) {
        classpath = classpath - layout.files(tasks.jar) + layout.files(shadeRunDowngradedApi, shadowDowngrade)
    }
}

tasks.test {
    // ensure tests are run with java8
    javaLauncher = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.AZUL)
    }

    testClassesDirs = layout.files(dgTest.outputs)

    classpath = classpath
        .plus(layout.files(dgTest.outputs, dgMain.outputs, dgApi.outputs))
        .minus(layout.files(sourceSets.main.classesDirs, sourceSets.test.classesDirs, sourceSets.api.classesDirs))
}

tasks.compileInjectedInterfacesJava {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.AZUL)
    }
}

private fun downgradeSourceSet(name: String): TaskProvider<DowngradeFiles> {
    val capitalizedName = name.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }

    return tasks.register<DowngradeFiles>("downgrade${capitalizedName}") {
        description = "Downgrade the $name sourceSet"
        inputCollection = objects.fileCollection().from(sourceSets.named(name).classesDirs)
    }
}

private val TaskProvider<DowngradeFiles>.outputs: Provider<FileCollection>
    get() = map { it.outputCollection }

private val Provider<SourceSet>.classesDirs: Provider<FileCollection>
    get() = map { it.output.classesDirs }

private class ConstantShadePath(private val path: String) : (String) -> String, JSerializable {
    override fun invoke(fileName: String): String = path
}
