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

package com.google.j2objc;

import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * Command-line tests for java.lang.reflect.Method support.
 *
 * @author Keith Stanger
 */
public class MethodTest extends TestCase {

  static class A {

    private String foo() {
      return "A.foo " + bar();
    }

    public String bar() {
      return "A.bar";
    }
  }

  static class B extends A {

    public String foo() {
      return "B.foo " + bar();
    }

    public String bar() {
      return "B.bar";
    }
  }

  public void testInvokeOverriddenPrivateMethod() throws Exception {
    Method m = A.class.getDeclaredMethod("foo");
    m.setAccessible(true);
    assertEquals("A.foo B.bar", m.invoke(new B()));
  }
}
