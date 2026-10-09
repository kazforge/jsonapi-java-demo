package com.kazforge.jsonapi.demo.jackson3;

import com.kazforge.jsonapi.api.JsonApi;
import com.kazforge.jsonapi.jackson3.JsonApiJackson3;
import tools.jackson.databind.json.JsonMapper;

public final class Jackson3Basic {
  public static void main(String[] args) {
    JsonMapper mapper = JsonMapper.builder().build();
    JsonApi api = JsonApiJackson3.jsonApi(mapper);

    Article article = new Article("1", "Working with JSON:API", "Hello from a consumer.");
    String document = api.resources().writeOne(article);
    Article readBack = api.resources().readOne(document, Article.class);

    if (!article.equals(readBack)) {
      throw new IllegalStateException("Article round trip did not preserve its values");
    }
    System.out.println(document);
    System.out.println(readBack);
  }
}
