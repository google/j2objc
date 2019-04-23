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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

  public void testGetArrayMethods() throws Exception {
    Method[] methods = int[].class.getDeclaredMethods();
    assertEquals(0, methods.length);
    methods = int[].class.getMethods();
    Set<String> methodNames = new HashSet<>();
    for (Method m : methods) {
      methodNames.add(m.getName());
    }
    assertTrue(methodNames.contains("equals"));
    assertTrue(methodNames.contains("toString"));
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

  // Verify that we can access the class literals for classes that are
  // hand-coded.
  public void testCertainClassLiterals() {
    assertEquals("java.lang.Object", Object.class.getName());
    assertEquals("java.lang.String", String.class.getName());
    assertEquals("java.lang.Cloneable", Cloneable.class.getName());
    assertEquals("java.lang.Number", Number.class.getName());
    assertEquals("java.lang.Iterable", Iterable.class.getName());
    assertEquals("java.lang.Throwable", Throwable.class.getName());
    assertEquals("java.lang.reflect.AccessibleObject", AccessibleObject.class.getName());
    assertEquals("java.lang.reflect.Constructor", Constructor.class.getName());
    assertEquals("java.lang.reflect.Field", Field.class.getName());
    assertEquals("java.lang.reflect.Method", Method.class.getName());
  }

  // Verify that mapped classes return the correct superclass from a Java view.
  public void testCertainSuperclasses() {
    assertNull("Non-null Object superclass", Object.class.getSuperclass());
    assertNull("Non-null Cloneable superclass", Cloneable.class.getSuperclass());
    assertNull("Non-null Iterable superclass", Iterable.class.getSuperclass());

    assertEquals("Bad String superclass", Object.class, String.class.getSuperclass());
    assertEquals("Bad Number superclass", Object.class, Number.class.getSuperclass());
    assertEquals("Bad Throwable superclass", Object.class, Throwable.class.getSuperclass());
    assertEquals("Bad AccessibleObject superclass", Object.class,
        AccessibleObject.class.getSuperclass());

    // Check a String subclass.
    assertEquals("Bad String superclass", Object.class, "foo".getClass().getSuperclass());
  }

  /**
   * Verify that a class with a package that has been renamed using an
   * ObjectiveCName annotation can be reflexively loaded.
   */
  public void testPackagePrefixAnnotation() throws Exception {
    // Lookup class by its Java name.
    Class<?> cls = Class.forName("java.lang.test.Example");
    Object instance = cls.newInstance();
    Method m = cls.getMethod("nativeClassName");
    String nativeName = (String) m.invoke(instance);

    // Native name should have an OK prefix, instead of a camel-cased package name.
    assertEquals("OKExample", nativeName);
  }

  public void testDeclaringClass() throws Exception {
    Class<?> thisClass = Class.forName("com.google.j2objc.ClassTest");
    assertNotNull(thisClass);
    Class<?> innerClass = Class.forName("com.google.j2objc.ClassTest$InnerClass");
    assertNotNull(innerClass);
    assertEquals(thisClass, innerClass.getDeclaringClass());
    Class<?> innerInterface = Class.forName("com.google.j2objc.ClassTest$InnerInterface");
    assertNotNull(innerInterface);
    assertEquals(thisClass, innerInterface.getDeclaringClass());
    Class<?> innerEnum = Class.forName("com.google.j2objc.ClassTest$InnerEnum");
    assertNotNull(innerEnum);
    assertEquals(thisClass, innerEnum.getDeclaringClass());
  }

  public void testClassIsAssignableToItself() throws Exception {
    Class<?> cls = Class.forName("java.util.HashMap");
    assertNotNull(cls);
    assertTrue(cls.isAssignableFrom(cls));
    Class<?> protocol = Class.forName("java.util.Map");
    assertNotNull(protocol);
    assertTrue(protocol.isAssignableFrom(protocol));
  }

  public void testCharSequenceClass() throws Exception {
    Class<?> cls = CharSequence.class;
    assertEquals("interface java.lang.CharSequence", cls.toString());
    assertEquals(CharSequence.class, Class.forName("java.lang.CharSequence"));
  }

  public void testFindPrefixMappedClass() throws Exception {
    Class<?> cls = Class.forName("com.google.j2objc.mappedpkg.TestClass");
    assertNotNull(cls);
    cls = Class.forName("com.google.j2objc.mappedpkg.TestClass$Inner");
    assertNotNull(cls);
  }

  public void testDefaultEnumMethods() throws Exception {
    Method[] methods = InnerEnum.class.getDeclaredMethods();
    assertEquals(2, methods.length);
    Method valuesMethod = InnerEnum.class.getDeclaredMethod("values");
    assertNotNull(valuesMethod);
    assertEquals("[Lcom.google.j2objc.ClassTest$InnerEnum;",
        valuesMethod.getReturnType().getName());
    Method valueOfMethod = InnerEnum.class.getDeclaredMethod("valueOf", String.class);
    assertNotNull(valueOfMethod);
    assertEquals(InnerEnum.class, valueOfMethod.getReturnType());
  }

  boolean canCallAsSubclass(Class<?> x, Class<?> y) {
    boolean callSuccessful;
    try {
      Class<?> z = x.asSubclass(y);
      callSuccessful = true;
    } catch (ClassCastException e) {
      callSuccessful = false;
    }

    boolean assignable = y.isAssignableFrom(x);
    assertEquals(assignable, callSuccessful);
    return callSuccessful;
  }

  public void testAsSubclassCalls() throws Exception {
    assertTrue(canCallAsSubclass(Integer.class, Object.class));
    assertFalse(canCallAsSubclass(Long.class, Integer.class));
    assertTrue(canCallAsSubclass(Integer[].class, Object[].class));
    assertTrue(canCallAsSubclass(Integer[].class, Object.class));
    assertFalse(canCallAsSubclass(int.class, Object.class));
    assertFalse(canCallAsSubclass(int.class, long.class));
    assertTrue(canCallAsSubclass(int[].class, Object.class));
    assertFalse(canCallAsSubclass(int[].class, Object[].class));

    assertTrue(canCallAsSubclass(InterfaceP.class, Object.class));
    assertFalse(canCallAsSubclass(InterfaceP.class, InterfaceQ.class));
    assertTrue(canCallAsSubclass(InterfaceP[].class, Object.class));
    assertTrue(canCallAsSubclass(InterfaceP[].class, Object[].class));
    assertTrue(canCallAsSubclass(InterfaceR.class, InterfaceP.class));
    assertFalse(canCallAsSubclass(InterfaceR.class, InterfaceQ.class));
    assertTrue(canCallAsSubclass(InterfaceR.class, InterfaceR.class));

    assertTrue(canCallAsSubclass(AbstractClassX.class, Object.class));
    assertFalse(canCallAsSubclass(AbstractClassX.class, AbstractClassY.class));
    assertTrue(canCallAsSubclass(AbstractClassX[].class, Object.class));
    assertTrue(canCallAsSubclass(AbstractClassX[].class, Object[].class));

    assertTrue(canCallAsSubclass(AbstractClassY.class, Object.class));
    assertTrue(canCallAsSubclass(AbstractClassY.class, InterfaceP.class));
    assertTrue(canCallAsSubclass(AbstractClassY[].class, Object.class));
    assertTrue(canCallAsSubclass(AbstractClassY[].class, Object[].class));
    assertFalse(canCallAsSubclass(AbstractClassY[].class, InterfaceP.class));
    assertTrue(canCallAsSubclass(AbstractClassY[].class, InterfaceP[].class));

    assertTrue(canCallAsSubclass(ConcreteClassA.class, AbstractClassX.class));
    assertFalse(canCallAsSubclass(ConcreteClassA.class, AbstractClassY.class));
    assertTrue(canCallAsSubclass(ConcreteClassA[].class, AbstractClassX[].class));

    assertTrue(canCallAsSubclass(ConcreteClassB.class, AbstractClassY.class));
    assertTrue(canCallAsSubclass(ConcreteClassB.class, InterfaceP.class));
    assertFalse(canCallAsSubclass(ConcreteClassB.class, InterfaceQ.class));
    assertTrue(canCallAsSubclass(ConcreteClassB[].class, AbstractClassY[].class));
    assertTrue(canCallAsSubclass(ConcreteClassB[].class, InterfaceP[].class));

    assertFalse(canCallAsSubclass(ConcreteClassC.class, ConcreteClassA.class));
    assertTrue(canCallAsSubclass(ConcreteClassC.class, ConcreteClassB.class));
    assertTrue(canCallAsSubclass(ConcreteClassC.class, InterfaceQ.class));
    assertTrue(canCallAsSubclass(ConcreteClassC[].class, ConcreteClassB[].class));
    assertTrue(canCallAsSubclass(ConcreteClassC[].class, InterfaceQ[].class));

    assertTrue(canCallAsSubclass(ConcreteClassD.class, InterfaceP.class));
    assertTrue(canCallAsSubclass(ConcreteClassD.class, InterfaceQ.class));

    assertTrue(canCallAsSubclass(ConcreteClassE.class, AbstractClassX.class));
    assertFalse(canCallAsSubclass(ConcreteClassE.class, AbstractClassY.class));
    assertTrue(canCallAsSubclass(ConcreteClassE.class, InterfaceP.class));
    assertTrue(canCallAsSubclass(ConcreteClassE.class, InterfaceQ.class));
  }

  public void testNewInstance() throws Exception {
    Class<?> cls = Class.forName("com.google.j2objc.ClassTest$PrivateConstructorClass");
    try {
      cls.newInstance();
      fail("Expected IllegalAccessException");
    } catch (IllegalAccessException expected) {
      // expected
    }

    cls = Class.forName("com.google.j2objc.ClassTest$NoNullaryConstructorClass");
    try {
      cls.newInstance();
      fail("Expected InstantiationException");
    } catch (InstantiationException expected) {
      // expected
    }

    cls = Class.forName("com.google.j2objc.ClassTest$InnerClass");
    try {
      cls.newInstance();
    } catch (Exception e) {
      fail("Failed to create an instance of " + cls);
    }
  }

  public void testCast() throws Exception {
    ConcreteClassC c = new ConcreteClassC();
    try {
      ConcreteClassA.class.cast(c);
      fail("Expected ClassCastException");
    } catch (ClassCastException e) {
      // expected
    }

    try {
      ConcreteClassB.class.cast(c);
    } catch (ClassCastException e) {
      fail("Failed to cast " + c + " to ConcreteClassB");
    }

    try {
      InterfaceR.class.cast(c);
      fail("Expected ClassCastException");
    } catch (ClassCastException e) {
      // expected
    }

    try {
      InterfaceQ.class.cast(c);
    } catch (ClassCastException e) {
      fail("Failed to cast " + c + " to InterfaceQ");
    }

    try {
      InterfaceQ.class.cast(null);
    } catch (ClassCastException e) {
      fail("Unexpected ClassCastException");
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

  interface InterfaceP {
  }

  interface InterfaceQ {

  }
  interface InterfaceR extends InterfaceP {
  }

  static abstract class AbstractClassX {
  }

  static abstract class AbstractClassY implements InterfaceP {
  }

  class ConcreteClassA extends AbstractClassX {
  }

  class ConcreteClassB extends AbstractClassY {
  }

  class ConcreteClassC extends ConcreteClassB implements InterfaceQ {
  }

  class ConcreteClassD implements InterfaceP, InterfaceQ {
  }

  class ConcreteClassE extends AbstractClassX implements InterfaceP, InterfaceQ {
  }

  static class PrivateConstructorClass {
    private PrivateConstructorClass() {}
  }

  static class NoNullaryConstructorClass {
    NoNullaryConstructorClass(int i) {}
  }
}

class Test$$With$$Dollar$$Signs {
}
