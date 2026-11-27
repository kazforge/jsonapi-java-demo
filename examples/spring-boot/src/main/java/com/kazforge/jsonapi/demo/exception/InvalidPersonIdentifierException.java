package com.kazforge.jsonapi.demo.exception;

public class InvalidPersonIdentifierException extends RuntimeException {
  private final String pointer;

  public InvalidPersonIdentifierException(String pointer) {
    super("Expected a people identifier with a persistent ID");
    this.pointer = pointer;
  }

  public String pointer() {
    return pointer;
  }
}
