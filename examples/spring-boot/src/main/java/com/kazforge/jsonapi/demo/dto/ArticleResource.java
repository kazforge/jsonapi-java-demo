package com.kazforge.jsonapi.demo.dto;

import com.kazforge.jsonapi.annotation.JsonApiAttribute;
import com.kazforge.jsonapi.annotation.JsonApiId;
import com.kazforge.jsonapi.annotation.JsonApiRelationship;
import com.kazforge.jsonapi.annotation.JsonApiResource;
import com.kazforge.jsonapi.demo.entity.ArticleStatus;
import java.util.List;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = ArticleResource.TYPE)
public record ArticleResource(
    @JsonApiId String id,
    @JsonApiAttribute String title,
    @JsonApiAttribute @Nullable String body,
    @JsonApiAttribute ArticleStatus status,
    @JsonApiRelationship @Nullable PersonResource author,
    @JsonApiRelationship List<PersonResource> reviewers) {
  public static final String TYPE = "articles";
}
