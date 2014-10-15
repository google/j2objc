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

package com.google.j2objc;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

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
    Class<?> thisClass = Class.forName("com.google.j2objc.ClassTest");
    assertNotNull(thisClass);
    assertEquals("com.google.j2objc.ClassTest", thisClass.getName());
    Method answerToLife = thisClass.getMethod("answerToLife");
    Integer answer = (Integer) answerToLife.invoke(this);
    assertEquals(42, answer.intValue());
  }

  public void testArrayForName() throws Exception {
    Class<?> arrayClass = Class.forName("[Ljava.lang.String;");
    assertNotNull(arrayClass);
    assertEquals("[Ljava.lang.String;", arrayClass.getName());
    String[] array = new String[0];
    assertEquals(array.getClass(), arrayClass);

    // Test that array types not referenced in source can be loaded.
    arrayClass = Class.forName("[[[Ljava.lang.Integer;");
    assertNotNull(arrayClass);
  }

  public void testGetDefaultConstructor() throws Exception {
    Class<?> foo = Class.forName("com.google.j2objc.ClassTest");
    Constructor<?> c = foo.getConstructor();
    Class<?>[] paramTypes = c.getParameterTypes();
    assertEquals(0, paramTypes.length);
  }

  public void testGetConstructor() throws Exception {
    Class<?> foo = Class.forName("com.google.j2objc.ClassTest");
    Constructor<?> c = foo.getConstructor(Double.class);
    Class<?>[] paramTypes = c.getParameterTypes();
    assertEquals(1, paramTypes.length);
  }

  public void testGetDeclaredConstructor() throws Exception {
    Class<?> foo = Class.forName("com.google.j2objc.ClassTest");
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

  public void testGetPackage() throws Exception {
    // Test package name for a class.
    Class<?> listClass = Class.forName("java.util.ArrayList");
    Package pkg = listClass.getPackage();
    assertNotNull(pkg);
    assertEquals("java.util", pkg.getName());

    // Test package name for an interface.
    Class<?> readerClass = Class.forName("java.io.Reader");
    pkg = readerClass.getPackage();
    assertNotNull(pkg);
    assertEquals("java.io", pkg.getName());

    // Test no package for arrays and primitive types.
    assertNull(boolean.class.getPackage());
    assertNull(int[].class.getPackage());
  }

  public void testInnerClass() throws Exception {
    Class<?> innerClass = Class.forName("com.google.j2objc.ClassTest$InnerClass");
    assertEquals(InnerClass.class, innerClass);
    assertEquals("com.google.j2objc.ClassTest$InnerClass", innerClass.getName());
    assertEquals("InnerClass", innerClass.getSimpleName());
    assertTrue(innerClass.isMemberClass());
    assertEquals(ClassTest.class, innerClass.getEnclosingClass());
  }

  public void testInnerInterface() throws Exception {
    Class<?> innerInterface = Class.forName("com.google.j2objc.ClassTest$InnerInterface");
    assertEquals(InnerInterface.class, innerInterface);
    assertEquals("com.google.j2objc.ClassTest$InnerInterface", innerInterface.getName());
    assertEquals("InnerInterface", innerInterface.getSimpleName());
    assertTrue(innerInterface.isMemberClass());
    assertEquals(ClassTest.class, innerInterface.getEnclosingClass());
  }

  public void testAnonymousClass() throws Exception {
    Object o = new Object() {};
    Class<?> cls = o.getClass();
    assertTrue(cls.isAnonymousClass());
    assertFalse(cls.isMemberClass());
    assertEquals(ClassTest.class, cls.getEnclosingClass());
  }

  public void testGetGenericSuperclass() throws Exception {
    Class<?> cls = SubParameterizedClass.class;
    Type genericSuperclass = cls.getGenericSuperclass();
    assertTrue(genericSuperclass instanceof ParameterizedType);
    ParameterizedType pType = (ParameterizedType) genericSuperclass;
    assertEquals(ParameterizedClass.class, pType.getRawType());
    Type[] typeArgs = pType.getActualTypeArguments();
    assertEquals(2, typeArgs.length);
    assertEquals(String.class, typeArgs[0]);
    assertTrue(typeArgs[1] instanceof TypeVariable);
    assertEquals("C", ((TypeVariable) typeArgs[1]).getName());
  }

  public void testArrayIsAssignableToObject() throws Exception {
    assertTrue(Object.class.isAssignableFrom(Object[].class));
    assertTrue(Object.class.isAssignableFrom(Object[][].class));
    assertTrue(Object.class.isAssignableFrom(String[].class));
    assertTrue(Object.class.isAssignableFrom(int[].class));
    assertFalse(String.class.isAssignableFrom(String[].class));
  }

  public void testGetMappedMethods() throws Exception {
    Class<?> objectClass = Class.forName("java.lang.Object");
    Method[] methods = objectClass.getDeclaredMethods();
    assertTrue("not all Object methods returned, only: " +
        methods.length + " " + Arrays.toString(methods), methods.length >= 7);
    Class<?> classClass = Class.forName("java.lang.Class");
    methods = classClass.getDeclaredMethods();
    assertTrue("not all Class methods returned, only: " +
        methods.length + " " + Arrays.toString(methods), methods.length >= 5);
    Class<?> stringClass = Class.forName("java.lang.String");
    methods = stringClass.getDeclaredMethods();
    assertTrue("not all String methods returned, only: " +
        methods.length + " " + Arrays.toString(methods), methods.length >= 35);
    Class<?> numberClass = Class.forName("java.lang.Number");
    methods = numberClass.getDeclaredMethods();
    assertTrue("not all Number methods returned, only: " +
        methods.length + " " + Arrays.toString(methods), methods.length >= 6);
  }

  public void testGetEnum() throws Exception {
    Class<?> innerEnum = Class.forName("com.google.j2objc.ClassTest$InnerEnum");
    assertNotNull(innerEnum);
  }

  public void testLookupClassWith$() throws Exception {
    Class<?> dollarClass = Class.forName("com.google.j2objc.Test$$With$$Dollar$$Signs");
    assertNotNull(dollarClass);
  }

  // Verify that lookup of a method in an interface throws NoSuchMethod.
  public void testNoSuchInterfaceMethod() {
    try {
      Method m = java.io.Serializable.class.getMethod("foo");
      fail("method shouldn't have been returned");
    } catch (NoSuchMethodException e) {
      // Successful.
    } catch (Throwable t) {
      fail("wrong exception thrown");
    }
  }

  static class InnerClass {
  }

  interface InnerInterface {
  }

  static class ParameterizedClass<A, B> {
  }

  static class SubParameterizedClass<C> extends ParameterizedClass<String, C> {
  }

  static enum InnerEnum {
    A, B, C;
  }
}

class Test$$With$$Dollar$$Signs {
}
