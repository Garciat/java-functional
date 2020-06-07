package com.garciat.functional.functions;

import java.util.function.Function;

@FunctionalInterface
public interface Fn2<A, B, R> extends Fn1<A, Function<B, R>> {

  R apply(A a, B b);

  @SuppressWarnings("FunctionalInterfaceMethodChanged")
  @Override
  default Function<B, R> apply(A a) {
    return b -> apply(a, b);
  }

  default Fn2<B, A, R> rev2() {
    return (b, a) -> apply(a, b);
  }
}
