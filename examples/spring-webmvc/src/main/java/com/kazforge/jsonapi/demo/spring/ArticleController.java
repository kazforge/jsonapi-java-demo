package com.kazforge.jsonapi.demo.spring;

import com.kazforge.jsonapi.spring.webmvc.JsonApiEndpoint;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Stateless example; real applications own persistence and identifier allocation. */
@RestController
@JsonApiEndpoint
public final class ArticleController {
  @GetMapping("/articles/42")
  public Article one() {
    return new Article("42", "Hello JSON:API");
  }

  @GetMapping("/articles")
  public List<Article> many() {
    return List.of(one());
  }

  @PostMapping("/articles")
  public ResponseEntity<Article> create(@RequestBody Article input) {
    return ResponseEntity.created(URI.create("/articles/42"))
        .body(new Article("42", input.title()));
  }
}
