package com.kazforge.jsonapi.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ArticlePatchHttpTest extends HttpIntegrationTest {
  @Test
  void keepsOmittedMembersAndClearsExplicitNulls() {
    String id =
        createArticle(
            """
            {"data":{"type":"articles","attributes":{"title":"Original","body":"Body"},
              "relationships":{"author":{"data":{"type":"people","id":"%s"}},
                "reviewers":{"data":[{"type":"people","id":"%s"}]}}}}
            """
                .formatted(SeedData.ADA, SeedData.GRACE));

    client
        .patch()
        .uri("/articles/{id}", id)
        .contentType(JSON_API_TYPE)
        .body(patchDocument(id, "\"attributes\":{\"title\":\"Renamed\"}"))
        .exchange()
        .expectStatus()
        .isOk();

    var renamed = article(id);
    assertThat(renamed.at("/data/attributes/title").asString()).isEqualTo("Renamed");
    assertThat(renamed.at("/data/attributes/body").asString()).isEqualTo("Body");
    assertThat(renamed.at("/data/relationships/author/data/id").asString())
        .isEqualTo(SeedData.ADA.toString());
    assertThat(ids(renamed.at("/data/relationships/reviewers/data")))
        .containsExactly(SeedData.GRACE.toString());

    client
        .patch()
        .uri("/articles/{id}", id)
        .contentType(JSON_API_TYPE)
        .body(
            patchDocument(
                id,
                """
                "attributes":{"body":null},"relationships":{"author":{"data":null}}
                """))
        .exchange()
        .expectStatus()
        .isOk();

    var cleared = article(id);
    assertThat(cleared.at("/data/attributes/body").isNull()).isTrue();
    assertThat(cleared.at("/data/relationships/author/data").isNull()).isTrue();
    assertThat(ids(cleared.at("/data/relationships/reviewers/data")))
        .containsExactly(SeedData.GRACE.toString());
  }

  @Test
  void rejectsInvalidPatchDocumentsWithoutChangingStoredState() {
    String id = createArticle(draftDocument("Unchanged"));
    String otherId = UUID.randomUUID().toString();

    var nullTitle =
        expectError(
            client
                .patch()
                .uri("/articles/{id}", id)
                .contentType(JSON_API_TYPE)
                .body(patchDocument(id, "\"attributes\":{\"title\":null}"))
                .exchange(),
            400,
            "INVALID_FIELD");
    assertThat(nullTitle.at("/source/pointer").asString()).isEqualTo("/data/attributes/title");

    expectError(
        client
            .patch()
            .uri("/articles/{id}", id)
            .contentType(JSON_API_TYPE)
            .body(patchDocument(id, "\"attributes\":{\"status\":null}"))
            .exchange(),
        400,
        "INVALID_FIELD");
    expectError(
        client
            .patch()
            .uri("/articles/{id}", id)
            .contentType(JSON_API_TYPE)
            .body(patchDocument(id, "\"attributes\":{\"unknown\":true}"))
            .exchange(),
        400,
        "UNKNOWN_PATCH_MEMBER");
    expectError(
        client
            .patch()
            .uri("/articles/{id}", id)
            .contentType(JSON_API_TYPE)
            .body(patchDocument(otherId, "\"attributes\":{\"title\":\"Wrong target\"}"))
            .exchange(),
        409,
        "ENDPOINT_IDENTITY_MISMATCH");
    expectError(
        client
            .patch()
            .uri("/articles/{id}", id)
            .contentType(JSON_API_TYPE)
            .body(
                """
                {"data":{"type":"people","id":"%s","attributes":{"title":"Wrong type"}}}
                """
                    .formatted(id))
            .exchange(),
        409,
        "ENDPOINT_IDENTITY_MISMATCH");

    assertThat(article(id).at("/data/attributes/title").asString()).isEqualTo("Unchanged");
  }

  @Test
  void enforcesThePublicationRuleOnEveryMutationPath() {
    String id = createArticle(draftDocument("Unpublished"));

    expectError(
        client
            .patch()
            .uri("/articles/{id}", id)
            .contentType(JSON_API_TYPE)
            .body(patchDocument(id, "\"attributes\":{\"status\":\"PUBLISHED\"}"))
            .exchange(),
        409,
        "AUTHOR_REQUIRED");
    assertThat(article(id).at("/data/attributes/status").asString()).isEqualTo("DRAFT");

    expectError(
        client
            .patch()
            .uri("/articles/{id}/relationships/author", SeedData.API_GUIDE)
            .contentType(JSON_API_TYPE)
            .body("{\"data\":null}")
            .exchange(),
        409,
        "AUTHOR_REQUIRED");
    assertThat(
            getJson("/articles/" + SeedData.API_GUIDE + "/relationships/author")
                .at("/data/id")
                .asString())
        .isEqualTo(SeedData.ADA.toString());
  }

  @Test
  void rejectsClientAssignedIds() {
    expectError(
        client
            .post()
            .uri("/articles")
            .contentType(JSON_API_TYPE)
            .body(
                """
                {"data":{"type":"articles","id":"client-chosen","attributes":{"title":"Client ID"}}}
                """)
            .exchange(),
        403,
        "CLIENT_ID_NOT_SUPPORTED");
  }
}
