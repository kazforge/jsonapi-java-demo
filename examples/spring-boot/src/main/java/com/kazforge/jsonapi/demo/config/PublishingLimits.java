package com.kazforge.jsonapi.demo.config;

import java.util.Set;

/** Application-owned API limits and the text values PostgreSQL can store. */
public final class PublishingLimits {
  public static final int DEFAULT_PAGE_SIZE = 20;
  public static final int MAX_PAGE_SIZE = 100;
  public static final String VALID_TEXT = "[^\\x00]*";
  public static final Set<String> SORTABLE_FIELDS = Set.of("title", "status");

  private PublishingLimits() {}
}
