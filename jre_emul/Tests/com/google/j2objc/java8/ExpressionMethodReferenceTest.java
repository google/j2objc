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

interface Funct<T, R> {
  R f(T t);
}

class Q {
  static Object o(Object x) {
    return x;
  }

  Object o2(Object x) {
    return x;
  }
}

/**
 * Command-line tests for expression method references.
 *
 * @author Seth Kirby
 */
public class ExpressionMethodReferenceTest extends TestCase {
  public ExpressionMethodReferenceTest() { }

  public void testBasicReferences() throws Exception {
    Funct f = Z.ZZ::o;
    Funct<String, Object> f2 = Z.ZZ::o;
    Funct<String, String> f3 = new Z()::o;
    assertEquals("Bar", f.f(""));
    assertEquals("Foo", f2.f(""));
    assertEquals("Baz", f3.f(""));
    Funct ff = Q::o;
    Funct ff1 = new Q()::o2;
  }
}
