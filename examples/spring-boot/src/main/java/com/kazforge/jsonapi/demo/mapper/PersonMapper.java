package com.kazforge.jsonapi.demo.mapper;

import com.kazforge.jsonapi.core.model.ResourceIdentifier;
import com.kazforge.jsonapi.demo.dto.PersonResource;
import com.kazforge.jsonapi.demo.entity.PersonEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PersonMapper {
  public PersonResource toResource(PersonEntity person) {
    return new PersonResource(person.getId().toString(), person.getName());
  }

  /** Identity-only value for article linkage that will not appear in {@code included}. */
  public PersonResource toLinkage(UUID id) {
    return new PersonResource(id.toString(), null);
  }

  public ResourceIdentifier toIdentifier(UUID id) {
    return ResourceIdentifier.of(PersonResource.TYPE, id.toString());
  }
}
