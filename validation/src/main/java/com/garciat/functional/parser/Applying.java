package com.garciat.functional.parser;

import com.garciat.functional.functions.Fn1;
import com.garciat.functional.functions.Fn2;
import com.garciat.functional.functions.Fn3;

public interface Applying<T, N, R> {

  R apply(Parser<T, N> next);

  static <T, A, R> Applying<T, A, Parser<T, R>> to(Class<T> t, Fn1<A, R> f) {
    return pa -> pa.applyTo(Parser.returning(f));
  }

  static <T, A, B, R> Applying<T, A, Applying<T, B, Parser<T, R>>> to(Class<T> t, Fn2<A, B, R> f) {
    return pa -> pb -> pb.applyTo(pa.applyTo(Parser.returning(f)));
  }

  static <T, A, B, C, R> Applying<T, A, Applying<T, B, Applying<T, C, Parser<T, R>>>> to(Class<T> t, Fn3<A, B, C, R> f) {
    return pa -> pb -> pc -> pc.applyTo(pb.applyTo(pa.applyTo(Parser.returning(f))));
  }
}
