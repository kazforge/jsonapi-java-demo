package com.kazforge.jsonapi.demo.mapper;

import com.kazforge.jsonapi.demo.dto.ArticleResource;
import com.kazforge.jsonapi.demo.dto.PersonResource;
import com.kazforge.jsonapi.demo.entity.ArticleEntity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ArticleMapper {
  private final PersonMapper people;

  public ArticleMapper(PersonMapper people) {
    this.people = people;
  }

  /**
   * Maps one article. {@code includedPeople} holds the requested included people; any other
   * relationship value is written as identity-only linkage.
   */
  public ArticleResource toResource(
      ArticleEntity article,
      @Nullable UUID authorId,
      List<UUID> reviewerIds,
      Map<UUID, PersonResource> includedPeople) {
    PersonResource author = authorId == null ? null : person(authorId, includedPeople);
    List<PersonResource> reviewers =
        reviewerIds.stream().map(reviewerId -> person(reviewerId, includedPeople)).toList();
    return new ArticleResource(
        article.getId().toString(),
        article.getTitle(),
        article.getBody(),
        article.getStatus(),
        author,
        reviewers);
  }

  private PersonResource person(UUID id, Map<UUID, PersonResource> includedPeople) {
    return includedPeople.getOrDefault(id, people.toLinkage(id));
  }
}
