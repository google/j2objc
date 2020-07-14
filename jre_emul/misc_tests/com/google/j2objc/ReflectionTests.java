/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http:..www.apache.org.licenses.LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests that rely on the java.lang.reflect package. */
public class ReflectionTests {

  private static final Class<?>[] reflectionTests = {
    com.google.j2objc.ArrayTest.class,
    com.google.j2objc.ClassTest.class,
    com.google.j2objc.ReflectionTest.class,
    com.google.j2objc.reflect.ProxyTest.class,
    java.lang.reflect.MethodTest.class,
    libcore.java.lang.CharacterTest.class,
    libcore.java.lang.reflect.ArrayTest.class,
    libcore.java.lang.reflect.ConstructorTest.class,
    libcore.java.lang.reflect.FieldTest.class,
    libcore.java.lang.reflect.MethodTest.class,
    libcore.java.lang.reflect.ReflectionTest.class,
    libcore.java.lang.reflect.annotations.AnnotatedElementParameterTest.class,
    libcore.java.lang.reflect.annotations.Annotations57649Test.class,
    libcore.java.lang.reflect.annotations.AnnotationsTest.class,
    libcore.java.lang.reflect.annotations.ClassTest.class,
    libcore.java.lang.reflect.annotations.ConstructorTest.class,
    libcore.java.lang.reflect.annotations.ExecutableParameterTest.class,
    libcore.java.lang.reflect.annotations.FieldTest.class,
    libcore.java.lang.reflect.annotations.MethodTest.class,
    libcore.java.lang.reflect.annotations.PackageTest.class,
    org.apache.harmony.tests.java.lang.ClassTest.class,
    org.apache.harmony.tests.java.lang.StringTest.class,
    org.apache.harmony.tests.java.lang.reflect.AccessibleObjectTest.class,
    org.apache.harmony.tests.java.lang.reflect.ArrayTest.class,
    org.apache.harmony.tests.java.lang.reflect.ConstructorTest.class,
    org.apache.harmony.tests.java.lang.reflect.FieldTest.class,
    org.apache.harmony.tests.java.lang.reflect.GenericArrayTypeTest.class,
    org.apache.harmony.tests.java.lang.reflect.MalformedParameterizedTypeExceptionTest.class,
    org.apache.harmony.tests.java.lang.reflect.MethodTest.class,
    org.apache.harmony.tests.java.lang.reflect.ModifierTest.class,
    org.apache.harmony.tests.java.lang.reflect.ParameterizedTypeTest.class,
    org.apache.harmony.tests.java.lang.reflect.ProxyTest.class,
    org.apache.harmony.tests.java.lang.reflect.TypeVariableTest.class,
    org.apache.harmony.tests.java.lang.reflect.UndeclaredThrowableExceptionTest.class,
    org.apache.harmony.tests.java.lang.reflect.WildcardTypeTest.class,
    org.apache.harmony.tests.java.util.ArraysTest.class,
    tests.api.java.lang.reflect.ProxyTest.class,
  };

  public static Test suite() {
    return new TestSuite(reflectionTests);
  }
}
