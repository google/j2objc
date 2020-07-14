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

class I {
  String s = "...";
  I() { }
  I(int x) {
    s = "" + x;
  }
  I(int x, I j, String s, Object o) {
    this.s = s;
  }
  static String hello() {
    return "Hello";
  }
  String world() {
    return "World";
  }
  String getS() {
    return s;
  }

  I(Object a1, Object... rest) {
    s = a1 + p(rest);
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

final class J {
  int x = 41;
  J() { }
  J(int x) {
    this.x = x;
  }
  J(int x, I j, String s, Object o) {
    this.x = x;
  }
  static String hello() {
    return "Hello";
  }
  String world() {
    return "World";
  }
  int getX() {
    return x;
  }
}

/**
 * Command-line tests for creation references.
 *
 * @author Seth Kirby
 */
public class CreationReferenceTest extends TestCase {
  public CreationReferenceTest() {}

  interface FunInt<T> {
    T apply(int x);
  }

  interface FunInt4<T> {
    T apply(int x, I j, String s, Object o);
  }

  public void testBasicReferences() {
    Lambdas.Zero<I> iInit = I::new;
    FunInt<I> iInit2 = I::new;
    FunInt4<I> iInit3 = I::new;
    I myI = iInit.apply();
    I myI2 = iInit2.apply(42);
    I myI3 = iInit3.apply(0, myI, "43", "");
    assertEquals("World", myI.world());
    assertEquals("...", myI.getS());
    assertEquals("42", myI2.getS());
    assertEquals("43", myI3.getS());
    Lambdas.Zero<J> jInit = J::new;
    FunInt<J> jInit2 = J::new;
    FunInt4<J> jInit3 = J::new;
    J myJ = jInit.apply();
    J myJ2 = jInit2.apply(42);
    J myJ3 = jInit3.apply(43, myI, "", "");
    assertEquals("World", myJ.world());
    assertEquals(41, myJ.getX());
    assertEquals(42, myJ2.getX());
    assertEquals(43, myJ3.getX());
  }

  @SuppressWarnings("unchecked")
  public void testVarargs() {
    Lambdas.Three f = I::new;
    Lambdas.Four<Object, Object, Object, Object, I> f2 = I::new;
    assertEquals("12 [ 22 42 ]", ((I) f.apply(12, 22, "42")).getS());
    assertEquals("10 [ 20 20 10 ]", f2.apply(10, 20, "20", "10").s);
  }

  // Creation references can be initialized only for side effects, and have a void return.
  @SuppressWarnings("unchecked")
  public void testVoidFunctionalInterface() {
    Lambdas.VoidThree f = I::new;
    Lambdas.VoidFour<Object, Object, Object, Object> f2 = I::new;
    f.apply(12, 22, "42");
    f2.apply(10, 20, "20", "10");
  }

  // Test and helper function to make sure that constructor method references
  // use capturing lambdas when necessary.
  static Lambdas.Zero<Lambdas.Zero<String>> closes(String toClose) {
    class Closer implements Lambdas.Zero<String> {
      public String apply() {
        return toClose;
      };
    };
    return Closer::new;
  }

  public void testCapturingConstructorMethodReferences() throws Exception {
    Lambdas.Zero<Lambdas.Zero<String>> gen1 = closes("Works!");
    Lambdas.Zero<String> fun1 = gen1.apply();
    assertEquals(fun1.apply(), "Works!");
  }

  static class StatefulOuter {
    int state;

    class Inner {
      int getOuterState() {
        return state;
      }
    }

    interface Provider {
      Inner get();
    }

    Provider innerProvider() {
      return Inner::new;
    }
  }

  public void testCreationReferenceOfClassWithOuterScope() throws Exception {
    StatefulOuter o = new StatefulOuter();
    o.state = 9876;
    StatefulOuter.Provider p = o.innerProvider();
    assertEquals(9876, p.get().getOuterState());
  }
}
