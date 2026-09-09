plugins {
    alias(libs.plugins.spotless)
}

if (enableSpotless) {
    spotless {
        encoding = Charsets.UTF_8
        isEnforceCheck = false

        format("misc") {
            target(
                ".gitignore",
                ".gitattributes",
            )

            trimTrailingWhitespace()
            leadingTabsToSpaces()
            endWithNewline()
        }

        java {
            target(
                "src/main/java/**/*.java",
                "src/test/java/**/*.java",
            )

            toggleOffOn()
            removeUnusedImports()
            leadingTabsToSpaces()
            importOrderFile(rootProject.file("spotless.importorder"))
            endWithNewline()
            eclipse().configFile(rootProject.file("spotless.eclipseformat.xml"))
            formatAnnotations()
        }
    }
}
