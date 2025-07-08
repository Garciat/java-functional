package com.garciat.functional.parser;

import com.garciat.functional.data.Either;
import com.garciat.functional.functions.Functions;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static com.garciat.functional.functions.Functions.Curry.curry;
import static com.garciat.functional.functions.Functions.Reversed.reversed;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserCompositionTest {

    Parser<String, UUID> uuidParser = Parser.lift(UUID::fromString);
    Parser<String, Instant> instantParser = Parser.lift(Instant::parse);
    Parser<String, Currency> currencyParser = Parser.lift(Currency::getInstance);

    Parser<ThriftNested, ProperNested> nestedParser =
            ParserComposition.of(ThriftNested.class, ProperNested.class)
                    .with(Fields.required(ThriftNested::name, Parser.id()))
                    .build(reversed(curry(ProperNested::new)));

    Parser<ThriftThing, ProperThing> thingParser =
            ParserComposition.of(ThriftThing.class, ProperThing.class)
                    .with(Fields.required(ThriftThing::uuid, uuidParser))
                    .with(Fields.required(ThriftThing::timestamp, instantParser))
                    .with(Fields.optional(ThriftThing::currencyCode, currencyParser))
                    .with(Fields.required(ThriftThing::nested, nestedParser))
                    .build(Functions.Reversed.reversed(Functions.Curry.curry(ProperThing::new)));

    @Test
    public void good() {
        var input =
                new ThriftThing("1-1-1-1-1", "2020-05-20T10:23:31Z", "BOB", new ThriftNested("hello"));

        var result = thingParser.parse(input);

        var expected =
                Either.success(
                        new ProperThing(
                                UUID.fromString("00000001-0001-0001-0001-000000000001"),
                                Instant.parse("2020-05-20T10:23:31Z"),
                                Optional.of(Currency.getInstance("BOB")),
                                new ProperNested("hello")));
        assertEquals(expected, result);
    }

    private record ThriftThing(
            @Nullable String uuid,
            @Nullable String timestamp,
            @Nullable String currencyCode,
            @Nullable ThriftNested nested) {
    }

    private record ThriftNested(@Nullable String name) {
    }

    private record ProperThing(
            UUID uuid,
            Instant timestamp,
            Optional<Currency> currency,
            ProperNested nested
    ) {
    }

    private record ProperNested(
            String name
    ) {
    }
}