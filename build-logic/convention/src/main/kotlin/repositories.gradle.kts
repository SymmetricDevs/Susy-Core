plugins {
    java
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    exclusiveContent {
        forRepositories(
            maven {
                name = "Curse Maven"
                url = uri("https://www.cursemaven.com")
            },
            maven {
                name = "Curse Maven Mirror"
                url = uri("https://curse.cleanroommc.com")
            }
        )
        filter {
            includeGroup("curse.maven")
        }
    }

    maven {
        name = "Cleanroom Maven"
        url = uri("https://maven.cleanroommc.com")
    }

    maven {
        name = "BlameJared Maven"
        url = uri("https://maven.blamejared.com")
    }

    maven {
        name = "GTNH Maven"
        url = uri("https://nexus.gtnewhorizons.com/repository/public/")
    }

    maven {
        name = "GTCEu Maven"
        url = uri("https://maven.gtceu.com")
    }

    mavenCentral()
    mavenLocal()
}
