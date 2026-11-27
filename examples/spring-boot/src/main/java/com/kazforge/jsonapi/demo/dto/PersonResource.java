package com.kazforge.jsonapi.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kazforge.jsonapi.annotation.JsonApiAttribute;
import com.kazforge.jsonapi.annotation.JsonApiId;
import com.kazforge.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = PersonResource.TYPE)
public record PersonResource(
    @JsonApiId String id,
    @JsonApiAttribute @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable String name) {
  public static final String TYPE = "people";
}
