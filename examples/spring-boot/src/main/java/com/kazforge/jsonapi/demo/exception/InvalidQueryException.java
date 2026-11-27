package com.kazforge.jsonapi.demo.exception;

public class InvalidQueryException extends RuntimeException {
  private final String parameter;
  private final String code;

  public InvalidQueryException(String parameter, String code, String title) {
    super(title);
    this.parameter = parameter;
    this.code = code;
  }

  public String parameter() {
    return parameter;
  }

  public String code() {
    return code;
  }
}
