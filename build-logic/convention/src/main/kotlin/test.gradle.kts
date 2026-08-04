import gradle.kotlin.dsl.accessors._011ecd5265c2c0a72bb455ba6acdb7ef.packageMcLauncher
import gradle.kotlin.dsl.accessors._011ecd5265c2c0a72bb455ba6acdb7ef.packagePatchedMc
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.retrofuturaGradle)
}

tasks.test {
    classpath += layout.files(tasks.packagePatchedMc, tasks.packageMcLauncher)

    testLogging {
        events(TestLogEvent.STARTED, TestLogEvent.PASSED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
    }
    if (enableJUnit) useJUnitPlatform()
}

dependencies {
    if (enableJUnit) {
        testImplementation(platform(libs.junit.bom))
        testImplementation(libs.junit.jupiter)
        testRuntimeOnly(libs.junit.platform.launcher)
    }
}