package com.kazforge.jsonapi.demo.repository;

import com.kazforge.jsonapi.demo.entity.ArticleEntity;
import com.kazforge.jsonapi.demo.entity.ArticleStatus;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<ArticleEntity, UUID> {
  Page<ArticleEntity> findByStatus(ArticleStatus status, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select article from ArticleEntity article where article.id = :id")
  Optional<ArticleEntity> findForUpdate(@Param("id") UUID id);

  @Query(
      """
      select article.id as articleId, reviewer.id as personId
      from ArticleEntity article join article.reviewers reviewer
      where article.id in :articleIds
      order by reviewer.id
      """)
  List<ReviewerLink> findReviewerLinks(@Param("articleIds") Collection<UUID> articleIds);

  @Query(
      """
      select article.id as articleId, article.author.id as personId
      from ArticleEntity article
      where article.id in :articleIds and article.author is not null
      """)
  List<ArticleAuthorLink> findAuthorLinks(@Param("articleIds") Collection<UUID> articleIds);
}
