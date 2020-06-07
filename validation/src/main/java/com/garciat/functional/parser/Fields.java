package com.garciat.functional.parser;

import com.garciat.functional.functions.Getter;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Fields {

  private Fields() {}

  public static <A, B> Parser<A, B> required(Getter<A, B> getter) {
    return required(getter, Parser.id());
  }

  public static <A, B, C> Parser<A, C> required(Getter<A, B> getter, Parser<B, C> parser) {
    return Parser.liftNullable(getter)
        .andThen(Parsers.nonEmpty())
        .andThen(parser)
        .tagged(getter.getInfo().getPropertyName());
  }

  public static <A, B> Parser<A, Optional<B>> optional(Getter<A, B> getter) {
    return optional(getter, Parser.id());
  }

  public static <A, B, C> Parser<A, Optional<C>> optional(
      Getter<A, B> getter, Parser<B, C> parser) {
    return Parser.liftNullable(getter)
        .andThen(Parser.liftOptional(parser))
        .tagged(getter.getInfo().getPropertyName());
  }

  public static <A, B, C> Parser<A, C> optional(
      Getter<A, B> getter, Supplier<C> fallback, Parser<B, C> parser) {
    return Parser.liftNullable(getter)
        .andThen(Parser.liftOptional(parser))
        .andThen(Parsers.defaulting(fallback))
        .tagged(getter.getInfo().getPropertyName());
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
        .tagged(getter.getInfo().getPropertyName());
  }
}
