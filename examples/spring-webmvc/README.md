# Spring WebMVC consumer

A minimal Spring Framework application configuration, exercised through MockMvc
without Boot or an embedded server. Register `WebMvcConfiguration` in a Servlet
6.1 application's Spring context; the tests use that same configuration in a
`MockServletContext`.

Direct runtime dependencies are the published
`com.kazforge:jsonapi-java-spring-webmvc:0.1.0`,
`com.kazforge:jsonapi-java-jackson3:0.4.0` and
`tools.jackson.core:jackson-databind:3.2.3`. The Spring BOM pins **7.0.9**
(the release's 7.0.x minimum); Servlet **6.1.0** and neutral Core API/query
**0.4.0** arrive transitively. Versions live in the root catalog.

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

The wiring follows the [v0.1.0 guide and example](https://github.com/kazforge/jsonapi-java-spring/tree/v0.1.0/examples/jackson3-webmvc),
but **all library code is resolved from Maven Central**, never from that source
project. See [independent release verification](VERIFICATION.md) for exact
commands, public artifact URLs and evidence.
