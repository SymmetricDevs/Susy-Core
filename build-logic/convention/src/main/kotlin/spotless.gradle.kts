plugins {
    alias(libs.plugins.spotless)
}

if (enableSpotless || true) {
    spotless {
        encoding = Charsets.UTF_8

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
            importOrder()
            endWithNewline()
            palantirJavaFormat()
            formatAnnotations()
        }
    }
}
