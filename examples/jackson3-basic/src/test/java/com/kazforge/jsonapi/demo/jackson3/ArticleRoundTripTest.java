package com.kazforge.jsonapi.demo.jackson3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kazforge.jsonapi.annotation.JsonApiAttribute;
import com.kazforge.jsonapi.annotation.JsonApiId;
import com.kazforge.jsonapi.annotation.JsonApiLocalId;
import com.kazforge.jsonapi.annotation.JsonApiRelationship;
import com.kazforge.jsonapi.annotation.JsonApiResource;
import com.kazforge.jsonapi.api.JsonApi;
import com.kazforge.jsonapi.core.model.ResourceIdentifier;
import com.kazforge.jsonapi.core.validation.ValidationRuleCode;
import com.kazforge.jsonapi.diagnostic.CodecFailureCategory;
import com.kazforge.jsonapi.diagnostic.JsonApiDocumentReadException;
import com.kazforge.jsonapi.jackson3.JsonApiJackson3;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class ArticleRoundTripTest {
  private final JsonMapper mapper = JsonMapper.builder().build();
  private final JsonApi api = JsonApiJackson3.jsonApi(mapper);

  @Test
  void writesJsonApiDocumentAndReadsArticleBack() {
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

  @Test
  void readsIdlessCreateRequest() {
    String request =
        """
        {"data":{"type":"articles","attributes":{"title":"New","body":"Created"}}}
        """;

    NewArticle created = api.resources().readCreate(request, NewArticle.class);

    assertNull(created.id());
    assertEquals("New", created.title());
    assertEquals("Created", created.body());
  }

  @Test
  void readsCreateRequestWithLidSelfReference() {
    String request =
        """
        {"data":{"type":"articles","lid":"draft-1","attributes":{"title":"New","body":"Created"},"relationships":{"related":{"data":{"type":"articles","lid":"draft-1"}}}}}
        """;

    NewArticle created = api.resources().readCreate(request, NewArticle.class);

    assertEquals("draft-1", created.lid());
    assertEquals("articles", created.related().type());
    assertEquals("draft-1", created.related().lid());
  }

  @Test
  void rejectsCreateRelationshipWithoutData() {
    String request =
        """
        {"data":{"type":"articles","attributes":{"title":"New","body":"Created"},"relationships":{"related":{"meta":{}}}}}
        """;

    JsonApiDocumentReadException exception =
        assertThrows(
            JsonApiDocumentReadException.class,
            () -> api.resources().readCreate(request, NewArticle.class));

    assertEquals(CodecFailureCategory.AGGREGATE_VALIDATION, exception.category());
    assertEquals(ValidationRuleCode.RELATIONSHIP_DATA_REQUIRED, exception.ruleCode());
    assertEquals("/data/relationships/related/data", exception.jsonPointer());
  }

  @JsonApiResource(type = "articles")
  record NewArticle(
      @JsonApiId String id,
      @JsonApiLocalId String lid,
      @JsonApiAttribute String title,
      @JsonApiAttribute String body,
      @JsonApiRelationship ResourceIdentifier related) {}
}
