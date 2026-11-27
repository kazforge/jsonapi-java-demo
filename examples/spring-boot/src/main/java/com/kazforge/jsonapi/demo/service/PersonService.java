package com.kazforge.jsonapi.demo.service;

import com.kazforge.jsonapi.demo.dto.PersonResource;
import com.kazforge.jsonapi.demo.entity.PersonEntity;
import com.kazforge.jsonapi.demo.exception.PersonNotFoundException;
import com.kazforge.jsonapi.demo.mapper.PersonMapper;
import com.kazforge.jsonapi.demo.repository.PersonRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PersonService {
  private static final Sort BY_NAME = Sort.by("name", "id");

  private final PersonRepository personRepository;
  private final PersonMapper personMapper;

  public PersonService(PersonRepository personRepository, PersonMapper personMapper) {
    this.personRepository = personRepository;
    this.personMapper = personMapper;
  }

  public List<PersonResource> list() {
    return personRepository.findAll(BY_NAME).stream().map(personMapper::toResource).toList();
  }

  public PersonResource get(UUID id) {
    PersonEntity person = personRepository.findById(id).orElseThrow(PersonNotFoundException::new);
    return personMapper.toResource(person);
  }
}
