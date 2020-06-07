package com.garciat.functional.parser;

import com.garciat.functional.functions.Getter;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class Fields {

  private Fields() {}

  public static <A, B> Parser<A, B> required(Getter<A, B> getter) {
    return required(getter, Parser.id());
  }

  public static <A, B, C> Parser<A, C> required(Getter<A, B> getter, Parser<B, C> parser) {
    return Parser.liftNullable(getter)
            .andThen(Parsers.nonEmpty())
            .andThen(parser)
            .mapFailure(x -> ParseFailure.getter(getter, x));
  }

  public static <A, B> Parser<A, Optional<B>> optional(Getter<A, B> getter) {
    return optional(getter, Parser.id());
  }

  public static <A, B, C> Parser<A, Optional<C>> optional(
          Getter<A, B> getter, Parser<B, C> parser) {
    return Parser.liftNullable(getter)
            .andThen(Parser.liftOptional(parser))
            .mapFailure(x -> ParseFailure.getter(getter, x));
  }

  public static <A, B, C> Parser<A, C> optional(
          Getter<A, B> getter, Supplier<C> fallback, Parser<B, C> parser) {
    return Parser.liftNullable(getter)
            .andThen(Parser.liftOptional(parser))
            .andThen(Parsers.defaulting(fallback))
            .mapFailure(x -> ParseFailure.getter(getter, x));
  }

  /**
   * To be used by Thrift fields with primitive types
   *
   * <p>For example, {@code optional(MyThrift::getInt, MyThrift::isSetInt, ...)}
   */
  public static <A, B, C> Parser<A, C> optional(
          Getter<A, B> getter, Predicate<A> hasValue, Supplier<C> fallback, Parser<B, C> parser) {
    return Parser.<A, B>liftNullable(a -> hasValue.test(a) ? getter.apply(a) : null)
            .andThen(Parser.liftOptional(parser))
            .andThen(Parsers.defaulting(fallback))
            .mapFailure(x -> ParseFailure.getter(getter, x));
  }
}
