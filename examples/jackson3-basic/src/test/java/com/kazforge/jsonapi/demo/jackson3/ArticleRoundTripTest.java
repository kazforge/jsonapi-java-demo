package com.kazforge.jsonapi.demo.jackson3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.kazforge.jsonapi.api.JsonApi;
import com.kazforge.jsonapi.jackson3.JsonApiJackson3;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class ArticleRoundTripTest {
  @Test
  void writesJsonApiDocumentAndReadsArticleBack() {
    JsonMapper mapper = JsonMapper.builder().build();
    JsonApi api = JsonApiJackson3.jsonApi(mapper);
    Article article = new Article("1", "Working with JSON:API", "Hello from a consumer.");

    String document = api.resources().writeOne(article);

    JsonNode data = mapper.readTree(document).path("data");
    assertEquals("articles", data.path("type").asString());
    assertEquals(article.id(), data.path("id").asString());
    assertEquals(article.title(), data.path("attributes").path("title").asString());
    assertEquals(article.body(), data.path("attributes").path("body").asString());

    Article readBack = api.resources().readOne(document, Article.class);
    assertEquals(article, readBack);
  }
}
