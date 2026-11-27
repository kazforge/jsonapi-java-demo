pluginManagement {
    repositories {
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "jsonapi-java-demo"
include(":examples:jackson3-basic")
include(":examples:spring-boot")
include(":examples:spring-webmvc")
