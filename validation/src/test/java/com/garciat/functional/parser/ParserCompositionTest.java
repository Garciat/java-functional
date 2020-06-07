package com.garciat.functional.parser;

import com.garciat.functional.data.Either;
import com.garciat.functional.functions.Functions;
import junit.framework.TestCase;
import lombok.Value;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static com.garciat.functional.functions.Functions.Curry.curry;
import static com.garciat.functional.functions.Functions.Reversed.reversed;

public class ParserCompositionTest extends TestCase {

  Parser<String, UUID> uuidParser = Parser.lift(UUID::fromString);
  Parser<String, Instant> instantParser = Parser.lift(Instant::parse);
  Parser<String, Currency> currencyParser = Parser.lift(Currency::getInstance);

  Parser<ThriftNested, ProperNested> nestedParser =
          ParserComposition.of(ThriftNested.class, ProperNested.class)
                  .with(Fields.required(ThriftNested::getName, Parser.id()))
                  .build(reversed(curry(ProperNested::new)));

  Parser<ThriftThing, ProperThing> thingParser =
          ParserComposition.of(ThriftThing.class, ProperThing.class)
                  .with(Fields.required(ThriftThing::getUuid, uuidParser))
                  .with(Fields.required(ThriftThing::getTimestamp, instantParser))
                  .with(Fields.optional(ThriftThing::getCurrencyCode, currencyParser))
                  .with(Fields.required(ThriftThing::getNested, nestedParser))
                  .build(Functions.Reversed.reversed(Functions.Curry.curry(ProperThing::new)));

  @Test
  public void good() {
    val input =
            new ThriftThing("1-1-1-1-1", "2020-05-20T10:23:31Z", "BOB", new ThriftNested("hello"));

    val result = thingParser.parse(input);

    val expected =
            Either.success(
                    new ProperThing(
                            UUID.fromString("00000001-0001-0001-0001-000000000001"),
                            Instant.parse("2020-05-20T10:23:31Z"),
                            Optional.of(Currency.getInstance("BOB")),
                            new ProperNested("hello")));
    assertEquals(expected, result);
  }

  @Value
  private static class ThriftThing {
    @Nullable String uuid;
    @Nullable String timestamp;
    @Nullable String currencyCode;
    @Nullable ThriftNested nested;
  }

  @Value
  private static class ThriftNested {
    @Nullable String name;
  }

  @Value
  private static class ProperThing {
    UUID uuid;
    Instant timestamp;
    Optional<Currency> currency;
    ProperNested nested;
  }

  @Value
  private static class ProperNested {
    String name;
  }
}