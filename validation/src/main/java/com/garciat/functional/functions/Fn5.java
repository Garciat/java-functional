package com.garciat.functional.functions;

import java.util.function.Function;

@FunctionalInterface
public interface Fn5<A, B, C, D, E, R> extends Fn4<A, B, C, D, Function<E, R>> {

  R apply(A a, B b, C c, D d, E e);

  @SuppressWarnings("FunctionalInterfaceMethodChanged")
  @Override
  default Function<E, R> apply(A a, B b, C c, D d) {
    return e -> apply(a, b, c, d, e);
  }

  default Fn5<E, D, C, B, A, R> rev5() {
    return (e, d, c, b, a) -> apply(a, b, c, d, e);
  }
}
