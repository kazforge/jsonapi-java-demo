package com.kazforge.jsonapi.demo.exception;

public class InvalidIdentifierException extends RuntimeException {
  public InvalidIdentifierException() {
    super("Identifier is not a UUID");
  }
}
