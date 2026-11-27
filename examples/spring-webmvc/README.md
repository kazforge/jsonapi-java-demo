# Spring WebMVC consumer

A minimal Spring Framework application configuration, exercised through MockMvc
without Boot or an embedded server. Register `WebMvcConfiguration` in a Servlet
6.1 application's Spring context; the tests use that same configuration in a
`MockServletContext`.

Direct runtime dependencies are the published
`com.kazforge:jsonapi-java-spring-webmvc:0.7.0`,
`com.kazforge:jsonapi-java-jackson3:0.5.1` and
`tools.jackson.core:jackson-databind:3.3.0`. The Spring BOM uses **7.0.11**;
Servlet **6.1.0** and Core API/query **0.5.1** arrive transitively. Versions live
in the root catalog.

`WebMvcConfiguration` owns the `JsonApi` bean and explicitly installs the
converter, negotiation/context interceptors, argument resolver, error resolver
and negotiation advice. Only `@JsonApiEndpoint` controllers opt in. Do not add
`consumes`/`produces` mapping restrictions to those handlers: Spring evaluates
them before JSON:API negotiation. Controllers own IDs and persistence; this
stateless example always allocates ID `42`.

From the repository root with Java 21:

```sh
./gradlew :examples:spring-webmvc:test
```

The focused tests cover single/collection GET, ID-less POST DTO binding,
JSON:API/wildcard negotiation, 406/415 errors, safe malformed-input and query
protocol errors, and ordinary JSON GET/POST. This is not a full adapter conformance suite.

The wiring uses the standalone APIs in the [v0.7.0 guide](https://jsonapi.kazforge.com/spring/0.7.0/).
All library code is resolved from Maven Central. The
[historical verification report](VERIFICATION.md) covers the earlier 0.1.0 release.
