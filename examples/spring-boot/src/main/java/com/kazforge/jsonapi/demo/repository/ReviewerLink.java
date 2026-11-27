package com.kazforge.jsonapi.demo.repository;

import java.util.UUID;

public interface ReviewerLink {
  UUID getArticleId();

  UUID getPersonId();
}
