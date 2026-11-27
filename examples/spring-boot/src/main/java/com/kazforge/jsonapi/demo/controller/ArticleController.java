package com.kazforge.jsonapi.demo.controller;

import com.kazforge.jsonapi.demo.dto.ArticleCreateRequest;
import com.kazforge.jsonapi.demo.dto.ArticlePatchRequest;
import com.kazforge.jsonapi.demo.dto.ArticleQuery;
import com.kazforge.jsonapi.demo.dto.ArticleResource;
import com.kazforge.jsonapi.demo.dto.PersonResource;
import com.kazforge.jsonapi.demo.service.ArticleIncludeGuard;
import com.kazforge.jsonapi.demo.service.ArticleService;
import com.kazforge.jsonapi.spring.webmvc.JsonApiEndpoint;
import com.kazforge.jsonapi.spring.webmvc.JsonApiPatchTarget;
import com.kazforge.jsonapi.spring.webmvc.document.JsonApiResponse;
import com.kazforge.jsonapi.spring.webmvc.request.JsonApiQueryParams;
import com.kazforge.jsonapi.spring.webmvc.request.JsonApiRequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
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
@RequestMapping("/articles")
@JsonApiEndpoint
public class ArticleController {
  private final ArticleService articles;
  private final ArticleCollectionAssembler collectionAssembler;
  private final EmptyArticleAuthorResponse emptyAuthor;
  private final ArticleIncludeGuard includes;

  public ArticleController(
      ArticleService articles,
      ArticleCollectionAssembler collectionAssembler,
      EmptyArticleAuthorResponse emptyAuthor,
      ArticleIncludeGuard includes) {
    this.articles = articles;
    this.collectionAssembler = collectionAssembler;
    this.emptyAuthor = emptyAuthor;
    this.includes = includes;
  }

  @GetMapping
  public JsonApiResponse<List<ArticleResource>> search(
      @Valid @JsonApiQueryParams ArticleQuery query, HttpServletRequest request) {
    return collectionAssembler.assemble(articles.search(query), request);
  }

  @PostMapping
  public ResponseEntity<ArticleResource> create(
      @Valid @RequestBody ArticleCreateRequest request, JsonApiRequestContext context) {
    ArticleResource article = articles.create(request, context.representationSelection());
    return ResponseEntity.created(URI.create("/articles/" + article.id())).body(article);
  }

  @GetMapping("/{id}")
  public ArticleResource get(@PathVariable("id") String id, JsonApiRequestContext context) {
    return articles.get(RequestIdentifiers.toUuid(id), context.representationSelection());
  }

  @PatchMapping("/{id}")
  @JsonApiPatchTarget(ArticleResource.TYPE)
  public ArticleResource update(
      @PathVariable("id") String id,
      @Valid @RequestBody ArticlePatchRequest request,
      JsonApiRequestContext context) {
    return articles.update(
        RequestIdentifiers.toUuid(id), request, context.representationSelection());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") String id) {
    articles.delete(RequestIdentifiers.toUuid(id));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/author")
  public ResponseEntity<PersonResource> author(
      @PathVariable("id") String id, JsonApiRequestContext context, HttpServletResponse response)
      throws IOException {
    var selection = context.representationSelection();
    includes.requireNone(selection);
    PersonResource author = articles.author(RequestIdentifiers.toUuid(id));
    if (author == null) {
      // The writer commits {"data":null}; returning null stops Spring from writing a body.
      emptyAuthor.write(selection, response);
      return null;
    }
    return ResponseEntity.ok(author);
  }

  @GetMapping("/{id}/reviewers")
  public List<PersonResource> reviewers(@PathVariable("id") String id) {
    return articles.reviewers(RequestIdentifiers.toUuid(id));
  }
}
