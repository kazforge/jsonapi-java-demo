plugins {
    base
    alias(libs.plugins.spotless)
}

spotless {
    java {
        target("examples/**/src/**/*.java")
        targetExclude("**/build/**", "**/bin/**")
        googleJavaFormat(
            libs.versions.google.java.format
                .get(),
        )
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", "**/bin/**", "**/.gradle/**")
        ktlint(libs.versions.ktlint.get())
        trimTrailingWhitespace()
        endWithNewline()
    }
}

subprojects {
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        }
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}

tasks.named("build") {
    dependsOn(":examples:jackson3-basic:build")
}

tasks.named("clean") {
    dependsOn(":examples:jackson3-basic:clean")
}
