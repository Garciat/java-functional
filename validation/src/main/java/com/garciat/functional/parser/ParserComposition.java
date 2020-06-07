package com.garciat.functional.parser;

import java.util.function.Function;

public abstract class ParserComposition<T, R, F> {

  private ParserComposition() {}

  public final Parser<T, R> build(F f) {
    return Parser.<T>id()
        .flatMap(t -> build().map(cont -> cont.apply(f)).tagged(t.getClass().getSimpleName()));
  }

  protected abstract Parser<T, Function<F, R>> build();

  public <A> ParserComposition<T, R, Function<A, F>> with(Parser<T, A> field) {
    return new ParserComposition<T, R, Function<A, F>>() {
      @Override
      protected Parser<T, Function<Function<A, F>, R>> build() {
        Parser<T, Function<F, R>> parent = ParserComposition.this.build();
        return Parser.merge(field, parent, (a, fr) -> af -> fr.apply(af.apply(a)));
      }
    };
  }

  public static <T, R> ParserComposition<T, R, R> of(Class<T> input, Class<R> result) {
    return new ParserComposition<T, R, R>() {
      @Override
      protected Parser<T, Function<R, R>> build() {
        return Parser.returning(r -> r);
      }
    };
  }
}
