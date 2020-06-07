package com.garciat.functional.parser;

import com.garciat.functional.data.Either;
import com.garciat.functional.functions.NullableFunction;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Parser<T, R> {

  Either<R, ParseFailure> parse(T input);

  // structural

  // Functor
  default <S> Parser<T, S> map(Function<R, S> mapper) {
    return andThen(lift(mapper));
  }

  default Parser<T, R> mapFailure(Function<ParseFailure, ParseFailure> mapper) {
    return input -> parse(input).mapFailure(mapper);
  }

  // Applicative
  default <S> Parser<T, S> applyTo(Parser<T, Function<R, S>> next) {
    return merge(this, next, (r, rs) -> rs.apply(r));
  }

  // Alternative
  default Parser<T, R> recoverWith(Parser<T, R> fallback) {
    return input -> parse(input).match(Either::success, $ -> fallback.parse(input));
  }

  // Monad (bind)
  default <S> Parser<T, S> flatMap(Function<R, Parser<T, S>> callback) {
    return input -> parse(input).flatMap(t -> callback.apply(t).parse(input));
  }

  // Category (composition)
  default <S> Parser<T, S> andThen(Parser<R, S> next) {
    return input -> parse(input).flatMap(next::parse);
  }

  // semantic

  default Parser<T, R> filter(Predicate<R> predicate, String message) {
    return andThen(predicate(predicate, message));
  }

  default Parser<T, R> tagged(String tag) {
    return mapFailure(f -> ParseFailure.tag(tag, f));
  }

  // constructors

  // Category (identity)
  static <T> Parser<T, T> id() {
    return input -> Either.success(input);
  }

  // Applicative (pure)
  static <T, R> Parser<T, R> returning(R r) {
    return input -> Either.success(r);
  }

  static <T, R> Parser<T, R> returning(Supplier<R> r) {
    return input -> Either.success(r.get());
  }

  static <T, R> Parser<T, R> fail() {
    return fail(ParseFailure.message("fail"));
  }

  static <T, R> Parser<T, R> fail(ParseFailure failure) {
    return input -> Either.failure(failure);
  }

  static <T, R> Parser<T, R> lift(Function<T, R> function) {
    return input -> Either.lift(function).apply(input).mapFailure(ParseFailure::exception);
  }

  static <T, R> Parser<T, Optional<R>> liftNullable(NullableFunction<T, R> function) {
    return input -> Either.liftNullable(function).apply(input).mapFailure(ParseFailure::exception);
  }

  static <T, R> Parser<T, R> liftPure(Function<T, R> function) {
    return input -> Either.success(function.apply(input));
  }

  static <T, R> Parser<Optional<T>, Optional<R>> liftOptional(Parser<T, R> parser) {
    return input ->
        input.isPresent()
            ? parser.parse(input.get()).map(Optional::of)
            : Either.success(Optional.empty());
  }

  static <T> Parser<T, T> predicate(Predicate<T> predicate, String message) {
    return input ->
        predicate.test(input)
            ? Either.success(input)
            : Either.failure(ParseFailure.message(message));
  }

  // combinators

  // Applicative
  static <T, A, B, C> Parser<T, C> merge(
      Parser<T, A> left, Parser<T, B> right, BiFunction<A, B, C> merger) {
    return input ->
        Either.merge(left.parse(input), right.parse(input), merger, ParseFailure::merge);
  }
}
