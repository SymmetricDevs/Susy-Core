plugins {
    java
    alias(libs.plugins.ideaExt)
}

// Project properties
group = modGroup
version = modVersion

base {
    archivesName = archiveName
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }

    project {
        setLanguageLevel(JavaVersion.VERSION_25)
    }
}
