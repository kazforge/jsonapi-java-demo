package com.kazforge.jsonapi.demo.service;

import static java.util.stream.Collectors.toSet;

import com.kazforge.jsonapi.core.model.ResourceIdentifier;
import com.kazforge.jsonapi.demo.config.ArticleIncludes;
import com.kazforge.jsonapi.demo.dto.ArticleCreateRequest;
import com.kazforge.jsonapi.demo.dto.ArticlePatchRequest;
import com.kazforge.jsonapi.demo.dto.ArticleQuery;
import com.kazforge.jsonapi.demo.dto.ArticleResource;
import com.kazforge.jsonapi.demo.dto.PersonResource;
import com.kazforge.jsonapi.demo.entity.ArticleEntity;
import com.kazforge.jsonapi.demo.entity.ArticleStatus;
import com.kazforge.jsonapi.demo.entity.PersonEntity;
import com.kazforge.jsonapi.demo.exception.ArticleNotFoundException;
import com.kazforge.jsonapi.demo.exception.ClientAssignedIdException;
import com.kazforge.jsonapi.demo.exception.InvalidPersonIdentifierException;
import com.kazforge.jsonapi.demo.exception.RelatedPersonNotFoundException;
import com.kazforge.jsonapi.demo.mapper.ArticleMapper;
import com.kazforge.jsonapi.demo.mapper.PersonMapper;
import com.kazforge.jsonapi.demo.repository.ArticleRepository;
import com.kazforge.jsonapi.demo.repository.PersonRepository;
import com.kazforge.jsonapi.patch.PatchPresence;
import com.kazforge.jsonapi.representation.RepresentationSelection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArticleService {
  private static final String AUTHOR_POINTER = "/data/relationships/author/data";
  private static final String REVIEWERS_POINTER = "/data/relationships/reviewers/data";
  private static final String LINKAGE_POINTER = "/data";

  private final ArticleRepository articleRepository;
  private final PersonRepository personRepository;
  private final ArticleMapper articleMapper;
  private final PersonMapper personMapper;
  private final ArticleIncludeGuard includes;

  public ArticleService(
      ArticleRepository articleRepository,
      PersonRepository personRepository,
      ArticleMapper articleMapper,
      PersonMapper personMapper,
      ArticleIncludeGuard includes) {
    this.articleRepository = articleRepository;
    this.personRepository = personRepository;
    this.articleMapper = articleMapper;
    this.personMapper = personMapper;
    this.includes = includes;
  }

  public Page<ArticleResource> search(ArticleQuery query) {
    RepresentationSelection selection = query.requested().selection();
    includes.requireSupported(selection);
    Pageable pageable = ArticlePageRequest.of(query);
    Page<ArticleEntity> page =
        query.status() == null
            ? articleRepository.findAll(pageable)
            : articleRepository.findByStatus(query.status(), pageable);
    List<ArticleResource> resources = toResources(page.getContent(), selection);
    return new PageImpl<>(resources, pageable, page.getTotalElements());
  }

  public ArticleResource get(UUID id, RepresentationSelection selection) {
    includes.requireSupported(selection);
    return toResources(List.of(find(id)), selection).getFirst();
  }

  @Transactional
  public ArticleResource create(ArticleCreateRequest request, RepresentationSelection selection) {
    includes.requireSupported(selection);
    if (request.id() != null) {
      throw new ClientAssignedIdException();
    }
    ArticleStatus status = request.status() == null ? ArticleStatus.DRAFT : request.status();
    var article = new ArticleEntity(UUID.randomUUID(), request.title(), request.body(), status);
    article.assignAuthor(resolveAuthor(request.author(), AUTHOR_POINTER));
    article.requirePublishable();
    if (request.reviewers() != null) {
      article.replaceReviewers(resolveReviewers(request.reviewers(), REVIEWERS_POINTER));
    }
    return toResources(List.of(articleRepository.save(article)), selection).getFirst();
  }

  @Transactional
  public ArticleResource update(
      UUID id, ArticlePatchRequest request, RepresentationSelection selection) {
    includes.requireSupported(selection);
    ArticleEntity article = lock(id);
    if (request.title() instanceof PatchPresence.Present<String>(var title)) {
      article.changeTitle(title);
    }
    if (request.body() instanceof PatchPresence.Present<String>(var body)) {
      article.changeBody(body);
    }
    if (request.status() instanceof PatchPresence.Present<ArticleStatus>(var status)) {
      article.changeStatus(status);
    }
    if (request.author() instanceof PatchPresence.Present<ResourceIdentifier>(var author)) {
      article.assignAuthor(resolveAuthor(author, AUTHOR_POINTER));
    }
    article.requirePublishable();
    if (request.reviewers()
        instanceof PatchPresence.Present<List<ResourceIdentifier>>(var reviewers)) {
      article.replaceReviewers(resolveReviewers(reviewers, REVIEWERS_POINTER));
    }
    return toResources(List.of(article), selection).getFirst();
  }

  @Transactional
  public void delete(UUID id) {
    articleRepository.delete(lock(id));
  }

  public @Nullable PersonResource author(UUID id) {
    PersonEntity author = find(id).getAuthor();
    return author == null ? null : personMapper.toResource(author);
  }

  public List<PersonResource> reviewers(UUID id) {
    find(id);
    List<UUID> reviewerIds = reviewerIds(List.of(id)).getOrDefault(id, List.of());
    Map<UUID, PersonEntity> people = peopleById(Set.copyOf(reviewerIds));
    return reviewerIds.stream().map(people::get).map(personMapper::toResource).toList();
  }

  public @Nullable ResourceIdentifier authorLink(UUID id) {
    find(id);
    UUID authorId = authorIds(List.of(id)).get(id);
    return authorId == null ? null : personMapper.toIdentifier(authorId);
  }

  public List<ResourceIdentifier> reviewerLinks(UUID id) {
    find(id);
    return reviewerIds(List.of(id)).getOrDefault(id, List.of()).stream()
        .map(personMapper::toIdentifier)
        .toList();
  }

  @Transactional
  public void replaceAuthor(UUID id, @Nullable ResourceIdentifier author) {
    ArticleEntity article = lock(id);
    article.assignAuthor(resolveAuthor(author, LINKAGE_POINTER));
    article.requirePublishable();
  }

  @Transactional
  public void replaceReviewers(UUID id, List<ResourceIdentifier> reviewers) {
    lock(id).replaceReviewers(resolveReviewers(reviewers, LINKAGE_POINTER));
  }

  @Transactional
  public void addReviewers(UUID id, List<ResourceIdentifier> reviewers) {
    lock(id).addReviewers(resolveReviewers(reviewers, LINKAGE_POINTER));
  }

  @Transactional
  public void removeReviewers(UUID id, List<ResourceIdentifier> reviewers) {
    Set<UUID> personIds =
        reviewers.stream().map(reviewer -> personId(reviewer, LINKAGE_POINTER)).collect(toSet());
    lock(id).removeReviewers(personIds);
  }

  private ArticleEntity find(UUID id) {
    return articleRepository.findById(id).orElseThrow(ArticleNotFoundException::new);
  }

  private ArticleEntity lock(UUID id) {
    return articleRepository.findForUpdate(id).orElseThrow(ArticleNotFoundException::new);
  }

  private List<ArticleResource> toResources(
      List<ArticleEntity> articles, RepresentationSelection selection) {
    if (articles.isEmpty()) {
      return List.of();
    }
    List<UUID> articleIds = articles.stream().map(ArticleEntity::getId).toList();
    Map<UUID, UUID> authorIds = authorIds(articleIds);
    Map<UUID, List<UUID>> reviewerIds = reviewerIds(articleIds);
    Map<UUID, PersonResource> includedPeople = includedPeople(authorIds, reviewerIds, selection);
    return articles.stream()
        .map(
            article ->
                articleMapper.toResource(
                    article,
                    authorIds.get(article.getId()),
                    reviewerIds.getOrDefault(article.getId(), List.of()),
                    includedPeople))
        .toList();
  }

  private Map<UUID, UUID> authorIds(List<UUID> articleIds) {
    Map<UUID, UUID> result = new HashMap<>();
    articleRepository
        .findAuthorLinks(articleIds)
        .forEach(link -> result.put(link.getArticleId(), link.getPersonId()));
    return result;
  }

  private Map<UUID, List<UUID>> reviewerIds(List<UUID> articleIds) {
    Map<UUID, List<UUID>> result = new HashMap<>();
    articleRepository
        .findReviewerLinks(articleIds)
        .forEach(
            link ->
                result
                    .computeIfAbsent(link.getArticleId(), ignored -> new ArrayList<>())
                    .add(link.getPersonId()));
    return result;
  }

  private Map<UUID, PersonResource> includedPeople(
      Map<UUID, UUID> authorIds,
      Map<UUID, List<UUID>> reviewerIds,
      RepresentationSelection selection) {
    Set<UUID> ids = new LinkedHashSet<>();
    if (selection.includePaths().contains(ArticleIncludes.AUTHOR)) {
      ids.addAll(authorIds.values());
    }
    if (selection.includePaths().contains(ArticleIncludes.REVIEWERS)) {
      reviewerIds.values().forEach(ids::addAll);
    }
    if (ids.isEmpty()) {
      return Map.of();
    }
    Map<UUID, PersonResource> people = new HashMap<>();
    personRepository
        .findAllById(ids)
        .forEach(person -> people.put(person.getId(), personMapper.toResource(person)));
    return people;
  }

  private Map<UUID, PersonEntity> peopleById(Set<UUID> ids) {
    Map<UUID, PersonEntity> people = new HashMap<>();
    personRepository.findAllById(ids).forEach(person -> people.put(person.getId(), person));
    return people;
  }

  private @Nullable PersonEntity resolveAuthor(
      @Nullable ResourceIdentifier identifier, String pointer) {
    if (identifier == null) {
      return null;
    }
    UUID personId = personId(identifier, pointer);
    return personRepository
        .findById(personId)
        .orElseThrow(() -> new RelatedPersonNotFoundException(pointer));
  }

  private List<PersonEntity> resolveReviewers(
      List<ResourceIdentifier> identifiers, String pointer) {
    Set<UUID> personIds = new HashSet<>();
    for (ResourceIdentifier identifier : identifiers) {
      personIds.add(personId(identifier, pointer));
    }
    List<PersonEntity> people = personRepository.findAllById(personIds);
    if (people.size() != personIds.size()) {
      throw new RelatedPersonNotFoundException(pointer);
    }
    return people;
  }

  private static UUID personId(ResourceIdentifier identifier, String pointer) {
    if (!PersonResource.TYPE.equals(identifier.type()) || identifier.id() == null) {
      throw new InvalidPersonIdentifierException(pointer);
    }
    return UuidValues.parse(identifier.id())
        .orElseThrow(() -> new InvalidPersonIdentifierException(pointer));
  }
}
