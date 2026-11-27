package com.kazforge.jsonapi.demo;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Starts one PostgreSQL 17 container for every Spring test context in the build. */
public class PostgresInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17");

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    if (!POSTGRES.isRunning()) {
      POSTGRES.start();
    }
    TestPropertyValues.of(
            "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
            "spring.datasource.username=" + POSTGRES.getUsername(),
            "spring.datasource.password=" + POSTGRES.getPassword())
        .applyTo(applicationContext);
  }
}
