package com.kazforge.jsonapi.demo.service;

import java.util.Optional;
import java.util.UUID;

/** Parses canonical UUID strings at application boundaries without trusting the input shape. */
public final class UuidValues {
  private static final int CANONICAL_LENGTH = 36;

  private UuidValues() {}

  public static Optional<UUID> parse(String value) {
    if (value.length() != CANONICAL_LENGTH) {
      return Optional.empty();
    }
    try {
      return Optional.of(UUID.fromString(value));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }
}
