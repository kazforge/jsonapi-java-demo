package com.kazforge.jsonapi.demo.entity;

import com.kazforge.jsonapi.demo.exception.PublicationRequiresAuthorException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "articles")
public class ArticleEntity {
  public static final int MAX_TITLE_LENGTH = 200;

  @Id private UUID id;

  @Column(nullable = false, length = MAX_TITLE_LENGTH)
  private String title;

  @Column(columnDefinition = "text")
  private @Nullable String body;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ArticleStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private @Nullable PersonEntity author;

  @ManyToMany
  @JoinTable(
      name = "article_reviewers",
      joinColumns = @JoinColumn(name = "article_id"),
      inverseJoinColumns = @JoinColumn(name = "person_id"))
  private Set<PersonEntity> reviewers = new HashSet<>();

  protected ArticleEntity() {}

  public ArticleEntity(UUID id, String title, @Nullable String body, ArticleStatus status) {
    this.id = id;
    this.title = title;
    this.body = body;
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public @Nullable String getBody() {
    return body;
  }

  public ArticleStatus getStatus() {
    return status;
  }

  public @Nullable PersonEntity getAuthor() {
    return author;
  }

  public void changeTitle(String title) {
    this.title = title;
  }

  public void changeBody(@Nullable String body) {
    this.body = body;
  }

  public void changeStatus(ArticleStatus status) {
    this.status = status;
  }

  public void assignAuthor(@Nullable PersonEntity author) {
    this.author = author;
  }

  public void requirePublishable() {
    if (status == ArticleStatus.PUBLISHED && author == null) {
      throw new PublicationRequiresAuthorException();
    }
  }

  public void replaceReviewers(Collection<PersonEntity> people) {
    reviewers.clear();
    reviewers.addAll(people);
  }

  public void addReviewers(Collection<PersonEntity> people) {
    Set<UUID> current = new HashSet<>();
    reviewers.forEach(reviewer -> current.add(reviewer.getId()));
    people.stream().filter(person -> !current.contains(person.getId())).forEach(reviewers::add);
  }

  public void removeReviewers(Collection<UUID> personIds) {
    reviewers.removeIf(reviewer -> personIds.contains(reviewer.getId()));
  }
}
