package com.kazforge.jsonapi.demo.controller;

import com.kazforge.jsonapi.core.model.ResourceIdentifier;
import com.kazforge.jsonapi.core.validation.PrimaryDataContext;
import com.kazforge.jsonapi.demo.service.ArticleService;
import com.kazforge.jsonapi.spring.webmvc.JsonApiEndpoint;
import com.kazforge.jsonapi.spring.webmvc.document.JsonApiToOneRelationship;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles/{id}/relationships")
@JsonApiEndpoint(primaryDataContext = PrimaryDataContext.RELATIONSHIP)
public class ArticleRelationshipController {
  private final ArticleService articles;

  public ArticleRelationshipController(ArticleService articles) {
    this.articles = articles;
  }

  @GetMapping("/author")
  public JsonApiToOneRelationship author(@PathVariable("id") String id) {
    return new JsonApiToOneRelationship(articles.authorLink(RequestIdentifiers.toUuid(id)));
  }

  @PatchMapping("/author")
  public ResponseEntity<Void> replaceAuthor(
      @PathVariable("id") String id, @RequestBody JsonApiToOneRelationship author) {
    articles.replaceAuthor(RequestIdentifiers.toUuid(id), author.identifier());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/reviewers")
  public List<ResourceIdentifier> reviewers(@PathVariable("id") String id) {
    return articles.reviewerLinks(RequestIdentifiers.toUuid(id));
  }

  @PatchMapping("/reviewers")
  public ResponseEntity<Void> replaceReviewers(
      @PathVariable("id") String id, @RequestBody List<ResourceIdentifier> reviewers) {
    articles.replaceReviewers(RequestIdentifiers.toUuid(id), reviewers);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/reviewers")
  public ResponseEntity<Void> addReviewers(
      @PathVariable("id") String id, @RequestBody List<ResourceIdentifier> reviewers) {
    articles.addReviewers(RequestIdentifiers.toUuid(id), reviewers);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/reviewers")
  public ResponseEntity<Void> removeReviewers(
      @PathVariable("id") String id, @RequestBody List<ResourceIdentifier> reviewers) {
    articles.removeReviewers(RequestIdentifiers.toUuid(id), reviewers);
    return ResponseEntity.noContent().build();
  }
}
