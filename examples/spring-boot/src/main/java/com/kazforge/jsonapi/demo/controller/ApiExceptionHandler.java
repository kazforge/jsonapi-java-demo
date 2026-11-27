package com.kazforge.jsonapi.demo.controller;

import com.kazforge.jsonapi.core.model.ErrorObject;
import com.kazforge.jsonapi.core.model.ErrorSource;
import com.kazforge.jsonapi.demo.exception.ArticleNotFoundException;
import com.kazforge.jsonapi.demo.exception.ClientAssignedIdException;
import com.kazforge.jsonapi.demo.exception.InvalidIdentifierException;
import com.kazforge.jsonapi.demo.exception.InvalidPersonIdentifierException;
import com.kazforge.jsonapi.demo.exception.InvalidQueryException;
import com.kazforge.jsonapi.demo.exception.PersonNotFoundException;
import com.kazforge.jsonapi.demo.exception.PublicationRequiresAuthorException;
import com.kazforge.jsonapi.demo.exception.RelatedPersonNotFoundException;
import com.kazforge.jsonapi.spring.webmvc.error.JsonApiErrorResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Application-owned errors for JSON:API controllers only; ordinary MVC keeps its handling. */
@RestControllerAdvice(
    assignableTypes = {
      ArticleController.class,
      ArticleRelationshipController.class,
      PersonController.class
    })
public class ApiExceptionHandler {
  @ExceptionHandler(ArticleNotFoundException.class)
  ResponseEntity<JsonApiErrorResponse> articleNotFound(ArticleNotFoundException exception) {
    return error(HttpStatus.NOT_FOUND, "ARTICLE_NOT_FOUND", exception.getMessage(), null);
  }

  @ExceptionHandler(PersonNotFoundException.class)
  ResponseEntity<JsonApiErrorResponse> personNotFound(PersonNotFoundException exception) {
    return error(HttpStatus.NOT_FOUND, "PERSON_NOT_FOUND", exception.getMessage(), null);
  }

  @ExceptionHandler(RelatedPersonNotFoundException.class)
  ResponseEntity<JsonApiErrorResponse> relatedPersonNotFound(
      RelatedPersonNotFoundException exception) {
    return error(
        HttpStatus.NOT_FOUND,
        "PERSON_NOT_FOUND",
        exception.getMessage(),
        ErrorSource.builder().pointer(exception.pointer()).build());
  }

  @ExceptionHandler(InvalidPersonIdentifierException.class)
  ResponseEntity<JsonApiErrorResponse> invalidPersonIdentifier(
      InvalidPersonIdentifierException exception) {
    return error(
        HttpStatus.BAD_REQUEST,
        "INVALID_PERSON_IDENTIFIER",
        exception.getMessage(),
        ErrorSource.builder().pointer(exception.pointer()).build());
  }

  @ExceptionHandler(InvalidIdentifierException.class)
  ResponseEntity<JsonApiErrorResponse> invalidIdentifier(InvalidIdentifierException exception) {
    return error(HttpStatus.BAD_REQUEST, "INVALID_IDENTIFIER", exception.getMessage(), null);
  }

  @ExceptionHandler(PublicationRequiresAuthorException.class)
  ResponseEntity<JsonApiErrorResponse> publicationRequiresAuthor(
      PublicationRequiresAuthorException exception) {
    return error(HttpStatus.CONFLICT, "AUTHOR_REQUIRED", exception.getMessage(), null);
  }

  @ExceptionHandler(ClientAssignedIdException.class)
  ResponseEntity<JsonApiErrorResponse> clientAssignedId(ClientAssignedIdException exception) {
    return error(
        HttpStatus.FORBIDDEN,
        "CLIENT_ID_NOT_SUPPORTED",
        exception.getMessage(),
        ErrorSource.builder().pointer("/data/id").build());
  }

  @ExceptionHandler(InvalidQueryException.class)
  ResponseEntity<JsonApiErrorResponse> invalidQuery(InvalidQueryException exception) {
    return error(
        HttpStatus.BAD_REQUEST,
        exception.code(),
        exception.getMessage(),
        ErrorSource.builder().parameter(exception.parameter()).build());
  }

  private static ResponseEntity<JsonApiErrorResponse> error(
      HttpStatus status, String code, String title, @Nullable ErrorSource source) {
    var error =
        ErrorObject.builder()
            .status(Integer.toString(status.value()))
            .code(code)
            .title(title)
            .source(source)
            .build();
    return ResponseEntity.status(status).body(JsonApiErrorResponse.of(error));
  }
}
