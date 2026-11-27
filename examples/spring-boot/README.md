# Spring Boot reference application

A runnable Article/Person publishing API that consumes the released
`com.kazforge:jsonapi-java-spring-boot-starter` **0.7.0** from Maven Central.
Core **0.5.1**, Spring Framework 7.0.11 and Jackson 3.1.8 arrive through the
starter and the Spring Boot 4.1.3 BOM.

It is intentionally a plain Spring Boot application: controllers, DTOs, entities,
repositories, services, mappers, configuration and application exceptions, with no
generic CRUD or mapping layer. Spring Data executes filtering, sorting and paging;
the application owns transactions, publication rules and representation shape.

## Run locally

Install JDK 21 and Docker with the Compose plugin. From the repository root:

```sh
docker compose -f examples/spring-boot/compose.yaml up -d --wait
./gradlew :examples:spring-boot:bootRun
```

Liquibase creates the schema and seeds the data on the first start; Hibernate
validates the schema and open-in-view is disabled. The Compose volume keeps data
across restarts. If port 5432 is taken, set `POSTGRES_PORT` for both commands.
Stop and reset with:

```sh
docker compose -f examples/spring-boot/compose.yaml down -v
```

```sh
curl -fsS 'http://localhost:8080/articles?include=author,reviewers' \
  -H 'Accept: application/vnd.api+json'

curl -fsS 'http://localhost:8080/articles?filter%5Bstatus%5D=PUBLISHED&sort=-title&page%5Bsize%5D=1&fields%5Barticles%5D=title,author' \
  -H 'Accept: application/vnd.api+json'
```

[requests.http](requests.http) walks through the seeded data, create, PATCH,
relationship mutations, validation and delete. The directory also owns the
Compose file, so `docker compose up -d --wait` works when run from here.

## Seed data

| Resource | ID | Notes |
| --- | --- | --- |
| Person | `11111111-1111-4111-8111-111111111111` | Ada Lovelace |
| Person | `22222222-2222-4222-8222-222222222222` | Grace Hopper |
| Person | `33333333-3333-4333-8333-333333333333` | Ursula K. Le Guin |
| Article | `aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1` | Draft notes, draft, no author |
| Article | `aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa2` | API guide, published, Ada, reviewers Grace and Ursula |
| Article | `aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa3` | Editorial guide, published, Grace, reviewer Ada |

## API

| Path | Methods | Result |
| --- | --- | --- |
| `/articles` | `GET`, `POST` | Paged articles; create returns `201` and `Location` |
| `/articles/{id}` | `GET`, `PATCH`, `DELETE` | Resource; PATCH returns `200`, DELETE `204` |
| `/people`, `/people/{id}` | `GET` | Seeded people resources |
| `/articles/{id}/author`, `/articles/{id}/reviewers` | `GET` | Related people resources |
| `/articles/{id}/relationships/author` | `GET`, `PATCH` | To-one linkage; `{"data":null}` clears |
| `/articles/{id}/relationships/reviewers` | `GET`, `PATCH`, `POST`, `DELETE` | Replace, add or remove to-many linkage |

Relationship mutations return `204`. Adding existing reviewers and removing
absent reviewers are idempotent. Removing linkage never deletes people.

- IDs are server-generated UUIDs. The JSON:API `id` stays a string on the wire; it
  is parsed at the controller boundary. A malformed UUID is `400`, while a valid
  but unknown UUID is `404`.
- `title` is required (maximum 200 characters), `body` is nullable and `status`
  defaults to `DRAFT`. A published article requires an author; publishing without
  one, or clearing the author while published, returns `409`.
- Resource PATCH requires the addressed type and ID. Omitted members stay
  unchanged, explicit `null` clears `body` or `author`, and supplied values
  replace. Sending `reviewers: []` removes all reviewers.
- `GET /articles` accepts `filter[status]`, `sort=title,-status` and
  `page[number]`/`page[size]` (default 1/20, maximum 100). Sorting always breaks
  ties by ID. `meta.total` counts matches and page links preserve the rest of the
  query.
- `include=author,reviewers` is explicitly allowed. `fields[articles]` and
  `fields[people]` apply sparse fieldsets. Relationship linkage endpoints return
  identifiers; related-resource endpoints return complete people.
- `GET /articles/{id}/author` returns `200 {"data":null}` when there is no author.
  Spring 0.7.0 has no typed nullable response, so that empty case is written with
  the documented low-level document writer.

Linkage values that are not included are written without attributes; included
people carry `name`. This keeps unrelated people out of the response and
deduplicates included resources.

## Test

```sh
./gradlew :examples:spring-boot:test
```

The tests start Boot on a random port and one PostgreSQL 17 Testcontainer through
a shared context initializer. Five focused classes cover the lifecycle, PATCH
presence semantics, relationship mutations, query/representation selection and
safe errors. They use the standard Spring `RestTestClient` and real HTTP; no mocks.
Created fixtures are deleted after each test so the seeded data stays
deterministic.
