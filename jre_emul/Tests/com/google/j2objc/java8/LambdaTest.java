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

import junit.framework.TestCase;

import java.util.concurrent.Callable;

interface Function<F, T> {
  T apply(F input);
}

interface Supplier<T> {
  T get();
}

interface Consumer<R> {
  void accept(R input);
}

// interface UnaryOperator<T> {
// public T apply(T t);
// }
//
// interface UOToUO<T> {
// UnaryOperator<T> apply(UOToUO<T> x);
// }

/**
 * Command-line tests for Lambda support.
 *
 * @author Seth Kirby
 */
public class LambdaTest extends TestCase {

  public LambdaTest() {}

  // public static <T> UnaryOperator<T> yComb(UnaryOperator<UnaryOperator<T>> r) {
  // return ((UOToUO<T>) f -> f.apply(f)).apply(
  // f -> r.apply(x -> f.apply(f).apply(x)));
  // }

  // public void testYCombinator() throws Exception {
  //
  // UnaryOperator<UnaryOperator<String>> a = (UnaryOperator<String> f) -> {
  // return (String x) -> {
  // if (x.equals("1111")) {
  // return x;
  // } else {
  // return f.apply(x + "1");
  // }
  // };
  // };
  //
  // assertEquals("1111", yComb(a).apply(""));
  // }

  public void testNestedLambdas() throws Exception {
    Function<String, Function<String, String>> f = (x) -> (y) -> x;
    assertEquals(f.apply("Foo").apply("Bar"), "Foo");
    Function<Integer, Function<Integer, Integer>> f2 = (x) -> (y) -> x;
    assertEquals(f2.apply(42).apply(43), Integer.valueOf(42));
    Function<Integer, Function<Integer, Integer>> f3 = (y) -> (x) -> x;
    assertEquals(f3.apply(43).apply(42), Integer.valueOf(42));
    Function<String, Function<String, Integer>> f4 = (x) -> (y) -> Integer.parseInt(x);
    assertEquals(f4.apply("42").apply("43"), Integer.valueOf(42));
    Function<String, Function<Integer, Integer>> f5 = (x) -> (y) -> Integer.parseInt(x);
    assertEquals(f5.apply("42").apply(43), Integer.valueOf(42));
    Callable<Callable> c2 = () -> () -> 42;
    assertEquals(c2.call().call(), 42);
    Supplier<Supplier> s2 = () -> () -> 42;
    assertEquals(s2.get().get(), 42);
  }

  // Tests outer reference resolution, and that inner fields are being correctly resolved for
  // lambdas with implicit blocks.
  public void testAdditionInLambda() throws Exception {
    Function f = new Function<Integer, Integer>() {
        @Override
      public Integer apply(Integer x) {
        return x + 20;
      }
    };
    assertEquals(42, f.apply(22));
    Function<Integer, Integer> g = x -> {
      return x + 20;
    };
    assertEquals((Integer) 42, g.apply(22));
    Function<Integer, Integer> h = x -> x + 20;
    assertEquals((Integer) 42, h.apply(22));
  }

  public void testBasicAnonymousClass() throws Exception {
    Function h = new Function() {
        @Override
      public Object apply(Object x) {
        return x;
      }
    };
    assertEquals(42, h.apply(42));
  }

  public void testBasicFunction() throws Exception {
    Function f = x -> x;
    assertEquals(42, f.apply(42));
    Function<Integer, Integer> f2 = x -> x;
    assertEquals((Integer) 42, f2.apply(42));
    Function f3 = x -> {
      int y = 42;
      return y;
    };
    assertEquals(42, f3.apply(null));
  }

  public void testBasicSupplier() throws Exception {
    Supplier s = () -> 42;
    assertEquals(42, s.get());
  }

  public void testBasicConsumer() throws Exception {
    Object[] ls = new Object[1];
    Consumer c = (x) -> ls[0] = x;
    c.accept(42);
    assertEquals(42, ls[0]);
  }

  public void testBasicCallable() throws Exception {
    Callable c = () -> 42;
    assertEquals(42, c.call());
  }
}
