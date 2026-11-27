package com.kazforge.jsonapi.demo;

import java.util.UUID;

/** Stable identities from the Liquibase seed. */
final class SeedData {
  static final UUID ADA = UUID.fromString("11111111-1111-4111-8111-111111111111");
  static final UUID GRACE = UUID.fromString("22222222-2222-4222-8222-222222222222");
  static final UUID URSULA = UUID.fromString("33333333-3333-4333-8333-333333333333");
  static final UUID DRAFT_NOTES = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1");
  static final UUID API_GUIDE = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa2");
  static final UUID EDITORIAL_GUIDE = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa3");

  private SeedData() {}
}
