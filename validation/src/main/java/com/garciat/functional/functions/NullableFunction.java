package com.garciat.functional.functions;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface NullableFunction<T, R> {
  @Nullable
  R apply(T t);
}
