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

interface FunInt<T> {
  T apply(int x);
}

interface FunInt4<T> {
  T apply(int x, I j, String s, Object o);
}

interface Call<T> {
  T call();
}

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

  public void testBasicReferences() throws Exception {
    Call<I> iInit = I::new;
    FunInt<I> iInit2 = I::new;
    FunInt4<I> iInit3 = I::new;
    I myI = iInit.call();
    I myI2 = iInit2.apply(42);
    I myI3 = iInit3.apply(0, myI, "43", "");
    assertEquals("World", myI.world());
    assertEquals("...", myI.getS());
    assertEquals("42", myI2.getS());
    assertEquals("43", myI3.getS());
    Call<J> jInit = J::new;
    FunInt<J> jInit2 = J::new;
    FunInt4<J> jInit3 = J::new;
    J myJ = jInit.call();
    J myJ2 = jInit2.apply(42);
    J myJ3 = jInit3.apply(43, myI, "", "");
    assertEquals("World", myJ.world());
    assertEquals(41, myJ.getX());
    assertEquals(42, myJ2.getX());
    assertEquals(43, myJ3.getX());
  }
}
