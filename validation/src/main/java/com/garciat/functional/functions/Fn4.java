package com.garciat.functional.functions;

import java.util.function.Function;

@FunctionalInterface
public interface Fn4<A, B, C, D, R> extends Fn3<A, B, C, Function<D, R>> {

  R apply(A a, B b, C c, D d);

  @SuppressWarnings("FunctionalInterfaceMethodChanged")
  @Override
  default Function<D, R> apply(A a, B b, C c) {
    return d -> apply(a, b, c, d);
  }

  default Fn4<D, C, B, A, R> rev4() {
    return (d, c, b, a) -> apply(a, b, c, d);
  }
}
