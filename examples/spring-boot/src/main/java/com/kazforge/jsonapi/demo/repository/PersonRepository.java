package com.kazforge.jsonapi.demo.repository;

import com.kazforge.jsonapi.demo.entity.PersonEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<PersonEntity, UUID> {}
