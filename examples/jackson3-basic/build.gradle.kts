plugins {
    application
}

dependencies {
    implementation(libs.jsonapi.jackson3)
    implementation(libs.jackson.databind)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

application {
    mainClass.set("com.kazforge.jsonapi.demo.jackson3.Jackson3Basic")
}
