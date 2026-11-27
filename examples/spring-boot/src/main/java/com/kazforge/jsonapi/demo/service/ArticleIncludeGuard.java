package com.kazforge.jsonapi.demo.service;

import com.kazforge.jsonapi.demo.config.ArticleIncludes;
import com.kazforge.jsonapi.query.JsonApiQueryException;
import com.kazforge.jsonapi.query.QueryDiagnostic;
import com.kazforge.jsonapi.representation.RepresentationSelection;
import org.springframework.stereotype.Component;

/** Rejects include paths the application does not serve before any work is performed. */
@Component
public class ArticleIncludeGuard {
  public void requireSupported(RepresentationSelection selection) {
    if (!ArticleIncludes.ALLOWED.containsAll(selection.includePaths())) {
      throw new JsonApiQueryException(
          QueryDiagnostic.DISALLOWED_INCLUDE_PATH,
          "include",
          "Supported includes: author, reviewers");
    }
  }

  public void requireNone(RepresentationSelection selection) {
    if (!selection.includePaths().isEmpty()) {
      throw new JsonApiQueryException(
          QueryDiagnostic.DISALLOWED_INCLUDE_PATH, "include", "People have no relationships");
    }
  }
}
