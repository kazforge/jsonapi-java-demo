package com.kazforge.jsonapi.demo.exception;

public class PublicationRequiresAuthorException extends RuntimeException {
  public PublicationRequiresAuthorException() {
    super("A published article requires an author");
  }
}
