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
import java.util.concurrent.Callable;

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

  public void testBasicReflection() {
    assertEquals(false, ((Lambdas.One) (x) -> 1).equals((Lambdas.One) (x) -> 1));
    Lambdas.One f = x -> 1;
    Lambdas.One g = f;
    assertEquals(f, g);
    assertEquals(f.toString(), "" + g);
    String lambdaClassName = f.getClass().getName();
    assertEquals(true, lambdaClassName.contains("LambdaTest"));
    assertEquals(true, lambdaClassName.contains("ambda$"));
  }

  String outerCall() {
    return "Foo";
  }

  private String privateOuterCall() {
    return "Bar";
  }

  static class Foo {
    String outer() {
      return "Foo";
    }

    class Bar {
      String findMe() {
        return "Bar";
      }

      Lambdas.Zero fooS = () -> outer();
      Lambdas.Zero barS = () -> findMe();
    }

    Bar getBar() {
      return new Bar();
    }
  }

  public void testOuterMethodCalls() {
    assertEquals("Foo", Lambdas.get(() -> outerCall()).apply());
    assertEquals("Bar", Lambdas.get(() -> privateOuterCall()).apply());
    Foo foo = new Foo();
    assertEquals("Foo", Lambdas.get(() -> foo.outer()).apply());
    assertEquals("Bar", Lambdas.get(() -> foo.getBar().barS.apply()).apply());
  }

  Integer outerX = 0;
  int outerY = 0;

  public void testOuterVarCapture() {
    Lambdas.Zero s = () -> outerX;
    Lambdas.Zero s2 = () -> outerY;
    assertEquals((Integer) 0, s.apply());
    assertEquals((Integer) 0, s2.apply());
    outerX += 42;
    outerY += 42;
    assertEquals((Integer) 42, s.apply());
    assertEquals((Integer) 42, s2.apply());
    outerX++;
    outerY++;
    assertEquals((Integer) 43, s.apply());
    assertEquals((Integer) 43, s2.apply());
    outerX++;
    outerY++;
  }

  public void testFunctionArray() throws Exception {
    List<Lambdas.One> fs = new ArrayList<>();
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

  Lambdas.One outerF = (x) -> x;

  public void testOuterFunctions() throws Exception {
    assertEquals(42, outerF.apply(42));
  }

  static Lambdas.One staticF = (x) -> x;

  public void testStaticFunctions() throws Exception {
    assertEquals(42, staticF.apply(42));
  }

  public void testNestedLambdas() throws Exception {
    Lambdas.One<String, Lambdas.One<String, String>> f = (x) -> (y) -> x;
    assertEquals(f.apply("Foo").apply("Bar"), "Foo");
    Lambdas.One<Integer, Lambdas.One<Integer, Integer>> f2 = (x) -> (y) -> x;
    assertEquals(f2.apply(42).apply(43), Integer.valueOf(42));
    Lambdas.One<Integer, Lambdas.One<Integer, Integer>> f3 = (y) -> (x) -> x;
    assertEquals(f3.apply(43).apply(42), Integer.valueOf(42));
    Lambdas.One<String, Lambdas.One<String, Integer>> f4 = (x) -> (y) -> Integer.parseInt(
        x);
    assertEquals(f4.apply("42").apply("43"), Integer.valueOf(42));
    Lambdas.One<String, Lambdas.One<Integer, Integer>> f5 = (x) -> (y) -> Integer.parseInt(
        x);
    assertEquals(f5.apply("42").apply(43), Integer.valueOf(42));
    Callable<Callable> c2 = () -> () -> 42;
    assertEquals(c2.call().call(), 42);
    Lambdas.Zero<Lambdas.Zero> s2 = () -> () -> 42;
    assertEquals(s2.apply().apply(), 42);
  }

  // Tests outer reference resolution, and that inner fields are being correctly resolved for
  // lambdas with implicit blocks.
  public void testAdditionInLambda() throws Exception {
    Lambdas.One f = new Lambdas.One<Integer, Integer>() {
        @Override
      public Integer apply(Integer x) {
        return x + 20;
      }
    };
    assertEquals(42, f.apply(22));
    Lambdas.One<Integer, Integer> g = x -> {
      return x + 20;
    };
    assertEquals((Integer) 42, g.apply(22));
    Lambdas.One<Integer, Integer> h = x -> x + 20;
    assertEquals((Integer) 42, h.apply(22));
  }

  public void testBasicAnonymousClass() throws Exception {
    Lambdas.One h = new Lambdas.One() {
        @Override
      public Object apply(Object x) {
        return x;
      }
    };
    assertEquals(42, h.apply(42));
  }

  public void testBasicFunction() throws Exception {
    Lambdas.One f = x -> x;
    assertEquals(42, f.apply(42));
    Lambdas.One<Integer, Integer> f2 = x -> x;
    assertEquals((Integer) 42, f2.apply(42));
    Lambdas.One f3 = x -> {
      int y = 42;
      return y;
    };
    assertEquals(42, f3.apply(null));
    Lambdas.Four<String, Double, Integer, Boolean, String> appendFour = (a, b, c, d) -> a + b + c
        + d;
    assertEquals("Foo4.214true", appendFour.apply("Foo", 4.2, 14, true));
  }

  public void testBasicSupplier() throws Exception {
    Lambdas.Zero s = () -> 42;
    assertEquals(42, s.apply());
  }

  public void testBasicConsumer() throws Exception {
    Object[] ls = new Object[1];
    Lambdas.VoidOne c = (x) -> ls[0] = x;
    c.apply(42);
    assertEquals(42, ls[0]);
  }

  public void testBasicCallable() throws Exception {
    Callable c = () -> 42;
    assertEquals(42, c.call());
  }
}
