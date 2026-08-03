plugins {
    java
    alias(libs.plugins.shadow)
}

val shadowImplementation = configurations.create("shadowImplementation")

configurations.implementation {
    extendsFrom(shadowImplementation)
}

tasks.shadowJar {
    archiveClassifier = "shadowed"

    filesMatching("**/module-info.class") { exclude() }
    dependencies {
        exclude(dependency("org.jspecify:jspecify:.*"))
        exclude(dependency("org.jetbrains:annotations:.*"))
    }

    configurations = listOf(
        shadowImplementation,
    )

    configurations.add(project.configurations.named("shadowDowngrade"))

    if (minimizeShadowedDependencies) minimize()
    if (relocateShadowedDependencies) {
        enableAutoRelocation.set(true)
        relocationPrefix.set(defaultShadowPath)
    }
}