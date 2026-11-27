package com.kazforge.jsonapi.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ErrorHandlingHttpTest extends HttpIntegrationTest {
  private static final UUID UNKNOWN = UUID.fromString("00000000-0000-4000-8000-000000000000");

  @Test
  void distinguishesMalformedAndUnknownIdentifiers() {
    expectError(client.get().uri("/articles/not-a-uuid").exchange(), 400, "INVALID_IDENTIFIER");
    expectError(client.get().uri("/people/not-a-uuid").exchange(), 400, "INVALID_IDENTIFIER");
    expectError(client.get().uri("/articles/{id}", UNKNOWN).exchange(), 404, "ARTICLE_NOT_FOUND");
    expectError(client.get().uri("/people/{id}", UNKNOWN).exchange(), 404, "PERSON_NOT_FOUND");
    expectError(
        client.get().uri("/articles/{id}/author", UNKNOWN).exchange(), 404, "ARTICLE_NOT_FOUND");
  }

  @Test
  void returnsSafeQueryErrors() {
    var filter =
        expectError(
            client.get().uri(URI.create("/articles?filter%5Bstatus%5D=secret-value")).exchange(),
            400,
            "INVALID_QUERY_PARAMETER");
    assertThat(filter.at("/source/parameter").asString()).isEqualTo("filter[status]");
    assertThat(filter.toString()).doesNotContain("secret-value");

    var size =
        expectError(
            client.get().uri(URI.create("/articles?page%5Bsize%5D=101")).exchange(),
            400,
            "INVALID_QUERY_PARAMETER");
    assertThat(size.at("/source/parameter").asString()).isEqualTo("page[size]");

    var number =
        expectError(
            client.get().uri(URI.create("/articles?page%5Bnumber%5D=0")).exchange(),
            400,
            "INVALID_QUERY_PARAMETER");
    assertThat(number.at("/source/parameter").asString()).isEqualTo("page[number]");

    var sort =
        expectError(
            client.get().uri(URI.create("/articles?sort=secret-value")).exchange(),
            400,
            "UNSUPPORTED_SORT_FIELD");
    assertThat(sort.at("/source/parameter").asString()).isEqualTo("sort");
    assertThat(sort.toString()).doesNotContain("secret-value");

    expectError(
        client.get().uri(URI.create("/articles?filter%5Bother%5D=secret-value")).exchange(),
        400,
        "UNSUPPORTED_QUERY_PARAMETER");
    expectError(
        client.get().uri(URI.create("/articles?include=unknown")).exchange(),
        400,
        "DISALLOWED_INCLUDE_PATH");
    expectError(
        client
            .get()
            .uri(
                URI.create(
                    "/articles/" + SeedData.API_GUIDE + "/relationships/reviewers?include=author"))
            .exchange(),
        400,
        "DISALLOWED_INCLUDE_PATH");
  }

  @Test
  void returnsSafeDocumentValidationErrors() {
    expectError(
        client
            .post()
            .uri("/articles")
            .contentType(JSON_API_TYPE)
            .body("{\"data\":bad-json")
            .exchange(),
        400,
        "MALFORMED_JSON");
    expectError(
        client
            .post()
            .uri("/articles")
            .contentType(JSON_API_TYPE)
            .body("{\"data\":{\"type\":\"articles\",\"attributes\":{}}}")
            .exchange(),
        400,
        "INVALID_FIELD");

    var blank =
        expectError(
            client
                .post()
                .uri("/articles")
                .contentType(JSON_API_TYPE)
                .body(draftDocument("   "))
                .exchange(),
            400,
            "INVALID_FIELD");
    assertThat(blank.at("/source/pointer").asString()).isEqualTo("/data/attributes/title");

    long totalBefore = getJson("/articles").at("/meta/total").asLong();
    expectError(
        client
            .post()
            .uri(URI.create("/articles?include=author.unknown"))
            .contentType(JSON_API_TYPE)
            .body(draftDocument("Must not be created"))
            .exchange(),
        400,
        "DISALLOWED_INCLUDE_PATH");
    assertThat(getJson("/articles").at("/meta/total").asLong()).isEqualTo(totalBefore);
  }

  @Test
  void enforcesJsonApiMediaTypes() {
    expectError(
        client.get().uri("/articles").accept(MediaType.APPLICATION_JSON).exchange(),
        406,
        "NOT_ACCEPTABLE");
    expectError(
        client
            .post()
            .uri("/articles")
            .contentType(MediaType.APPLICATION_JSON)
            .body(draftDocument("Wrong media type"))
            .exchange(),
        415,
        "UNSUPPORTED_MEDIA_TYPE");
  }

  @Test
  void leavesOrdinaryMvcEndpointsAlone() {
    var ordinary =
        client
            .get()
            .uri(URI.create("/ordinary?include=a..b"))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .returnResult();
    assertThat(ordinary.getResponseHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(json(ordinary).path("mode").asString()).isEqualTo("ordinary");
  }
}
