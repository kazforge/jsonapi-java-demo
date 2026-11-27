package com.kazforge.jsonapi.demo.exception;

public class RelatedPersonNotFoundException extends RuntimeException {
  private final String pointer;

  public RelatedPersonNotFoundException(String pointer) {
    super("Related person not found");
    this.pointer = pointer;
  }

  public String pointer() {
    return pointer;
  }
}
