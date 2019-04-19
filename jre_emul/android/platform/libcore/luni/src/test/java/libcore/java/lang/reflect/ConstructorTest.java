/*
 * Copyright (C) 2009 The Android Open Source Project
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

package libcore.java.lang.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

import junit.framework.TestCase;

public final class ConstructorTest extends TestCase {
    public void test_getExceptionTypes() throws Exception {
        Constructor<?> constructor = ConstructorTestHelper.class.getConstructor(new Class[0]);
        Class[] exceptions = constructor.getExceptionTypes();
        assertEquals(1, exceptions.length);
        assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
        // Check that corrupting our array doesn't affect other callers.
        exceptions[0] = NullPointerException.class;
        exceptions = constructor.getExceptionTypes();
        assertEquals(1, exceptions.length);
        assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
    }

    public void test_getParameterTypes() throws Exception {
        Class[] expectedParameters = new Class[0];
        Constructor<?> constructor = ConstructorTestHelper.class.getConstructor(expectedParameters);
        assertEquals(0, constructor.getParameterTypes().length);

        expectedParameters = new Class[] { Object.class };
        constructor = ConstructorTestHelper.class.getConstructor(expectedParameters);
        Class[] parameters = constructor.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
        // Check that corrupting our array doesn't affect other callers.
        parameters[0] = String.class;
        parameters = constructor.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
    }

    public void test_getParameterCount() throws Exception {
        Class[] expectedParameters = new Class[0];
        Constructor<?> constructor = ConstructorTestHelper.class.getConstructor(expectedParameters);
        assertEquals(0, constructor.getParameterCount());

        expectedParameters = new Class[] { Object.class };
        constructor = ConstructorTestHelper.class.getConstructor(expectedParameters);
        int count = constructor.getParameterCount();
        assertEquals(1, count);
    }

    public void test_getParameters() throws Exception {
        Class[] expectedParameters = new Class[0];
        Constructor<?> constructor = ConstructorTestHelper.class.getConstructor(expectedParameters);
        assertEquals(0, constructor.getParameters().length);

        expectedParameters = new Class[] { Object.class };
        constructor = ConstructorTestHelper.class.getConstructor(expectedParameters);

        // Test the information available via other Constructor methods. See ParameterTest and
        // annotations.ParameterTest for more in-depth Parameter testing.
        Parameter[] parameters = constructor.getParameters();
        assertEquals(1, parameters.length);
        assertEquals(Object.class, parameters[0].getType());

        // Check that corrupting our array doesn't affect other callers.
        parameters[0] = null;
        parameters = constructor.getParameters();
        assertEquals(1, parameters.length);
        assertEquals(Object.class, parameters[0].getType());
    }

    public void testGetConstructorWithNullArgumentsArray() throws Exception {
        Constructor<?> constructor = ConstructorTestHelper.class.getConstructor((Class[]) null);
        assertEquals(0, constructor.getParameterTypes().length);
    }

    public void testGetConstructorWithNullArgument() throws Exception {
        try {
            ConstructorTestHelper.class.getConstructor(new Class[] { null });
            fail();
        } catch (NoSuchMethodException expected) {
        }
    }

    public void testGetConstructorReturnsDoesNotReturnPrivateConstructor() throws Exception {
        try {
            ConstructorTestHelper.class.getConstructor(Object.class, Object.class);
            fail();
        } catch (NoSuchMethodException expected) {
        }
    }

    public void testGetDeclaredConstructorReturnsPrivateConstructor() throws Exception {
        Constructor<?> constructor = ConstructorTestHelper.class.getDeclaredConstructor(
                Object.class, Object.class);
        assertEquals(2, constructor.getParameterTypes().length);
    }

    public void testEqualConstructorEqualsAndHashCode() throws Exception {
        Constructor<?> c1 = ConstructorTestHelper.class.getConstructor();
        Constructor<?> c2 = ConstructorTestHelper.class.getConstructor();
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    public void testHashCodeSpec() throws Exception {
        Constructor<?> c1 = ConstructorTestHelper.class.getConstructor();
        assertEquals(ConstructorTestHelper.class.getName().hashCode(), c1.hashCode());
    }

    public void testDifferentConstructorEqualsAndHashCode() throws Exception {
        Constructor<?> c1 = ConstructorTestHelper.class.getConstructor();
        Constructor<?> c2 = ConstructorTestHelper.class.getConstructor(Object.class);
        assertFalse(c1.equals(c2));
    }

    public void testToString() throws Exception {
        checkToString(
                "public libcore.java.lang.reflect.ConstructorTest$ConstructorTestHelper() throws java.lang.IndexOutOfBoundsException",
                ConstructorTestHelper.class);
        checkToString(
                "public libcore.java.lang.reflect.ConstructorTest$ConstructorTestHelper(java.lang.Object)",
                ConstructorTestHelper.class, Object.class);
        checkToString(
                "private libcore.java.lang.reflect.ConstructorTest$ConstructorTestHelper(java.lang.Object,java.lang.Object)",
                ConstructorTestHelper.class, Object.class, Object.class);
        checkToString(
                "public libcore.java.lang.reflect.ConstructorTest$GenericConstructorTestHelper() throws java.lang.Exception",
                GenericConstructorTestHelper.class);
        checkToString(
                "public libcore.java.lang.reflect.ConstructorTest$GenericConstructorTestHelper(java.lang.String)",
                GenericConstructorTestHelper.class, String.class);
        checkToString(
                "public libcore.java.lang.reflect.ConstructorTest$GenericConstructorTestHelper(java.lang.String,java.lang.Integer)",
                GenericConstructorTestHelper.class, String.class, Integer.class);
    }

    private static void checkToString(String expected, Class<?> clazz, Class... constructorArgTypes)
            throws Exception {
        Constructor c = clazz.getDeclaredConstructor(constructorArgTypes);
        assertEquals(expected, c.toString());
    }

    public void testToGenericString() throws Exception {
        checkToGenericString(
                "public libcore.java.lang.reflect.ConstructorTest$ConstructorTestHelper() throws java.lang.IndexOutOfBoundsException",
                ConstructorTestHelper.class);
        checkToGenericString(
                "public libcore.java.lang.reflect.ConstructorTest$ConstructorTestHelper(java.lang.Object)",
                ConstructorTestHelper.class, Object.class);
        checkToGenericString(
                "private libcore.java.lang.reflect.ConstructorTest$ConstructorTestHelper(java.lang.Object,java.lang.Object)",
                ConstructorTestHelper.class, Object.class, Object.class);
        checkToGenericString(
                "public libcore.java.lang.reflect.ConstructorTest$GenericConstructorTestHelper() throws E",
                GenericConstructorTestHelper.class);
        checkToGenericString(
                "public libcore.java.lang.reflect.ConstructorTest$GenericConstructorTestHelper(A)",
                GenericConstructorTestHelper.class, String.class);
        checkToGenericString(
                "public <B> libcore.java.lang.reflect.ConstructorTest$GenericConstructorTestHelper(A,B)",
                GenericConstructorTestHelper.class, String.class, Integer.class);
    }

    private static void checkToGenericString(String expected, Class<?> clazz,
            Class... constructorArgTypes) throws Exception {
        Constructor c = clazz.getDeclaredConstructor(constructorArgTypes);
        assertEquals(expected, c.toGenericString());
    }

    static class ConstructorTestHelper {
        public ConstructorTestHelper() throws IndexOutOfBoundsException { }
        public ConstructorTestHelper(Object o) { }
        private ConstructorTestHelper(Object a, Object b) { }
    }

    static class GenericConstructorTestHelper<A extends String, E extends Exception> {
        public GenericConstructorTestHelper() throws E { }
        public GenericConstructorTestHelper(A a) { }
        public <B extends Integer> GenericConstructorTestHelper(A a, B b) { }
    }
}
