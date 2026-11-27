# Contributing

Use a local JDK 21, Docker for the PostgreSQL integration tests, and the
committed Gradle wrapper. No separate Gradle installation or jsonapi-java
source checkout is needed.

For source or build changes, run from the repository root:

```sh
./gradlew spotlessApply
./gradlew spotlessCheck
./gradlew clean build
git diff --check
```

Spotless formats Java sources with Google Java Format and Gradle Kotlin DSL
scripts with ktlint. The normal root `build` also checks formatting.
Groovy/Spock and documentation formatting are out of scope.

Keep examples small, runnable and focused on observable consumer behavior.
Use supported public APIs and released `com.kazforge:jsonapi-java-*` artifacts
from Maven Central. Do not add alternate repositories or local artifacts.

Manage dependency coordinates in `gradle/libs.versions.toml` and use catalog
aliases. The Spring Boot example uses the Boot BOM; standalone examples own their
Spring, Jackson and JUnit choices. Use stable, published releases.
Update the Gradle wrapper with its distribution SHA-256 checksum as well as
the version, and regenerate the wrapper using the new Gradle version.

When adding an example, place it under `examples/`, include it in
`settings.gradle.kts`, and add its `build` and `clean` tasks to the root
orchestration in `build.gradle.kts`. Give it a focused automated test and
document its run command. The persistent publishing reference application lives
in `examples/spring-boot`; the root project stays an aggregator.

Open a PR against `main` for one coherent change. Describe the behavior and
checks run; use Conventional Commits for commit messages and PR titles.
Follow `.editorconfig` and keep unrelated cleanup out of the PR.
