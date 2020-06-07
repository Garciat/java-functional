package com.garciat.functional.functions;

import java.util.function.Function;

@FunctionalInterface
public interface Fn3<A, B, C, R> extends Fn2<A, B, Function<C, R>> {

  R apply(A a, B b, C c);

  @SuppressWarnings("FunctionalInterfaceMethodChanged")
  @Override
  default Function<C, R> apply(A a, B b) {
    return c -> apply(a, b, c);
  }

  default Fn3<C, B, A, R> rev3() {
    return (c, b, a) -> apply(a, b, c);
  }
}
