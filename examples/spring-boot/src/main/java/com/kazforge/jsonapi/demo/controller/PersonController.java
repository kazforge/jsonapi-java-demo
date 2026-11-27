package com.kazforge.jsonapi.demo.controller;

import com.kazforge.jsonapi.demo.dto.PersonResource;
import com.kazforge.jsonapi.demo.service.PersonService;
import com.kazforge.jsonapi.spring.webmvc.JsonApiEndpoint;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/people")
@JsonApiEndpoint
public class PersonController {
  private final PersonService people;

  public PersonController(PersonService people) {
    this.people = people;
  }

  @GetMapping
  public List<PersonResource> list() {
    return people.list();
  }

  @GetMapping("/{id}")
  public PersonResource get(@PathVariable("id") String id) {
    return people.get(RequestIdentifiers.toUuid(id));
  }
}
