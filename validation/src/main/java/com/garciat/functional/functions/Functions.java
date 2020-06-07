package com.garciat.functional.functions;

public final class Functions {

  private Functions() {}

  public static final class Curry {

    private Curry() {}

    public static <A, R> Fn1<A, R> curry(Fn1<A, R> f) {
      return f;
    }

    public static <A, B, R> Fn2<A, B, R> curry(Fn2<A, B, R> f) {
      return f;
    }

    public static <A, B, C, R> Fn3<A, B, C, R> curry(Fn3<A, B, C, R> f) {
      return f;
    }

    public static <A, B, C, D, R> Fn4<A, B, C, D, R> curry(Fn4<A, B, C, D, R> f) {
      return f;
    }
  }

  public static final class Reversed {

    private Reversed() {}

    public static <A, R> Fn1<A, R> reversed(Fn1<A, R> f) {
      return f.rev1();
    }

    public static <A, B, R> Fn2<B, A, R> reversed(Fn2<A, B, R> f) {
      return f.rev2();
    }

    public static <A, B, C, R> Fn3<C, B, A, R> reversed(Fn3<A, B, C, R> f) {
      return f.rev3();
    }

    public static <A, B, C, D, R> Fn4<D, C, B, A, R> reversed(Fn4<A, B, C, D, R> f) {
      return f.rev4();
    }
  }
}
