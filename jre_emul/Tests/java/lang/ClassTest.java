/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

package java.lang;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Command-line tests for java.lang.Class support (IOSClass)
 *
 * @author Tom Ball
 */
public class ClassTest extends TestCase {

  public ClassTest() {}

  public ClassTest(Double d) {
    super();
  }

  public int answerToLife() {
    return 42;
  }

  public void testForName() throws Exception {
    Class<?> thisClass = Class.forName("java.lang.ClassTest");
    assertNotNull(thisClass);
    assertEquals("JavaLangClassTest", thisClass.getName());
    Method answerToLife = thisClass.getMethod("answerToLife");
    Integer answer = (Integer) answerToLife.invoke(this);
    assertEquals(42, answer.intValue());
  }

  public void testGetDefaultConstructor() throws Exception {
    Class<?> foo = Class.forName("java.lang.ClassTest");
    Constructor<?> c = foo.getConstructor();
    Class<?>[] paramTypes = c.getParameterTypes();
    assertEquals(0, paramTypes.length);
  }

  public void testGetConstructor() throws Exception {
    Class<?> foo = Class.forName("java.lang.ClassTest");
    Constructor<?> c = foo.getConstructor(Double.class);
    Class<?>[] paramTypes = c.getParameterTypes();
    assertEquals(1, paramTypes.length);
  }

  public void testGetDeclaredConstructor() throws Exception {
    Class<?> foo = Class.forName("java.lang.ClassTest");
    Constructor<?> c = foo.getConstructor();
    Class<?>[] paramTypes = c.getParameterTypes();
    assertEquals(0, paramTypes.length);
  }

  public void testGetInterfaceMethods() throws Exception {
    Class<?> runnableClass = Class.forName("java.lang.Runnable");
    Method[] methods = runnableClass.getMethods();
    assertEquals(1, methods.length);
    Method runMethod = methods[0];
    assertEquals("run", runMethod.getName());
    assertEquals(0, runMethod.getParameterTypes().length);
  }

  public void testGetDeclaredInterfaceMethods() throws Exception {
    Class<?> runnableClass = Class.forName("java.lang.Runnable");
    Method[] methods = runnableClass.getDeclaredMethods();
    assertEquals(1, methods.length);
    Method runMethod = methods[0];
    assertEquals("run", runMethod.getName());
    assertEquals(0, runMethod.getParameterTypes().length);
  }

  public void testGetInterfaceMethod() throws Exception {
    Class<?> runnableClass = Class.forName("java.lang.Runnable");
    Method runMethod = runnableClass.getMethod("run", new Class<?>[0]);
    assertEquals("run", runMethod.getName());
    assertEquals(0, runMethod.getParameterTypes().length);
  }

  public void testInterfaceMethodInvocation() throws Exception {
    Class<?> runnableClass = Class.forName("java.lang.Runnable");
    Method runMethod = runnableClass.getMethod("run", new Class<?>[0]);
    Runnable r = new Runnable() {
      public void run() { System.out.println("run, run"); }
    };
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    PrintStream oldOut = System.out;
    System.setOut(ps);
    try {
      runMethod.invoke(r, new Object[0]);
      System.out.flush();
      assertEquals("run, run\n", baos.toString());
    } finally {
      System.setOut(oldOut);
    }
  }

  public void testArrayClassObjects() throws Exception {
    Class<?> c1 = int[].class;
    Class<?> c2 = int[][].class;
    assertFalse(c1 == c2);
    assertTrue(c1 == c2.getComponentType());
    c1 = String[].class;
    c2 = String[][].class;
    assertFalse(c1 == c2);
    assertTrue(c1 == c2.getComponentType());
  }
}
