package com.kazforge.jsonapi.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "people")
public class PersonEntity {
  public static final int MAX_NAME_LENGTH = 200;

  @Id private UUID id;

  @Column(nullable = false, length = MAX_NAME_LENGTH)
  private String name;

  protected PersonEntity() {}

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
