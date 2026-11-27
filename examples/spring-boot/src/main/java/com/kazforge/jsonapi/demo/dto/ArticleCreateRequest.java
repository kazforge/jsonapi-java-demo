package com.kazforge.jsonapi.demo.dto;

import com.kazforge.jsonapi.annotation.JsonApiAttribute;
import com.kazforge.jsonapi.annotation.JsonApiId;
import com.kazforge.jsonapi.annotation.JsonApiRelationship;
import com.kazforge.jsonapi.annotation.JsonApiResource;
import com.kazforge.jsonapi.core.model.ResourceIdentifier;
import com.kazforge.jsonapi.demo.config.PublishingLimits;
import com.kazforge.jsonapi.demo.entity.ArticleEntity;
import com.kazforge.jsonapi.demo.entity.ArticleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = ArticleResource.TYPE)
public record ArticleCreateRequest(
    @JsonApiId @Nullable String id,
    @JsonApiAttribute
        @NotBlank
        @Size(max = ArticleEntity.MAX_TITLE_LENGTH)
        @Pattern(regexp = PublishingLimits.VALID_TEXT)
        String title,
    @JsonApiAttribute @Pattern(regexp = PublishingLimits.VALID_TEXT) @Nullable String body,
    @JsonApiAttribute @Nullable ArticleStatus status,
    @JsonApiRelationship @Nullable ResourceIdentifier author,
    @JsonApiRelationship @Nullable List<ResourceIdentifier> reviewers) {}
