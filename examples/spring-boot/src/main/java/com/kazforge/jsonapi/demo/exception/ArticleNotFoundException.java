package com.kazforge.jsonapi.demo.exception;

public class ArticleNotFoundException extends RuntimeException {
  public ArticleNotFoundException() {
    super("Article not found");
  }
}
