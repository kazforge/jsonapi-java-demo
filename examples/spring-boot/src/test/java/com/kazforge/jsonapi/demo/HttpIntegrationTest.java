package com.kazforge.jsonapi.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = PostgresInitializer.class)
@Import(HttpIntegrationTest.OrdinaryController.class)
abstract class HttpIntegrationTest {
  protected static final String JSON_API = "application/vnd.api+json";
  protected static final MediaType JSON_API_TYPE = MediaType.parseMediaType(JSON_API);

  @LocalServerPort private int port;
  @Autowired private JsonMapper jsonMapper;
  private final List<String> createdArticleIds = new ArrayList<>();
  protected RestTestClient client;

  @BeforeEach
  void bindClient() {
    client =
        RestTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .defaultHeader("Accept", JSON_API)
            .build();
  }

  @AfterEach
  void deleteCreatedArticles() {
    for (String id : createdArticleIds) {
      var result = client.delete().uri("/articles/{id}", id).exchange().returnResult();
      assertThat(result.getStatus().value()).isIn(204, 404);
    }
  }

  protected String createArticle(String document) {
    return createArticle(document, "");
  }

  protected String createArticle(String document, String query) {
    var result =
        client
            .post()
            .uri(URI.create("/articles" + query))
            .contentType(JSON_API_TYPE)
            .body(document)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody()
            .returnResult();
    var created = json(result);
    String id = created.at("/data/id").asString();
    assertThat(result.getResponseHeaders().getFirst("Location")).isEqualTo("/articles/" + id);
    createdArticleIds.add(id);
    return id;
  }

  protected JsonNode getJson(String path) {
    var result =
        client
            .get()
            .uri(URI.create(path))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .returnResult();
    assertThat(result.getResponseHeaders().getContentType()).isEqualTo(JSON_API_TYPE);
    return json(result);
  }

  protected JsonNode article(String id) {
    return getJson("/articles/" + id);
  }

  protected JsonNode expectError(RestTestClient.ResponseSpec response, int status, String code) {
    var result = response.expectStatus().isEqualTo(status).expectBody().returnResult();
    assertThat(result.getResponseHeaders().getContentType()).isEqualTo(JSON_API_TYPE);
    var body = json(result);
    assertThat(body.has("data")).isFalse();
    assertThat(body.path("errors").isArray()).isTrue();
    var error = body.at("/errors/0");
    assertThat(error.at("/code").asString()).isEqualTo(code);
    return error;
  }

  protected JsonNode json(EntityExchangeResult<byte[]> result) {
    return jsonMapper.readTree(result.getResponseBody());
  }

  protected static String draftDocument(String title) {
    return """
        {"data":{"type":"articles","attributes":{"title":"%s"}}}
        """
        .formatted(title);
  }

  protected static String patchDocument(String id, String members) {
    return "{\"data\":{\"type\":\"articles\",\"id\":\"%s\",%s}}".formatted(id, members);
  }

  protected static String linkage(UUID... ids) {
    String data =
        Arrays.stream(ids)
            .map(id -> "{\"type\":\"people\",\"id\":\"%s\"}".formatted(id))
            .collect(Collectors.joining(","));
    return "{\"data\":[" + data + "]}";
  }

  protected static List<String> ids(JsonNode array) {
    return StreamSupport.stream(array.spliterator(), false)
        .map(node -> node.path("id").asString())
        .toList();
  }

  @RestController
  static class OrdinaryController {
    @GetMapping("/ordinary")
    Map<String, String> ordinary() {
      return Map.of("mode", "ordinary");
    }
  }
}
