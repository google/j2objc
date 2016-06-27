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

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for default method support.
 *
 * @author Lukhnos Liu
 */
public class DefaultMethodsTest extends TestCase {

  interface A {
    default boolean f() {
      return true;
    }

    default boolean notF() {
      return !f();
    }

    default String getPrefix() {
      return "";
    }

    default String getTag(String name) {
      return getPrefix() + name;
    }

    default String getDefaultTag() {
      return getTag("default");
    }
  }

  interface B extends A {
    boolean f();

    default B not() {
      return () -> !f();
    }
  }

  interface C extends A {
    default boolean f() {
      return !A.super.f();
    }
  }

  interface D extends A {
    static String getDPrefix() {
      return "static-";
    }

    String getTag(String name);
  }

  static class AP implements A {
  }

  static class AQ implements A {
    @Override
    public boolean f() {
      return false;
    }

    @Override
    public String getPrefix() {
      return "prefix-";
    }

    @Override
    public String getDefaultTag() {
      return getTag("DEFAULT");
    }
  }

  interface WithParam {
    int something(int in);
    default int get8() {
      return 8;
    }
  }
  interface Unrelated {
    default boolean somethingElse() {
      return false;
    }
  }

  public void testBasicInstantiation() {
    AP a = new AP();
    assertTrue(a.f());
    assertFalse(a.notF());
    assertEquals("default", a.getDefaultTag());
  }

  public void testSuperInvocation() {
    A c = new C() {};
    assertFalse(c.f());
    assertTrue(c.notF());
  }

  public void testOverriding() {
    AQ a = new AQ();
    assertFalse(a.f());
    assertTrue(a.notF());
    assertEquals("prefix-DEFAULT", a.getDefaultTag());
  }

  public void testLambdaWithDefaultMethods() {
    B b1 = () -> true;
    assertTrue(b1.f());
    assertFalse(b1.notF());
    assertFalse(b1.not().f());

    B b2 = () -> false;
    assertFalse(b2.f());
    assertTrue(b2.notF());
    assertTrue(b2.not().f());

    assertFalse(((B & Unrelated) () -> true).somethingElse());
    assertFalse(((WithParam & Unrelated) (a) -> a).somethingElse());

    D d = (name) -> D.getDPrefix() + name;
    assertEquals("static-default", d.getDefaultTag());
  }

  public void testGetMethodsReturnsDefaultMethod() {
    Set<String> methodNames = new HashSet<>();
    for (Method m : AP.class.getMethods()) {
      methodNames.add(m.getName());
    }
    assertTrue(methodNames.contains("f"));
    assertTrue(methodNames.contains("notF"));
  }
}
