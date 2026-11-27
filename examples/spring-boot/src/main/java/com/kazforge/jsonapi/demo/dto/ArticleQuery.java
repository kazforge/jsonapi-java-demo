package com.kazforge.jsonapi.demo.dto;

import com.kazforge.jsonapi.demo.config.PublishingLimits;
import com.kazforge.jsonapi.demo.entity.ArticleStatus;
import com.kazforge.jsonapi.query.JsonApiQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.BindParam;

/** Declarative collection query. Pageable and sort conversion belongs to the service layer. */
public record ArticleQuery(
    @BindParam("filter[status]") @Nullable ArticleStatus status,
    @BindParam("page[number]") @Min(1) @Nullable Integer number,
    @BindParam("page[size]") @Min(1) @Max(PublishingLimits.MAX_PAGE_SIZE) @Nullable Integer size,
    JsonApiQuery requested) {}
