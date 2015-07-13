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

import java.util.ArrayList;
import java.util.List;

interface Function<F, T> {
  T apply(F input);
}

interface Callable<T> {
  T call();
}

interface Supplier<T> {
  T get();
}

interface Consumer<R> {
  void accept(R input);
}

interface FourToOne<F, G, H, I, R> {
  R apply(F f, G g, H h, I i);
}

interface UnaryOperator<T> {
  public T apply(T t);
}

interface UOToUO<T> {
  UnaryOperator<T> apply(UOToUO<T> x);
}

/**
 * Command-line tests for Lambda support.
 *
 * @author Seth Kirby
 */
public class LambdaTest extends TestCase {

  public LambdaTest() {}

  Integer outerX = 0;
  int outerY = 0;

  public void testOuterVarCapture() {
    Supplier<Integer> s = () -> outerX;
    Supplier<Integer> s2 = () -> outerY;
    assertEquals((Integer) 0, s.get());
    assertEquals((Integer) 0, s2.get());
    outerX += 42;
    outerY += 42;
    assertEquals((Integer) 42, s.get());
    assertEquals((Integer) 42, s2.get());
    outerX++;
    outerY++;
    assertEquals((Integer) 43, s.get());
    assertEquals((Integer) 43, s2.get());
    outerX++;
    outerY++;
  }

  public void testFunctionArray() throws Exception {
    List<Function> fs = new ArrayList<Function>();
    int[] nums = { 3, 2, 1, 0 };
    for (int i : nums) {
      fs.add((x) -> i);
    }
    assertEquals(0, fs.get(3).apply("5"));
    assertEquals(1, fs.get(2).apply(new Object()));
    assertEquals(2, fs.get(1).apply(new Object()));
    assertEquals(3, fs.get(0).apply("4"));
  }

  public <T> UnaryOperator<T> yComb(UnaryOperator<UnaryOperator<T>> r) {
    return ((UOToUO<T>) f -> f.apply(f)).apply(f -> r.apply(x -> f.apply(f).apply(x)));
  }

  public void testYCombinator() throws Exception {
    UnaryOperator<UnaryOperator<String>> a = (UnaryOperator<String> f) -> {
      return (x) -> {
        if (x.length() == 5) {
          return x;
        } else {
          return f.apply((char) (x.charAt(0) + 1) + x);
        }
      };
    };
    assertEquals("edcba", yComb(a).apply("a"));
    UnaryOperator<UnaryOperator<Integer>> fibonacci = (UnaryOperator<Integer> f) -> {
      return (Integer x) -> {
        if (x < 1) {
          return 0;
        } else if (x == 1) {
          return x;
        } else {
          return f.apply(x - 1) + f.apply(x - 2);
        }
      };
    };
    assertEquals((Integer) 55, yComb(fibonacci).apply(10));
  }

  Function outerF = (x) -> x;

  public void testOuterFunctions() throws Exception {
    assertEquals(42, outerF.apply(42));
  }

  static Function staticF = (x) -> x;

  public void testStaticFunctions() throws Exception {
    assertEquals(42, staticF.apply(42));
  }

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
    FourToOne<String, Double, Integer, Boolean, String> appendFour = (a, b, c, d) -> a + b + c + d;
    assertEquals("Foo4.214true", appendFour.apply("Foo", 4.2, 14, true));
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
