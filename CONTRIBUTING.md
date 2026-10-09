# Contributing

Use a local JDK 21 and the committed Gradle wrapper. No separate Gradle
installation or jsonapi-java source checkout is needed.

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
The initial Jackson 3 example deliberately tests release `0.2.0` for KAZ-126;
dependency upgrades should be a separate, explicit change.

Manage dependency coordinates and versions in `gradle/libs.versions.toml` and
use catalog aliases in example builds. Pin current stable releases, not dynamic
versions or pre-releases. Keep JUnit modules aligned with the catalog's BOM.
Update the Gradle wrapper with its distribution SHA-256 checksum as well as
the version, and regenerate the wrapper using the new Gradle version.

When adding an example, place it under `examples/`, include it in
`settings.gradle.kts`, and add its `build` and `clean` tasks to the root
orchestration in `build.gradle.kts`. Give it a focused automated test and
document its run command. Keep application code out of the root project.

Open a PR against `main` for one coherent change. Describe the behavior and
checks run; use Conventional Commits for commit messages and PR titles.
Follow `.editorconfig` and keep unrelated cleanup out of the PR.
