package com.garciat.functional.data;

import com.garciat.functional.functions.NullableFunction;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Either<T, F> {

    static <T, F> Either<T, F> success(T t) {
        Objects.requireNonNull(t);
        return new Success<>(t);
    }

    // semantic

    static <T, F> Either<T, F> failure(F f) {
        Objects.requireNonNull(f);
        return new Failure<>(f);
    }

    static <T> Either<T, Throwable> run(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        T value;
        try {
            value = supplier.get();
        } catch (Throwable t) {
            return failure(t);
        }
        return success(value);
    }

    static <A, B> Function<A, Either<B, Throwable>> lift(Function<A, B> func) {
        return a -> run(() -> func.apply(a));
    }

    // structural

    static <A, B> Function<A, Either<Optional<B>, Throwable>> liftNullable(
            NullableFunction<A, B> func) {
        return a -> run(() -> Optional.ofNullable(func.apply(a)));
    }

    static <F, A, B, C> Either<C, F> merge(
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

    <R> R match(Function<T, R> success, Function<F, R> failure);

    // constructors

    default <E extends Throwable> T orElseThrow(Function<F, E> handler) throws E {
        return match(
                Function.identity(),
                new Function<F, T>() {
                    @Override
                    public T apply(F f) {
                        try {
                            throw handler.apply(f);
                        } catch (Throwable e) {
                            throw new RuntimeException(e); // TODO
                        }
                    }
                });
    }

    default T unsafeAssumeSuccess() throws IllegalStateException {
        return match(Function.identity(), $ -> {
            throw new IllegalStateException("Expected success from " + this);
        });
    }

    default F unsafeAssumeFailure() throws IllegalStateException {
        return match($ -> {
            throw new IllegalStateException("Expected failure from " + this);
        }, Function.identity());
    }

    // Functor
    default <U> Either<U, F> map(Function<T, U> mapper) {
        return match(t -> success(mapper.apply(t)), Either::failure);
    }

    default <G> Either<T, G> mapFailure(Function<F, G> mapper) {
        return match(Either::success, f -> failure(mapper.apply(f)));
    }

    // combinators

    // Monad
    default <U> Either<U, F> flatMap(Function<T, Either<U, F>> callback) {
        return match(callback, Either::failure);
    }

    // data

    record Success<T, F>(T t) implements Either<T, F> {

        @Override
        public <R> R match(Function<T, R> success, Function<F, R> failure) {
            return success.apply(t);
        }

        @Override
        public String toString() {
            return "Either.success(" + t + ")";
        }
    }

    record Failure<T, F>(F f) implements Either<T, F> {

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
