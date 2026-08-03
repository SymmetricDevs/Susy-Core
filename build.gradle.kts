plugins {
    alias(conventions.plugins.repositories)
    alias(conventions.plugins.minecraft)
    alias(conventions.plugins.spotless)
    alias(conventions.plugins.publish)
    alias(conventions.plugins.shadow)
    alias(conventions.plugins.jvmdg)
    alias(conventions.plugins.idea)
    alias(conventions.plugins.test)
    alias(conventions.plugins.jvm)
}

repositories {
    maven {
        name = "GeckoLib"
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    }
    maven {
        name = "ChickenBones"
        url = uri("https://chickenbones.net/maven/")
    }
}

dependencies {
    fun Provider<MinimalExternalModuleDependency>.deobf() = get().let {
        rfg.deobf("${it.module.group}:${it.module.name}:${it.versionConstraint.requiredVersion}")
    }

    compileOnlyApi(deps.jspecify)
    compileOnlyApi(deps.annotations)

    implementation(deps.hei)
    implementation(deps.theOneProbe)

    // # Fix crashes on macOS with Narrator
    runtimeOnly(deps.osxNarratorBlocker)

    // # GregTech dependencies
    implementation(deps.codechickenlib) { isTransitive = false }
    implementation(deps.gregtech) { isTransitive = false }
    implementation(deps.gregicalityMultiblocks) { isTransitive = false }

    // # Transitive GregTech dependencies
    // CTM 1.0.2.31
    api(deps.craftTweaker2)
    api(deps.ae2Uel) { isTransitive = false }
    api(deps.ctm.deobf())

    // # GregTech Addons
    // Supercritical 0.2.5
    implementation(deps.supercritical.deobf())
    // GT-FO 1.12.4
    implementation(deps.gregtechFoodOption.deobf())

    // # GroovyScript
    api(deps.groovyScript) { isTransitive = false }

    // # Lib Mods we are using
    api(deps.modularUi) { isTransitive = false }
    api(deps.geckoLib.deobf())
    // SussyPatches 0.4.0
    api(deps.sussyPatches)

    // # Immersive Railroading dependencies
    // Universal Mod Care 1.2.1
    implementation(deps.universalModCore.deobf())
    // Track API 1.2.0
    implementation(deps.trackApi.deobf())
    // Immersive Railroading 1.10.0
    implementation(deps.immersiveRailroading.deobf())

    // # Recurrent Complex dependencies
    compileOnly(deps.recurrentComplex.deobf())
    compileOnly(deps.ivToolkit.deobf())

    compileOnly(deps.fluidloggedApi)

    // # Pyrotech dependencies
    compileOnly(deps.pyrotech.deobf())
    compileOnly(deps.athenaeum.deobf())
    compileOnly(deps.dropt.deobf())

    // # Other dependencies
    compileOnly(deps.biomesOPlenty.deobf())
    // XNet-1.8.3-ynet
    compileOnly(deps.ynetXnetFork.deobf())
    // ReFinedTools 7.77
    compileOnly(deps.refinedTools.deobf())
    compileOnly(deps.mcjtyLibRefilmed.deobf())
    compileOnly(deps.travelersBackpack.deobf())
    compileOnly(deps.bubblesABaublesFork.deobf())
    compileOnly(deps.barrelsDrumsStorageMore.deobf())
    // LittleTiles
    compileOnly(deps.creativeCore.deobf())
    compileOnly(deps.littleTiles.deobf())

    // ProjectRed
    compileOnly(deps.chickenAsm) { isTransitive = false }
    compileOnly(deps.mrtjpCore.deobf())
    compileOnly(deps.cbMultipart.deobf())
    compileOnly(deps.projectRedCore.deobf())

    runtimeOnly(deps.serverUtil.deobf())

    //ICBM
    //temporary dep, will replace once we have our own missiles
    implementation(deps.icbm.deobf())

    // # Optional dependencies. Uncomment the ones you need
//    runtimeOnly(deps.theBeneath.deobf())
//    runtimeOnly(deps.realisticTerrainGenerationUnofficial.deobf())
//    runtimeOnly(deps.worldEdit.deobf())
//    runtimeOnly(deps.worldEditCuiForgeEdition3.deobf())
//    runtimeOnly(deps.configAnytime)
//    runtimeOnly(deps.flare.deobf())

    // # OptiFine
//    // Copied from GTCEu, originally used to download latest Vintagium from GitHub
//    // Using Gradle's Ant integration seems to be the least hacky way to download an arbitrary file without a plugin
//    file("libs/optifine").mkdirs()
//    ant.get(src = "https://github.com/OpenCubicChunks/OptiFineDevTweaker/releases/download/2.6.15/aa_do_not_rename_OptiFineDevTweaker-2.6.15-all.jar",
//            dest = "libs/optifine/",
//            skipexisting = "true")
//    // Download OptiFine from some random GitHub repo I found by just searching
//    // Since I failed to get the jar from https://optifine.net/home
//    ant.get(src = "https://github.com/SynArchive/OptiFine-Archive/raw/refs/heads/main/1.12.2/preview_OptiFine_1.12.2_HD_U_G6_pre1.jar",
//            dest = "libs/optifine/",
//            skipexisting = "true")
//    runtimeOnly(fileTree("libs/optifine") { include("*.jar") })
}

configurations {
    compileOnly {
        // exclude GNU trove, FastUtil is superior and still updated
        exclude(group = "net.sf.trove4j", module = "trove4j")
        // exclude javax.annotation from findbugs, JetBrains annotations are superior
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        // exclude scala as we don't use it for anything and causes import confusion
        exclude(group = "org.scala-lang")
        exclude(group = "org.scala-lang.modules")
        exclude(group = "org.scala-lang.plugins")
    }
}
