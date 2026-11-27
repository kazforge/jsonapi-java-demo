package com.kazforge.jsonapi.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ArticleLifecycleHttpTest extends HttpIntegrationTest {
  @Test
  void persistsAnArticleAcrossCreateReadPatchAndDeleteRequests() {
    String id =
        createArticle(
            """
            {"data":{"type":"articles","attributes":{"title":"Database-backed draft","body":"Keep me"},
              "relationships":{"author":{"data":{"type":"people","id":"%s"}},
                "reviewers":{"data":[{"type":"people","id":"%s"}]}}}}
            """
                .formatted(SeedData.ADA, SeedData.GRACE),
            "?include=author");

    assertThat(UUID.fromString(id)).isNotNull();
    var created = article(id);
    assertThat(created.at("/data/id").asString()).isEqualTo(id);
    assertThat(created.at("/data/attributes/title").asString()).isEqualTo("Database-backed draft");
    assertThat(created.at("/data/attributes/status").asString()).isEqualTo("DRAFT");
    assertThat(created.at("/data/relationships/author/data/id").asString())
        .isEqualTo(SeedData.ADA.toString());
    assertThat(ids(created.at("/data/relationships/reviewers/data")))
        .containsExactly(SeedData.GRACE.toString());

    var compound = getJson("/articles/" + id + "?include=author,reviewers");
    assertThat(ids(compound.at("/included")))
        .containsExactlyInAnyOrder(SeedData.ADA.toString(), SeedData.GRACE.toString());
    assertThat(compound.at("/included/0/attributes/name").isString()).isTrue();

    client
        .patch()
        .uri("/articles/{id}", id)
        .contentType(JSON_API_TYPE)
        .body(
            patchDocument(
                id, "\"attributes\":{\"title\":\"Published draft\",\"status\":\"PUBLISHED\"}"))
        .exchange()
        .expectStatus()
        .isOk();

    var published = article(id);
    assertThat(published.at("/data/attributes/title").asString()).isEqualTo("Published draft");
    assertThat(published.at("/data/attributes/status").asString()).isEqualTo("PUBLISHED");
    assertThat(published.at("/data/attributes/body").asString()).isEqualTo("Keep me");
    assertThat(published.at("/data/relationships/author/data/id").asString())
        .isEqualTo(SeedData.ADA.toString());

    client
        .patch()
        .uri("/articles/{id}", id)
        .contentType(JSON_API_TYPE)
        .body(
            patchDocument(
                id,
                """
                "attributes":{"body":null,"status":"DRAFT"},
                "relationships":{"author":{"data":null},
                  "reviewers":{"data":[{"type":"people","id":"%s"}]}}
                """
                    .formatted(SeedData.URSULA)))
        .exchange()
        .expectStatus()
        .isOk();

    var revised = article(id);
    assertThat(revised.at("/data/attributes/body").isNull()).isTrue();
    assertThat(revised.at("/data/relationships/author/data").isNull()).isTrue();
    assertThat(ids(revised.at("/data/relationships/reviewers/data")))
        .containsExactly(SeedData.URSULA.toString());

    client.delete().uri("/articles/{id}", id).exchange().expectStatus().isNoContent();
    expectError(client.get().uri("/articles/{id}", id).exchange(), 404, "ARTICLE_NOT_FOUND");
    expectError(
        client.get().uri("/articles/{id}/relationships/reviewers", id).exchange(),
        404,
        "ARTICLE_NOT_FOUND");
    assertThat(getJson("/people/" + SeedData.URSULA).at("/data/attributes/name").asString())
        .isEqualTo("Ursula K. Le Guin");
  }
}
