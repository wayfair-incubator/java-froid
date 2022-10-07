package com.wayfair.javafroid;

import java.util.function.Function;

/**
 * Why... In most cases checked exceptions can't be thrown out of Lamba expressions.
 * This Interface can be used to catch Checked, throw Runtimes out of Lamba expression.
 * It keeps the Lamba expressions and stream pipelines cleaner and more concise (IMO)...
 *
 * @param <T> T
 * @param <R> R
 * @param <E> E
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {

  R apply(T t) throws E;

  static <T, R, E extends Exception> Function<T, R> unchecked(ThrowingFunction<T, R, E> f) {
    return t -> {
      try {
        return f.apply(t);
      } catch (Exception e) {
        throw new RuntimeException(e); //NOSONAR
      }
    };
  }
}
