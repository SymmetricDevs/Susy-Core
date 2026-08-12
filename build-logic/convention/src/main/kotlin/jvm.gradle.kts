plugins {
    java
}

val utf8: String = Charsets.UTF_8.name()

tasks.withType<JavaCompile>().configureEach {
    options.encoding = utf8
}

tasks.javadoc {
    isFailOnError = false
    with(options as CoreJavadocOptions) {
        quiet()
        encoding = utf8
        addStringOption("Xdoclint:none", "-quiet")
    }
}

// Set the toolchain version to decouple the Java we run Gradle with from the Java used to compile and run the mod
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        // Azul covers the most platforms, crucially including macOS arm64
        vendor.set(JvmVendorSpec.AZUL)
    }
}