package com.kazforge.jsonapi.demo.exception;

public class ClientAssignedIdException extends RuntimeException {
  public ClientAssignedIdException() {
    super("Article IDs are assigned by the server");
  }
}
