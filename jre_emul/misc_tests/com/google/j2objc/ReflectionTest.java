/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

/**
 * Miscellaneous tests for J2ObjC's reflection support.
 */
public class ReflectionTest extends TestCase {

  @Target({ElementType.CONSTRUCTOR})
  @Retention(RetentionPolicy.RUNTIME)
  @interface Mumble {}

  static class NoEquals {
    public NoEquals() {}

    @Mumble
    public NoEquals(String s) {}
  }

  static interface Defaults {
    public boolean noDefault();

    public default boolean withDefault() {
      return true;
    }
  }

  static class HasDefault implements Defaults {

    @Override
    public boolean noDefault() {
      return false;
    }

    public void unrelatedMethod() {}
  }

  static class ParameterizedReturnTest {
    // Method that returns a parameterized type.
    public List<String> getStringList() {
      return null;
    }
  }

  static class TestFields {
    public long longField;
  }

  static class TestStaticInit {
    public static TestStaticInit instance = new TestStaticInit();
  }

  // Assert equals method can be found using reflection. Because it's a mapped
  // method with a parameter, reflection was trying to find "equalsWithId:"
  // instead of "isEqual:".
  public void testEqualsMethodLookup() throws Exception {
    Method m = Integer.class.getMethod("equals", new Class<?>[] { Object.class });
    assertNotNull(m);
    Integer uno = new Integer(1);
    Integer dos = new Integer(2);
    Boolean b = (Boolean) m.invoke(uno,  new Object[] { dos });
    assertFalse(b);
    b = (Boolean) m.invoke(uno,  new Object[] { uno });
    assertTrue(b);

    NoEquals obj1 = new NoEquals();
    NoEquals obj2 = new NoEquals();
    m = NoEquals.class.getMethod("equals", new Class<?>[] { Object.class });
    assertNotNull(m);
    assertFalse ((Boolean) m.invoke(obj1, obj2));
    assertTrue ((Boolean) m.invoke(obj1, obj1));
  }

  // Verify non-default constructor annotations are returned. Issue 473
  // reported that the annotations for the default constructor are always
  // returned, regardless of whether the constructor had parameters.
  public void testNonDefaultConstructorAnnotations() {
    Constructor<?>[] constructors = NoEquals.class.getDeclaredConstructors();
    for (Constructor<?> c : constructors) {
      if (c.getParameterTypes().length == 0) {
        // Default constructor should not have a Mumble annotation.
        assertNull(c.getAnnotation(Mumble.class));
      } else {
        assertNotNull(c.getAnnotation(Mumble.class));
      }
    }
  }

  public void testParameterizedTypeMethodReturn() throws Exception {
    Method method = ParameterizedReturnTest.class.getMethod("getStringList");
    Type returnType = method.getGenericReturnType();
    assertTrue(returnType instanceof ParameterizedType);
  }

  // Regression for Issue 705.
  public void testAssignLong() throws Exception {
    TestFields o = new TestFields();
    Field field = TestFields.class.getField("longField");
    field.set(o, 3000000000L);
    assertEquals(3000000000L, o.longField);
  }

  // Regression for issue 767.
  public void testStaticInitialization() throws Exception {
    Class<?> unsafeClass = Class.forName("com.google.j2objc.ReflectionTest$TestStaticInit");
    Field field = unsafeClass.getDeclaredField("instance");
    // Verify instance isn't null, indicating the class's +initialize method ran.
    assertNotNull(field.get(null));
  }

  public void testIsDefaultMethod() throws Exception {
    // Test interface with a default method.
    Class<?> defaultsInterface = Class.forName("com.google.j2objc.ReflectionTest$Defaults");
    Method m = defaultsInterface.getMethod("withDefault");
    assertTrue("isDefault false for default method", m.isDefault());
    m = defaultsInterface.getMethod("noDefault");
    assertFalse("isDefault true for non-default method", m.isDefault());

    // Test implementing class.
    Class<?> defaultClass = Class.forName("com.google.j2objc.ReflectionTest$HasDefault");
    m = defaultClass.getMethod("withDefault");
    assertTrue("isDefault false for default method", m.isDefault());
    m = defaultClass.getMethod("noDefault");
    assertFalse("isDefault true for non-default method", m.isDefault());
    m = defaultClass.getMethod("unrelatedMethod");
    assertFalse("isDefault true for unrelated method", m.isDefault());
  }

  enum Color { RED, GREEN, BLUE }

  @Retention(RetentionPolicy.RUNTIME)
  @interface OtherAnnotation {
    String value() default "hello";
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface AnnotationWithDefaults {
    int intValue() default 123;
    String stringValue() default "abc";
    Color enumValue() default Color.RED;
    String[] stringArrayValue() default { "foo", "bar" };
    int[] intArrayValue() default { 4, 5, 6 };
    OtherAnnotation annotationValue() default @OtherAnnotation;
    Class<?> classValue() default Iterable.class;
  }

  @AnnotationWithDefaults
  static class AnnotatedClass {}

  public void testAnnotationInitializedWithDefaults() {
    AnnotationWithDefaults a = AnnotatedClass.class.getAnnotation(AnnotationWithDefaults.class);
    assertEquals(123, a.intValue());
    assertEquals("abc", a.stringValue());
    assertEquals(Color.RED, a.enumValue());
    assertTrue(Arrays.equals(new String[] { "foo", "bar" }, a.stringArrayValue()));
    assertTrue(Arrays.equals(new int[] { 4, 5, 6 }, a.intArrayValue()));
    assertEquals("hello", a.annotationValue().value());
    assertEquals(Iterable.class, a.classValue());
  }
}
