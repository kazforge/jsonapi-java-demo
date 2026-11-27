package com.kazforge.jsonapi.demo.dto;

import com.kazforge.jsonapi.annotation.JsonApiAttribute;
import com.kazforge.jsonapi.annotation.JsonApiId;
import com.kazforge.jsonapi.annotation.JsonApiRelationship;
import com.kazforge.jsonapi.annotation.JsonApiResource;
import com.kazforge.jsonapi.core.model.ResourceIdentifier;
import com.kazforge.jsonapi.demo.config.PublishingLimits;
import com.kazforge.jsonapi.demo.entity.ArticleEntity;
import com.kazforge.jsonapi.demo.entity.ArticleStatus;
import com.kazforge.jsonapi.patch.PatchPresence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = ArticleResource.TYPE)
public record ArticlePatchRequest(
    @JsonApiId String id,
    @JsonApiAttribute
        PatchPresence<
                @NotBlank @Size(max = ArticleEntity.MAX_TITLE_LENGTH)
                @Pattern(regexp = PublishingLimits.VALID_TEXT) String>
            title,
    @JsonApiAttribute
        PatchPresence<@Pattern(regexp = PublishingLimits.VALID_TEXT) @Nullable String> body,
    @JsonApiAttribute PatchPresence<@NotNull ArticleStatus> status,
    @JsonApiRelationship PatchPresence<@Nullable ResourceIdentifier> author,
    @JsonApiRelationship PatchPresence<@NotNull List<ResourceIdentifier>> reviewers) {}
