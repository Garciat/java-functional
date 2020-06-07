package com.garciat.functional.data;

import com.garciat.functional.functions.NullableFunction;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<T, F> {

  private Either() {}

  public abstract <R> R match(Function<T, R> success, Function<F, R> failure);

  // semantic

  public <E extends Throwable> T orElseThrow(Function<F, E> handler) throws E {
    return match(
        Function.identity(),
        new Function<F, T>() {
          @SneakyThrows
          @Override
          public T apply(F f) {
            throw handler.apply(f);
          }
        });
  }

  // structural

  // Functor
  public <U> Either<U, F> map(Function<T, U> mapper) {
    return match(t -> success(mapper.apply(t)), Either::failure);
  }

  public <G> Either<T, G> mapFailure(Function<F, G> mapper) {
    return match(Either::success, f -> failure(mapper.apply(f)));
  }

  // Monad
  public <U> Either<U, F> flatMap(Function<T, Either<U, F>> callback) {
    return match(callback, Either::failure);
  }

  // constructors

  public static <T, F> Either<T, F> success(T t) {
    Objects.requireNonNull(t);
    return new Success<>(t);
  }

  public static <T, F> Either<T, F> failure(F f) {
    Objects.requireNonNull(f);
    return new Failure<>(f);
  }

  public static <T> Either<T, Throwable> run(Supplier<T> supplier) {
    Objects.requireNonNull(supplier);
    T value;
    try {
      value = supplier.get();
    } catch (Throwable t) {
      return failure(t);
    }
    return success(value);
  }

  public static <A, B> Function<A, Either<B, Throwable>> lift(Function<A, B> func) {
    return a -> run(() -> func.apply(a));
  }

  public static <A, B> Function<A, Either<Optional<B>, Throwable>> liftNullable(
      NullableFunction<A, B> func) {
    return a -> run(() -> Optional.ofNullable(func.apply(a)));
  }

  // combinators

  public static <F, A, B, C> Either<C, F> merge(
      Either<A, F> eitherA,
      Either<B, F> eitherB,
      BiFunction<A, B, C> valueMerge,
      BinaryOperator<F> failureMerge) {
    return eitherA.match(
        a -> eitherB.match(b -> Either.success(valueMerge.apply(a, b)), Either::failure),
        f1 ->
            eitherB.match(
                b -> Either.failure(f1), f2 -> Either.failure(failureMerge.apply(f1, f2))));
  }

  // data

  @Value
  @EqualsAndHashCode(callSuper=false)
  private static class Success<T, F> extends Either<T, F> {

    @NonNull T t;

    @Override
    public <R> R match(Function<T, R> success, Function<F, R> failure) {
      return success.apply(t);
    }

    @Override
    public String toString() {
      return "Either.success(" + t + ")";
    }
  }

  @Value
  @EqualsAndHashCode(callSuper=false)
  private static class Failure<T, F> extends Either<T, F> {

    @NonNull F f;

    @Override
    public <R> R match(Function<T, R> success, Function<F, R> failure) {
      return failure.apply(f);
    }

    @Override
    public String toString() {
      return "Either.failure(" + f + ")";
    }
  }
}
