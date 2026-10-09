package com.kazforge.jsonapi.demo.jackson3;

import com.kazforge.jsonapi.annotation.JsonApiAttribute;
import com.kazforge.jsonapi.annotation.JsonApiId;
import com.kazforge.jsonapi.annotation.JsonApiResource;

@JsonApiResource(type = "articles")
public record Article(
    @JsonApiId String id, @JsonApiAttribute String title, @JsonApiAttribute String body) {}
