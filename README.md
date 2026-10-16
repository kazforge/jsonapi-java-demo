# jsonapi-java-demo

[![Build](https://github.com/kazforge/jsonapi-java-demo/actions/workflows/build.yml/badge.svg)](https://github.com/kazforge/jsonapi-java-demo/actions/workflows/build.yml)

Runnable examples for [jsonapi-java](https://jsonapi.kazforge.com/). Each example
is an independent Gradle subproject under `examples/`.

[Jackson 3 basic](examples/jackson3-basic) is the initial minimal example: an annotated
article is written as a JSON:API document and read back using the public API.
Its tests cover the round trip and Core 0.4.0 create-aware reads, including
ID-less resources, `lid` self-references and invalid relationships.

## Get started

Requires JDK 21. Run from the repository root using the committed Gradle wrapper:

```sh
./gradlew build
./gradlew :examples:jackson3-basic:run
```

Java and Gradle Kotlin DSL formatting is checked by `build`;
run `./gradlew spotlessApply` to fix it.

See the [full documentation](https://jsonapi.kazforge.com/) for library usage.

## External-consumer smoke test

Examples consume released artifacts exclusively from Maven Central, not the
jsonapi-java source repository. The initial example depends directly on
`com.kazforge:jsonapi-java-jackson3:0.4.0`; its API, annotations and core
dependencies arrive transitively. The published Core 0.4.0 inventory is:

| Artifact | Coordinate |
| --- | --- |
| Core | `com.kazforge:jsonapi-java-core:0.4.0` |
| Annotations | `com.kazforge:jsonapi-java-annotations:0.4.0` |
| API | `com.kazforge:jsonapi-java-api:0.4.0` |
| Query (optional) | `com.kazforge:jsonapi-java-query:0.4.0` |
| Jackson 3 adapter | `com.kazforge:jsonapi-java-jackson3:0.4.0` |

All five POMs and JARs resolve from Maven Central. The consumer uses no
`jsonapi-java-jackson2` or separate `mapping` artifact. Dependency versions live in
[`gradle/libs.versions.toml`](gradle/libs.versions.toml); Jackson is selected
explicitly and JUnit modules are aligned with its BOM. `settings.gradle.kts`
enforces Maven Central; no sibling checkout or locally published artifacts are needed.

The GitHub Actions build runs `./gradlew clean build` on a fresh Ubuntu checkout
with JDK 21. This is the external-consumer smoke test. To repeat it
locally with an empty dependency cache:

```sh
GRADLE_USER_HOME="$(mktemp -d /tmp/jsonapi-java-demo-gradle.XXXXXX)" ./gradlew clean build
```

## Project

[Contributing](CONTRIBUTING.md) ·
[Library security reporting](https://github.com/kazforge/jsonapi-java/blob/main/SECURITY.md)

Apache License 2.0 — see [LICENSE](LICENSE).
