package com.garciat.functional.parser;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public final class Parsers {

  private Parsers() {}

  public static <T> Parser<Optional<T>, T> nonEmpty() {
    return Parser.<Optional<T>>predicate(Optional::isPresent, "value is null")
        .map(Optional::get);
  }

  public static <T> Parser<Optional<T>, T> defaulting(Supplier<T> fallback) {
    return Parsers.<T>nonEmpty().recoverWith(Parser.returning(fallback));
  }

  public static Parser<String, UUID> uuid() {
    return Parser.lift(UUID::fromString);
  }

  public static Parser<String, Currency> currency() {
    return Parser.lift(Currency::getInstance);
  }

  public static Parser<String, Instant> iso8601() {
    return Parser.lift(Instant::parse);
  }

  public static Parser<String, ZoneId> zoneId() {
    return Parser.lift(ZoneId::of);
  }

  public static Parser<String, Locale> languageTag() {
    return Parser.lift(tag -> new Locale.Builder().setLanguageTag(tag).build());
  }

  public static <T extends Number> Parser<T, T> positive() {
    return Parser.predicate(x -> x.longValue() > 0, "value is negative");
  }
}
