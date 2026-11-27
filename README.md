# jsonapi-java-demo

[![Build](https://github.com/kazforge/jsonapi-java-demo/actions/workflows/build.yml/badge.svg)](https://github.com/kazforge/jsonapi-java-demo/actions/workflows/build.yml)

Released-artifact consumers for [jsonapi-java](https://jsonapi.kazforge.com/), all
from Maven Central. Each entry is an independent Gradle subproject under `examples/`.

## Reference application

[Spring Boot](examples/spring-boot) is the canonical database-backed reference: an
Article/Person publishing API on Java 21, Spring Boot 4.1.3, the published JSON:API
Spring starter **0.7.0** (transitive Core **0.5.1**), Spring Data JPA, PostgreSQL,
Liquibase, Docker Compose and Testcontainers.

From the repository root:

```sh
docker compose -f examples/spring-boot/compose.yaml up -d --wait
./gradlew :examples:spring-boot:bootRun
```

Liquibase creates the schema and seeds three articles and three people. Then open
[examples/spring-boot/requests.http](examples/spring-boot/requests.http) or try:

```sh
curl -fsS 'http://localhost:8080/articles?include=author,reviewers' \
  -H 'Accept: application/vnd.api+json'
```

The application README covers endpoints, query features, seeded UUIDs and design
boundaries.

## Other examples

- [Spring WebMVC](examples/spring-webmvc) wires the released Spring adapter 0.7.0
  explicitly, without Boot, and exercises it through MockMvc. Run
  `./gradlew :examples:spring-webmvc:test`.
- [Core / Jackson 3](examples/jackson3-basic) maps an article without Spring. Run
  `./gradlew :examples:jackson3-basic:run`.

## Build and verify

Requires JDK 21 and Docker for the reference application's integration tests:

```sh
./gradlew spotlessCheck
./gradlew clean build
./gradlew :examples:spring-boot:dependencies :examples:spring-webmvc:dependencies \
  --configuration runtimeClasspath
```

The root project is an aggregator; the application lives in its own subproject.
Java sources are formatted with Google Java Format and Gradle Kotlin DSL with
ktlint. CI uses an empty Gradle user home, resolves only from Maven Central and
publishes the test reports and both Spring runtime graphs.

[Contributing](CONTRIBUTING.md) · [Library security reporting](https://github.com/kazforge/jsonapi-java/blob/main/SECURITY.md)

Apache License 2.0 — see [LICENSE](LICENSE).
