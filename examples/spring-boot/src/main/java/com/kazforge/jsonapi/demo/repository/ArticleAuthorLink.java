package com.kazforge.jsonapi.demo.repository;

import java.util.UUID;

public interface ArticleAuthorLink {
  UUID getArticleId();

  UUID getPersonId();
}
