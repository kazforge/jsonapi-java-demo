package com.kazforge.jsonapi.demo.service;

import com.kazforge.jsonapi.demo.config.PublishingLimits;
import com.kazforge.jsonapi.demo.dto.ArticleQuery;
import com.kazforge.jsonapi.demo.exception.InvalidQueryException;
import com.kazforge.jsonapi.query.SortDirection;
import com.kazforge.jsonapi.query.SortField;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Converts the declarative query DTO into the repository page request. */
final class ArticlePageRequest {
  private static final String PAGE_NUMBER = "page[number]";
  private static final String SORT = "sort";

  private ArticlePageRequest() {}

  static Pageable of(ArticleQuery query) {
    int page = query.number() == null ? 0 : query.number() - 1;
    int size = query.size() == null ? PublishingLimits.DEFAULT_PAGE_SIZE : query.size();
    if ((long) page * size > Integer.MAX_VALUE) {
      throw new InvalidQueryException(
          PAGE_NUMBER, "INVALID_QUERY_PARAMETER", "Page offset is too large");
    }
    return PageRequest.of(page, size, Sort.by(orders(query.requested().sort())));
  }

  private static List<Sort.Order> orders(List<SortField> fields) {
    List<Sort.Order> orders = new ArrayList<>();
    for (SortField field : fields) {
      if (!PublishingLimits.SORTABLE_FIELDS.contains(field.field())) {
        throw new InvalidQueryException(SORT, "UNSUPPORTED_SORT_FIELD", "Sort by title or status");
      }
      Sort.Direction direction =
          field.direction() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC;
      orders.add(new Sort.Order(direction, field.field()));
    }
    orders.add(Sort.Order.asc("id"));
    return orders;
  }
}
