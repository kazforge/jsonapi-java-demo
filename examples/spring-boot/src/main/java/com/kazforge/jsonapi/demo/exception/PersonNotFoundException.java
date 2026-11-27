package com.kazforge.jsonapi.demo.exception;

public class PersonNotFoundException extends RuntimeException {
  public PersonNotFoundException() {
    super("Person not found");
  }
}
