/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc.java8;

/**
 * Helper class for lambda types and performing lambda operations.
 *
 * @author Seth Kirby
 */
public class Lambdas {
  interface Zero<R> {
    R apply();
  }

  interface One<T, R> {
    R apply(T t);
  }

  interface Two<T, U, R> {
    R apply(T t, U u);
  }

  interface Three<T, U, V, R> {
    R apply(T t, U u, V v);
  }

  interface Four<T, U, V, W, R> {
    R apply(T t, U u, V v, W w);
  }

  public static <R> Zero<R> get(Zero<R> lambda) {
    return lambda;
  }

  public static <T, R> One<T, R> get(One<T, R> lambda) {
    return lambda;
  }

  public static <T, U, R> Two<T, U, R> get(Two<T, U, R> lambda) {
    return lambda;
  }

  public static <T, U, V, R> Three<T, U, V, R> get(Three<T, U, V, R> lambda) {
    return lambda;
  }

  public static <T, U, V, W, R> Four<T, U, V, W, R> get(Four<T, U, V, W, R> lambda) {
    return lambda;
  }

  interface VoidZero {
    void apply();
  }

  interface VoidOne<T> {
    void apply(T t);
  }

  interface VoidTwo<T, U> {
    void apply(T t, U u);
  }

  interface VoidThree<T, U, V> {
    void apply(T t, U u, V v);
  }

  interface VoidFour<T, U, V, W> {
    void apply(T t, U u, V v, W w);
  }

  public static VoidZero get(VoidZero lambda) {
    return lambda;
  }

  public static <T> VoidOne<T> get(VoidOne<T> lambda) {
    return lambda;
  }

  public static <T, U> VoidTwo<T, U> get(VoidTwo<T, U> lambda) {
    return lambda;
  }

  public static <T, U, V> VoidThree<T, U, V> get(VoidThree<T, U, V> lambda) {
    return lambda;
  }

  public static <T, U, V, W> VoidFour<T, U, V, W> get(VoidFour<T, U, V, W> lambda) {
    return lambda;
  }
}
