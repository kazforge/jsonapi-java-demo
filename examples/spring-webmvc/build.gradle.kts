plugins {
    java
}

dependencies {
    implementation(libs.jsonapi.spring.webmvc)
    implementation(libs.jsonapi.jackson3)
    implementation(libs.jackson.databind)
    implementation(platform(libs.spring.bom))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.spring.test)
    testImplementation(libs.hamcrest)
    testRuntimeOnly(libs.junit.platform.launcher)
}
