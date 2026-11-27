package com.kazforge.jsonapi.demo.config;

import com.kazforge.jsonapi.representation.IncludePath;
import java.util.Set;

/** Include paths the article representation allows. */
public final class ArticleIncludes {
  public static final IncludePath AUTHOR = IncludePath.of("author");
  public static final IncludePath REVIEWERS = IncludePath.of("reviewers");
  public static final Set<IncludePath> ALLOWED = Set.of(AUTHOR, REVIEWERS);

  private ArticleIncludes() {}
}
