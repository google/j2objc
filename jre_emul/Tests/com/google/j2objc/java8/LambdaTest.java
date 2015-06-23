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

/**
 * Command-line tests for Lambda support.
 *
 * @author Seth Kirby
 */
public class LambdaTest extends TestCase {

  public LambdaTest() {}

  public void testBasicFunction() throws Exception {
    Function f = x -> x;
    assertEquals(42, f.apply(42));
    Function f2 = x -> {
      int y = 42;
      return y;
    };
    assertEquals(42, f2.apply(null));
    Function h = new Function() {
        @Override
      public Object apply(Object x) {
        return x;
      }
    };
    assertEquals(42, h.apply(42));
    // Function<Integer, Function<Integer, Integer>> f2 = (x) -> (y) -> x;
    // assertEquals(f2.apply(42).apply(43), Integer.valueOf(42));
    // Function<Integer, Function<Integer, Integer>> f3 = (y) -> (x) -> x;
    // assertEquals(f3.apply(43).apply(42), Integer.valueOf(42));
  }

  public void testBasicSupplier() throws Exception {
    Supplier s = () -> 42;
    assertEquals(42, s.get());
    // Supplier<Supplier> s2 = () -> () -> 42;
    // assertEquals(s2.get().get(), 42);
  }

  public void testBasicConsumer() throws Exception {
    Object[] ls = new Object[1];
    Consumer c = (x) -> ls[0] = x;
    c.accept(42);
    assertEquals(42, ls[0]);
  }

  public void testCallable() throws Exception {
    Callable c = () -> 42;
    assertEquals(42, c.call());
    // Callable<Callable> c2 = () -> () -> 42;
    // assertEquals(c2.call().call(), 42);
  }
}
