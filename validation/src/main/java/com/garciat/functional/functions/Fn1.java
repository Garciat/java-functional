package com.garciat.functional.functions;

import java.util.function.Function;

@FunctionalInterface
public interface Fn1<A, R> extends Function<A, R> {

  @Override
  R apply(A a);

  default Fn1<A, R> rev1() {
    return this;
  }
}
