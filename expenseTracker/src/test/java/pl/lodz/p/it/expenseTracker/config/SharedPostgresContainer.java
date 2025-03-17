package pl.lodz.p.it.expenseTracker.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class SharedPostgresContainer {
  private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
          .withDatabaseName("test")
          .withUsername("test")
          .withPassword("test")
          .withInitScript("test.sql");

  static {
    postgres.start();
  }

  public static PostgreSQLContainer<?> getInstance() {
    return postgres;
  }
}