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

class Z {
  static class ZZ {
    static Object o(Object x) {
      return "Bar";
    }

    static Object o(String x) {
      return "Foo";
    }
  }

  String o(String x) {
    return "Baz";
  }
}

class Q {
  static Object o(Object x) {
    return x;
  }

  Object o2(Object x) {
    return x;
  }
}

class Y {
  static String m(Number a1, Object... rest) {
    return a1 + p(rest);
  }

  static String p(Object... ls) {
    String out = " [ ";
    for (Object x : ls) {
      out += x;
      out += ' ';
    }
    return out + ']';
  }
}

/**
 * Command-line tests for expression method references.
 *
 * @author Seth Kirby
 */
public class ExpressionMethodReferenceTest extends TestCase {
  public ExpressionMethodReferenceTest() {
  }

  interface I {
    String foo(Integer a1, Integer a2, String a3);
  }

  interface J {
    public String m(Integer a1, Integer a2, String a3, String a4);
  }

  public void testBasicReferences() throws Exception {
    Lambdas.One f = Z.ZZ::o;
    Lambdas.One<String, Object> f2 = Z.ZZ::o;
    Lambdas.One<String, String> f3 = new Z()::o;
    assertEquals("Bar", f.apply(""));
    assertEquals("Foo", f2.apply(""));
    assertEquals("Baz", f3.apply(""));
    Lambdas.One f4 = Q::o;
    Lambdas.One f5 = new Q()::o2;
    assertEquals("Foo", f4.apply("Foo"));
    assertEquals("Bar", f5.apply("Bar"));
  }

  public void testVarArgs() throws Exception {
    I i = Y::m;
    J j = Y::m;
    assertEquals("12 [ 22 42 ]", i.foo(12, 22, "42"));
    assertEquals("10 [ 20 20 10 ]", j.m(10, 20, "20", "10"));
  }

  interface IntFun {
    String apply(int a);
  }

  interface IntegerFun {
    String apply(Integer a);
  }

  String foo(Integer a) {
    return "Foo";
  }

  String bar(int x) {
    return "Bar";
  }

  public void testBoxingAndUnboxing() throws Exception {
    IntFun i = this::foo;
    assertEquals("Foo", i.apply(new Integer(42)));
    i = (int x) -> "Lambda";
    assertEquals("Lambda", i.apply(42));
    IntegerFun i2 = this::bar;
    assertEquals("Bar", i2.apply(42));
    i2 = (Integer x) -> "Lambda";
    assertEquals("Lambda", i2.apply(42));
  }

  interface IntegerVarargsFun {
    Integer apply(int a, int b, Integer c, Integer d, int e);
  }

  interface IntVarargsFun {
    int apply(int a, int b, Integer c, Integer d, int e);
  }

  Integer foos(int x, Integer... xs) {
    int out = x;
    for (int a : xs) {
      out += a;
    }
    return out;
  }

  int bars(Integer x, int... xs) {
    Integer out = x;
    for (int a : xs) {
      out -= a;
    }
    return out;
  }

  public void testVarargBoxingAndUnboxing() throws Exception {
    IntegerVarargsFun i = this::foos;
    int b = 42, d = 24;
    Integer a = 5, c = 8, e = 13;
    IntVarargsFun i2 = this::bars;
    assertEquals((Integer) 92, i.apply(a, b, c, d, e));
    assertEquals(-82, i2.apply(a, b, c, d, e));
  }

  interface Fun {
    Integer apply();
  }

  interface Fun2 {
    int apply();
  }

  int size() {
    return 42;
  }

  Integer size2() {
    return 43;
  }

  public void testReturnBoxingAndUnboxing() throws Exception {
    Fun f = this::size;
    assertEquals((Integer) 42, f.apply());
    Fun2 f2 = this::size2;
    assertEquals(43, f2.apply());
  }

  static class DataHolder {
    private int innerNum;
    public DataHolder(int i) {
      innerNum = i;
    }
    int getData() {
      return innerNum;
    }
  }

  public void testCapturingExpressionMethodReferences() {
    DataHolder data = new DataHolder(3);
    Lambdas.Zero fun = data::getData;
    assertEquals(3, fun.apply());
    data = new DataHolder(6);
    assertEquals(3, fun.apply());
    fun = data::getData;
    assertEquals(6, fun.apply());
  }
}
