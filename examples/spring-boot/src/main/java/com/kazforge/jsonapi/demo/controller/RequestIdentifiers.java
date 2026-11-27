package com.kazforge.jsonapi.demo.controller;

import com.kazforge.jsonapi.demo.exception.InvalidIdentifierException;
import com.kazforge.jsonapi.demo.service.UuidValues;
import java.util.UUID;

/** Converts wire identifiers for route variables into persistent UUIDs. */
final class RequestIdentifiers {
  private RequestIdentifiers() {}

  static UUID toUuid(String value) {
    return UuidValues.parse(value).orElseThrow(InvalidIdentifierException::new);
  }
}
