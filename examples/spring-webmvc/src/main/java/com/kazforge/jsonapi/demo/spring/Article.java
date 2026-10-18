package com.kazforge.jsonapi.demo.spring;

import com.kazforge.jsonapi.annotation.JsonApiAttribute;
import com.kazforge.jsonapi.annotation.JsonApiId;
import com.kazforge.jsonapi.annotation.JsonApiResource;

/** Create requests may omit the ID; the application supplies one in the response. */
@JsonApiResource(type = "articles")
public record Article(@JsonApiId String id, @JsonApiAttribute String title) {}
