package com.kazforge.jsonapi.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class ArticleRelationshipHttpTest extends HttpIntegrationTest {
  @Test
  void readsAndMutatesAuthorLinkage() {
    String id = createArticle(draftDocument("Author cycle"));
    String linkage = "/articles/" + id + "/relationships/author";

    assertThat(getJson(linkage).path("data").isNull()).isTrue();
    var empty = getJson("/articles/" + id + "/author");
    assertThat(empty.path("data").isNull()).isTrue();
    var emptyIncluded = getJson("/articles/" + id + "/author?include=").path("included");
    assertThat(emptyIncluded.isArray()).isTrue();
    assertThat(emptyIncluded.isEmpty()).isTrue();

    client
        .patch()
        .uri(linkage)
        .contentType(JSON_API_TYPE)
        .body("{\"data\":{\"type\":\"people\",\"id\":\"%s\"}}".formatted(SeedData.ADA))
        .exchange()
        .expectStatus()
        .isNoContent();

    var set = getJson(linkage);
    assertThat(set.at("/data/id").asString()).isEqualTo(SeedData.ADA.toString());
    assertThat(set.path("data").has("attributes")).isFalse();
    assertThat(getJson("/articles/" + id + "/author").at("/data/attributes/name").asString())
        .isEqualTo("Ada Lovelace");
    assertThat(
            getJson("/articles/" + id + "/author?fields%5Bpeople%5D=")
                .path("data")
                .has("attributes"))
        .isFalse();

    var articleAuthor = article(id).at("/data/relationships/author/data");
    assertThat(articleAuthor.at("/id").asString()).isEqualTo(SeedData.ADA.toString());
    assertThat(articleAuthor.has("attributes")).isFalse();

    client
        .patch()
        .uri(linkage)
        .contentType(JSON_API_TYPE)
        .body("{\"data\":null}")
        .exchange()
        .expectStatus()
        .isNoContent();
    assertThat(getJson(linkage).path("data").isNull()).isTrue();
  }

  @Test
  void replacesAddsAndRemovesReviewersIdempotently() {
    String id = createArticle(draftDocument("Review cycle"));
    String linkage = "/articles/" + id + "/relationships/reviewers";

    client
        .patch()
        .uri(linkage)
        .contentType(JSON_API_TYPE)
        .body(linkage(SeedData.GRACE))
        .exchange()
        .expectStatus()
        .isNoContent();
    client
        .post()
        .uri(linkage)
        .contentType(JSON_API_TYPE)
        .body(linkage(SeedData.ADA, SeedData.GRACE))
        .exchange()
        .expectStatus()
        .isNoContent();
    client
        .post()
        .uri(linkage)
        .contentType(JSON_API_TYPE)
        .body(linkage(SeedData.GRACE))
        .exchange()
        .expectStatus()
        .isNoContent();

    assertThat(ids(getJson(linkage).path("data")))
        .containsExactly(SeedData.ADA.toString(), SeedData.GRACE.toString());
    var related = getJson("/articles/" + id + "/reviewers");
    assertThat(ids(related.path("data")))
        .containsExactly(SeedData.ADA.toString(), SeedData.GRACE.toString());
    assertThat(related.at("/data/1/attributes/name").asString()).isEqualTo("Grace Hopper");

    client
        .method(HttpMethod.DELETE)
        .uri(linkage)
        .contentType(JSON_API_TYPE)
        .body(linkage(SeedData.ADA, SeedData.URSULA))
        .exchange()
        .expectStatus()
        .isNoContent();
    client
        .method(HttpMethod.DELETE)
        .uri(linkage)
        .contentType(JSON_API_TYPE)
        .body(linkage(SeedData.ADA))
        .exchange()
        .expectStatus()
        .isNoContent();
    assertThat(ids(getJson(linkage).path("data"))).containsExactly(SeedData.GRACE.toString());

    client
        .patch()
        .uri(linkage)
        .contentType(JSON_API_TYPE)
        .body(linkage())
        .exchange()
        .expectStatus()
        .isNoContent();
    assertThat(ids(getJson(linkage).path("data"))).isEmpty();
    assertThat(ids(getJson("/articles/" + id + "/reviewers").path("data"))).isEmpty();
    assertThat(ids(getJson("/people").path("data")))
        .containsExactly(
            SeedData.ADA.toString(), SeedData.GRACE.toString(), SeedData.URSULA.toString());
  }

  @Test
  void validatesPersonIdentifiersAndKeepsPeopleIntact() {
    String id = createArticle(draftDocument("Identifier validation"));
    String author = "/articles/" + id + "/relationships/author";

    expectError(
        client
            .patch()
            .uri(author)
            .contentType(JSON_API_TYPE)
            .body("{\"data\":{\"type\":\"articles\",\"id\":\"%s\"}}".formatted(SeedData.API_GUIDE))
            .exchange(),
        400,
        "INVALID_PERSON_IDENTIFIER");
    expectError(
        client
            .patch()
            .uri(author)
            .contentType(JSON_API_TYPE)
            .body("{\"data\":{\"type\":\"people\",\"id\":\"not-a-uuid\"}}")
            .exchange(),
        400,
        "INVALID_PERSON_IDENTIFIER");
    expectError(
        client
            .patch()
            .uri(author)
            .contentType(JSON_API_TYPE)
            .body(
                "{\"data\":{\"type\":\"people\",\"id\":\"%s\"}}"
                    .formatted(UUID.fromString("00000000-0000-4000-8000-000000000000")))
            .exchange(),
        404,
        "PERSON_NOT_FOUND");

    assertThat(ids(getJson("/people").path("data")))
        .containsExactly(
            SeedData.ADA.toString(), SeedData.GRACE.toString(), SeedData.URSULA.toString());
  }
}
