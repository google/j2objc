/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Set;

public class MethodTest extends junit.framework.TestCase {

    static class TestMethod {
        public TestMethod() {
        }

        public void voidMethod() throws IllegalArgumentException {
        }

        public void parmTest(int x, short y, String s, boolean bool, Object o,
                long l, byte b, char c, double d, float f) {
        }

        public int intMethod() {
            return 1;
        }

        public static final void printTest(int x, short y, String s,
                boolean bool, Object o, long l, byte b, char c, double d,
                float f) {
        }

        public double doubleMethod() {
            return 1.0;
        }

        public short shortMethod() {
            return (short) 1;
        }

        public byte byteMethod() {
            return (byte) 1;
        }

        public float floatMethod() {
            return 1.0f;
        }

        public long longMethod() {
            return 1l;
        }

        public char charMethod() {
            return 'T';
        }

        public Object objectMethod() {
            return new Object();
        }

        private static void prstatic() {
        }

        public static void pustatic() {
        }

        public static synchronized void pustatsynch() {
        }

        public static int invokeStaticTest() {
            return 1;
        }

        public int invokeInstanceTest() {
            return 1;
        }

        private int privateInvokeTest() {
            return 1;
        }

        public int invokeExceptionTest() throws NullPointerException {
            throw new NullPointerException();
        }

        public static synchronized native void pustatsynchnat() /*-[
          // Empty method body to satisfy link error.
        ]-*/;

        public void publicVoidVarargs(Object... param){}
        public void publicVoidArray(Object[] param){}

        public void annotatedParameter(@TestAnno @Deprecated int a,
                @Deprecated int b, int c) {
        }

        @Deprecated
        @TestAnno
        public void annotatedMethod(){}

        public void hashCodeTest(int i){}
        public void hashCodeTest(String s){}

        public void invokeCastTest1(byte param) {
        }

        public void invokeCastTest1(short param) {
        }

        public void invokeCastTest1(int param) {
        }

        public void invokeCastTest1(long param) {
        }

        public void invokeCastTest1(float param) {
        }

        public void invokeCastTest1(double param) {
        }

        public void invokeCastTest1(char param) {
        }

        public void invokeCastTest1(boolean param) {
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    public static @interface TestAnno{
        public static final String DEFAULT_VALUE = "DEFAULT_VALUE";

        String value() default DEFAULT_VALUE;
    }

    abstract class AbstractTestMethod {
        public abstract void puabs();
    }

    class TestMethodSub extends TestMethod {
        public int invokeInstanceTest() {
            return 0;
        }
    }

    static interface IBrigeTest<T>{
        T m();
    }

    static class BrigeTest implements IBrigeTest<String> {
        public String m(){ return null; }
    }

    static class ExceptionTest<T extends Exception>{
        @SuppressWarnings("unused")
        void exceptionTest() throws T{}
    }

    static class GenericReturnType<T> {
        T returnGeneric(){return null;}
    }

    static class GenericString<T> {
      public static final String GENERIC =
      "T org.apache.harmony.tests.java.lang.reflect.MethodTest$GenericString.genericString(T)";
        T genericString(T t) {
            return null;
        }
    }

    /**
     * java.lang.reflect.Method#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean
        // java.lang.reflect.Method.equals(java.lang.Object)

        Method m1 = null, m2 = null;
        try {
            m1 = TestMethod.class.getMethod("invokeInstanceTest", new Class[0]);
            m2 = TestMethodSub.class.getMethod("invokeInstanceTest",
                    new Class[0]);
        } catch (Exception e) {
            fail("Exception during equals test : " + e.getMessage());
        }
        assertTrue("Overriden method returned equal", !m1.equals(m2));
        assertTrue("Same method returned not-equal", m1.equals(m1));
        try {
            m1 = TestMethod.class.getMethod("invokeStaticTest", new Class[0]);
            m2 = TestMethodSub.class
                    .getMethod("invokeStaticTest", new Class[0]);
        } catch (Exception e) {
            fail("Exception during equals test : " + e.getMessage());
        }
        assertTrue("Inherited method returned not-equal", m1.equals(m2));
    }

    /**
     * java.lang.Class#getMethod(java.lang.String, java.lang.Class[])
     */
    public void test_getMethod() throws NoSuchMethodException, SecurityException {
        // Check that getMethod treats null parameterTypes the same as an empty array.
        Method m1 = TestMethod.class.getMethod("invokeInstanceTest", new Class[0]);
        Method m2 = TestMethod.class.getMethod("invokeInstanceTest", (Class[]) null);
        assertEquals(m1, m2);
    }

    /**
     * java.lang.Class#getDeclaredMethod(java.lang.String, java.lang.Class[])
     */
    public void test_getDeclaredMethod() throws NoSuchMethodException, SecurityException {
        // Check that getDeclaredMethod treats null parameterTypes the same as an empty array.
        Method m1 = TestMethod.class.getDeclaredMethod("invokeInstanceTest", new Class[0]);
        Method m2 = TestMethod.class.getDeclaredMethod("invokeInstanceTest", (Class[]) null);
        assertEquals(m1, m2);
    }

    /**
     * java.lang.reflect.Method#getDeclaringClass()
     */
    public void test_getDeclaringClass() {
        // Test for method java.lang.Class
        // java.lang.reflect.Method.getDeclaringClass()

        Method[] mths;

        try {
            mths = TestMethod.class.getDeclaredMethods();
            assertTrue("Returned incorrect declaring class: "
                    + mths[0].getDeclaringClass().toString(), mths[0]
                    .getDeclaringClass().equals(TestMethod.class));
        } catch (Exception e) {
            fail("Exception during getDeclaringClass test: "
                    + e.toString());
        }
    }

    /**
     * java.lang.reflect.Method#getExceptionTypes()
     */
    public void test_getExceptionTypes() {
        // Test for method java.lang.Class []
        // java.lang.reflect.Method.getExceptionTypes()

        try {
            Method mth = TestMethod.class.getMethod("voidMethod", new Class[0]);
            Class[] ex = mth.getExceptionTypes();
            assertEquals("Returned incorrect number of exceptions",
                    1, ex.length);
            assertTrue("Returned incorrect exception type", ex[0]
                    .equals(IllegalArgumentException.class));
            mth = TestMethod.class.getMethod("intMethod", new Class[0]);
            ex = mth.getExceptionTypes();
            assertEquals("Returned incorrect number of exceptions",
                    0, ex.length);
        } catch (Exception e) {
            fail("Exception during getExceptionTypes: " + e.toString());
        }

    }

    /**
     * java.lang.reflect.Method#getModifiers()
     */
    public void test_getModifiers() {
        // Test for method int java.lang.reflect.Method.getModifiers()

        Class cl = TestMethod.class;
        int mods = 0;
        Method mth = null;
        int mask = 0;
        try {
            mth = cl.getMethod("pustatic", new Class[0]);
            mods = mth.getModifiers();
        } catch (Exception e) {
            fail("Exception during getModfiers test: " + e.toString());
        }
        mask = Modifier.PUBLIC | Modifier.STATIC;
        assertTrue("Incorrect modifiers returned", (mods | mask) == mask);
        try {
            mth = cl.getDeclaredMethod("prstatic", new Class[0]);
            mods = mth.getModifiers();
        } catch (Exception e) {
            fail("Exception during getModfiers test: " + e.toString());
        }
        mask = Modifier.PRIVATE | Modifier.STATIC;
        assertTrue("Incorrect modifiers returned", (mods | mask) == mask);
        try {
            mth = cl.getDeclaredMethod("pustatsynch", new Class[0]);
            mods = mth.getModifiers();
        } catch (Exception e) {
            fail("Exception during getModfiers test: " + e.toString());
        }
        mask = (Modifier.PUBLIC | Modifier.STATIC) | Modifier.SYNCHRONIZED;
        assertTrue("Incorrect modifiers returned", (mods | mask) == mask);
        try {
            mth = cl.getDeclaredMethod("pustatsynchnat", new Class[0]);
            mods = mth.getModifiers();
        } catch (Exception e) {
            fail("Exception during getModfiers test: " + e.toString());
        }
        mask = ((Modifier.PUBLIC | Modifier.STATIC) | Modifier.SYNCHRONIZED)
                | Modifier.NATIVE;
        assertTrue("Incorrect modifiers returned", (mods | mask) == mask);
        cl = AbstractTestMethod.class;
        try {
            mth = cl.getDeclaredMethod("puabs", new Class[0]);
            mods = mth.getModifiers();
        } catch (Exception e) {
            fail("Exception during getModfiers test: " + e.toString());
        }
        mask = Modifier.PUBLIC | Modifier.ABSTRACT;
        assertTrue("Incorrect modifiers returned", (mods | mask) == mask);
    }

    /**
     * java.lang.reflect.Method#getName()
     */
    public void test_getName() {
        // Test for method java.lang.String java.lang.reflect.Method.getName()
        Method mth = null;
        try {
            mth = TestMethod.class.getMethod("voidMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getMethodName(): " + e.toString());
        }
        assertEquals("Returned incorrect method name",
                "voidMethod", mth.getName());
    }

    /**
     * java.lang.reflect.Method#isVarArgs()
     */
    public void test_isVarArgs() throws Exception {
        Method mth = TestMethod.class.getMethod("publicVoidVarargs",
                Object[].class);
        assertTrue("Varargs method stated as non vararg.", mth.isVarArgs());

        mth = TestMethod.class.getDeclaredMethod("publicVoidArray",
                Object[].class);
        assertFalse("Non varargs method stated as vararg.", mth.isVarArgs());
    }

    // Disabled, as j2objc doesn't create bridge methods.
//    /**
//     * java.lang.reflect.Method#isBridge()
//     */
//    public void test_isBridge() throws Exception {
//        Method[] declaredMethods = BrigeTest.class.getDeclaredMethods();
//        assertEquals("Bridge method not generated.", 2, declaredMethods.length);
//        boolean foundBridgeMethod = false;
//        for (Method method : declaredMethods) {
//            if (method.getReturnType().equals(Object.class)) {
//                assertTrue("Bridge method not stated as bridge.", method
//                        .isBridge());
//                foundBridgeMethod = true;
//            }
//        }
//        assertTrue("Bridge method not found.", foundBridgeMethod);
//    }

    // Disabled, as j2objc creates different synthetic methods than Java compilers.
//    /**
//     * java.lang.reflect.Method#isSynthetic()
//     */
//    public void test_isSynthetic() throws Exception {
//        Method[] declaredMethods = BrigeTest.class.getDeclaredMethods();
//        assertEquals("Synthetic method not generated.", 2,
//                declaredMethods.length);
//        boolean foundSyntheticMethod = false;
//        for (Method method : declaredMethods) {
//            if (method.getReturnType().equals(Object.class)) {
//                assertTrue("Synthetic method not stated as synthetic.", method
//                        .isSynthetic());
//                foundSyntheticMethod = true;
//            }
//        }
//        assertTrue("Synthetic method not found.", foundSyntheticMethod);
//    }

    /**
     * java.lang.reflect.Method#getParameterAnnotations()
     */
    public void test_getParameterAnnotations() throws Exception {
        Method method = TestMethod.class.getDeclaredMethod(
                "annotatedParameter", new Class[] {
                        int.class, int.class, int.class});
        Annotation[][] annotations = method.getParameterAnnotations();
        assertEquals(3, annotations.length);
        assertEquals(
                "Wrong number of annotations returned for first parameter", 2,
                annotations[0].length);
        Set<Class<?>> annotationSet = new HashSet<Class<?>>();
        annotationSet.add(annotations[0][0].annotationType());
        annotationSet.add(annotations[0][1].annotationType());
        assertTrue("Missing TestAnno annotation", annotationSet
                .contains(TestAnno.class));
        assertTrue("Missing Deprecated annotation", annotationSet
                .contains(Deprecated.class));

        assertEquals(
                "Wrong number of annotations returned for second parameter",
                1, annotations[1].length);
        annotationSet = new HashSet<Class<?>>();
        annotationSet.add(annotations[1][0].annotationType());
        assertTrue("Missing Deprecated annotation", annotationSet
                .contains(Deprecated.class));
        assertEquals(
                "Wrong number of annotations returned for third parameter", 0,
                annotations[2].length);
    }

    /**
     * java.lang.reflect.Method#getDeclaredAnnotations()
     */
    public void test_getDeclaredAnnotations() throws Exception {
        Method method = TestMethod.class.getDeclaredMethod("annotatedMethod");
        Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
        assertEquals(2, declaredAnnotations.length);

        Set<Class<?>> annotationSet = new HashSet<Class<?>>();
        annotationSet.add(declaredAnnotations[0].annotationType());
        annotationSet.add(declaredAnnotations[1].annotationType());
        assertTrue("Missing TestAnno annotation", annotationSet
                .contains(TestAnno.class));
        assertTrue("Missing Deprecated annotation", annotationSet
                .contains(Deprecated.class));
    }

    /**
     * java.lang.reflect.Method#getDefaultValue()
     */
    public void test_getDefaultValue() throws Exception {
        Method method = TestAnno.class.getDeclaredMethod("value");
        assertEquals("Wrong default value returned", TestAnno.DEFAULT_VALUE,
                method.getDefaultValue());
    }

    /**
     * java.lang.reflect.Method#getDefaultValue()
     */
    public void test_getGenericExceptionTypes() throws Exception {
        Method method = ExceptionTest.class.getDeclaredMethod("exceptionTest");
        Type[] genericExceptionTypes = method.getGenericExceptionTypes();
        assertEquals(1, genericExceptionTypes.length);
        assertTrue(genericExceptionTypes[0] instanceof TypeVariable<?>);
        @SuppressWarnings("unchecked")
        TypeVariable<Class<ExceptionTest<?>>> tv =
            (TypeVariable<Class<ExceptionTest<?>>>) genericExceptionTypes[0];
        assertEquals("T", tv.getName());
    }

    /**
     * java.lang.reflect.Method#getGenericReturnType()
     */
    public void test_getGenericReturnType() throws Exception {
        Method method = GenericReturnType.class
                .getDeclaredMethod("returnGeneric");
        Type returnType = method.getGenericReturnType();
        assertNotNull("getGenericReturnType returned null", returnType);
        assertTrue(returnType instanceof TypeVariable<?>);
        @SuppressWarnings("unchecked")
        TypeVariable<Class<ExceptionTest<?>>> tv =
            (TypeVariable<Class<ExceptionTest<?>>>) returnType;
        assertEquals("T", tv.getName());
    }


    /**
     * java.lang.reflect.Method#toGenericString()
     */
    public void test_toGenericString() throws Exception {
        Method method = GenericString.class.getDeclaredMethod("genericString",
                Object.class);
        assertEquals("Wrong generic String returned", GenericString.GENERIC,
                method.toGenericString());
    }






    /**
     * java.lang.reflect.Method#hashCode()
     */
    public void test_hashCode() throws Exception {
        Method mth0 = TestMethod.class.getMethod("hashCodeTest", String.class);
        Method mth1 = TestMethod.class.getDeclaredMethod("hashCodeTest",
                int.class);
        assertEquals("Methods with same name did not return same hashCode.",
                mth0.hashCode(), mth1.hashCode());
    }

    /**
     * java.lang.reflect.Method#getParameterTypes()
     */
    public void test_getParameterTypes() {
        // Test for method java.lang.Class []
        // java.lang.reflect.Method.getParameterTypes()
        Class cl = TestMethod.class;
        Method mth = null;
        Class[] parms = null;
        Method[] methods = null;
        Class[] plist = { int.class, short.class, String.class, boolean.class,
                Object.class, long.class, byte.class, char.class, double.class,
                float.class };
        try {
            mth = cl.getMethod("voidMethod", new Class[0]);
            parms = mth.getParameterTypes();
        } catch (Exception e) {
            fail("Exception during getParameterTypes test: "
                    + e.toString());
        }
        assertEquals("Returned incorrect parameterTypes", 0, parms.length);
        try {
            mth = cl.getMethod("parmTest", plist);
            parms = mth.getParameterTypes();
        } catch (Exception e) {
            fail("Exception during getParameterTypes test: "
                    + e.toString());
        }
        assertTrue("Invalid number of parameters returned",
                plist.length == parms.length);
        for (int i = 0; i < plist.length; i++)
            assertTrue("Incorrect parameter returned", plist[i]
                    .equals(parms[i]));

        // Test same method. but this time pull it from the list of methods
        // rather than asking for it explicitly
        methods = cl.getDeclaredMethods();

        int i;
        for (i = 0; i < methods.length; i++)
            if (methods[i].getName().equals("parmTest")) {
                mth = methods[i];
                i = methods.length + 1;
            }
        if (i < methods.length) {
            parms = mth.getParameterTypes();
            assertTrue("Incorrect number of parameters returned",
                    parms.length == plist.length);
            for (i = 0; i < plist.length; i++)
                assertTrue("Incorrect parameter returned", plist[i]
                        .equals(parms[i]));
        }
    }

    /**
     * java.lang.reflect.Method#getReturnType()
     */
    public void test_getReturnType() {
        // Test for method java.lang.Class
        // java.lang.reflect.Method.getReturnType()
        Class cl = TestMethod.class;
        Method mth = null;
        try {
            mth = cl.getMethod("charMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted char", mth
                .getReturnType().equals(char.class));
        try {
            mth = cl.getMethod("longMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted long", mth
                .getReturnType().equals(long.class));
        try {
            mth = cl.getMethod("shortMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted short", mth
                .getReturnType().equals(short.class));
        try {
            mth = cl.getMethod("intMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted int: "
                + mth.getReturnType(), mth.getReturnType().equals(int.class));
        try {
            mth = cl.getMethod("doubleMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted double", mth
                .getReturnType().equals(double.class));
        try {
            mth = cl.getMethod("byteMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted byte", mth
                .getReturnType().equals(byte.class));
        try {
            mth = cl.getMethod("byteMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test:" + e.toString());
        }
        assertTrue("Gave incorrect returne type, wanted byte", mth
                .getReturnType().equals(byte.class));
        try {
            mth = cl.getMethod("objectMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted Object", mth
                .getReturnType().equals(Object.class));

        try {
            mth = cl.getMethod("voidMethod", new Class[0]);
        } catch (Exception e) {
            fail("Exception during getReturnType test : " + e.getMessage());
        }
        assertTrue("Gave incorrect returne type, wanted void", mth
                .getReturnType().equals(void.class));
    }

    /**
     * java.lang.reflect.Method#invoke(java.lang.Object,
     *        java.lang.Object[])
     */
    public void test_invokeLjava_lang_Object$Ljava_lang_Object() throws Exception{
        // Test for method java.lang.Object
        // java.lang.reflect.Method.invoke(java.lang.Object, java.lang.Object
        // [])
        Class cl = TestMethod.class;
        Class[] dcl = new Class[0];

        // Get and invoke a static method
        Method mth = cl.getDeclaredMethod("invokeStaticTest", dcl);
        Object ret = mth.invoke(null, new Object[0]);
        assertEquals("Invoke returned incorrect value", 1, ((Integer) ret)
                .intValue());

        // Get and invoke an instance method
        mth = cl.getDeclaredMethod("invokeInstanceTest", dcl);
        ret = mth.invoke(new TestMethod(), new Object[0]);
        assertEquals("Invoke returned incorrect value", 1, ((Integer) ret)
                .intValue());

        // Get and attempt to invoke a private method
        mth = cl.getDeclaredMethod("privateInvokeTest", dcl);
        try {
            ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (IllegalAccessException e) {
            // Correct behaviour
        } catch (Exception e) {
            fail("Exception during invoke test : " + e.getMessage());
        }
        // Generate an IllegalArgumentException
        mth = cl.getDeclaredMethod("invokeInstanceTest", dcl);

        try {
            Object[] args = { Object.class };
            ret = mth.invoke(new TestMethod(), args);
        } catch (IllegalArgumentException e) {
            // Correct behaviour
        } catch (Exception e) {
            fail("Exception during invoke test : " + e.getMessage());
        }

        // Generate a NullPointerException
        mth = cl.getDeclaredMethod("invokeInstanceTest", dcl);

        try {
            ret = mth.invoke(null, new Object[0]);
        } catch (NullPointerException e) {
            // Correct behaviour
        } catch (Exception e) {
            fail("Exception during invoke test : " + e.getMessage());
        }

        // Generate an InvocationTargetException
        mth = cl.getDeclaredMethod("invokeExceptionTest", dcl);
        try {
            ret = mth.invoke(new TestMethod(), new Object[0]);
        } catch (InvocationTargetException e) {
            // Correct behaviour
        } catch (Exception e) {
            fail("Exception during invoke test : " + e.getMessage());
        }

        TestMethod testMethod = new TestMethod();
        Method methods[] = cl.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("invokeCastTest1")) {
                Class param = methods[i].getParameterTypes()[0];

                try {
                    methods[i].invoke(testMethod, new Object[] { new Byte(
                            (byte) 1) });
                    assertTrue("invalid invoke with Byte: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Float.TYPE
                                    || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Byte invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Byte invalid failure: " + methods[i],
                            param == Boolean.TYPE || param == Character.TYPE);
                }

                try {
                    methods[i].invoke(testMethod, new Object[] { new Short(
                            (short) 1) });
                    assertTrue("invalid invoke with Short: " + methods[i],
                            param == Short.TYPE || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Float.TYPE
                                    || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Short invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Short invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Boolean.TYPE
                                    || param == Character.TYPE);
                }

                try {
                    methods[i].invoke(testMethod,
                            new Object[] { new Integer(1) });
                    assertTrue("invalid invoke with Integer: " + methods[i],
                            param == Integer.TYPE || param == Long.TYPE
                                    || param == Float.TYPE
                                    || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Integer invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Integer invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Boolean.TYPE
                                    || param == Character.TYPE);
                }

                try {
                    methods[i].invoke(testMethod, new Object[] { new Long(1) });
                    assertTrue("invalid invoke with Long: " + methods[i],
                            param == Long.TYPE || param == Float.TYPE
                                    || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Long invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Long invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Integer.TYPE
                                    || param == Boolean.TYPE
                                    || param == Character.TYPE);
                }

                try {
                    methods[i].invoke(testMethod, new Object[] { new Character(
                            'a') });
                    assertTrue("invalid invoke with Character: " + methods[i],
                            param == Character.TYPE || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Float.TYPE
                                    || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Character invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Character invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Boolean.TYPE);
                }

                try {
                    methods[i]
                            .invoke(testMethod, new Object[] { new Float(1) });
                    assertTrue("invalid invoke with Float: " + methods[i],
                            param == Float.TYPE || param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Float invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Float invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Boolean.TYPE
                                    || param == Character.TYPE);
                }

                try {
                    methods[i].invoke(testMethod,
                            new Object[] { new Double(1) });
                    assertTrue("invalid invoke with Double: " + methods[i],
                            param == Double.TYPE);
                } catch (Exception e) {
                    assertTrue("Double invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Double invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Boolean.TYPE
                                    || param == Character.TYPE
                                    || param == Float.TYPE);
                }

                try {
                    methods[i].invoke(testMethod, new Object[] { new Boolean(
                            true) });
                    assertTrue("invalid invoke with Boolean: " + methods[i],
                            param == Boolean.TYPE);
                } catch (Exception e) {
                    assertTrue("Boolean invalid exception: " + e,
                            e instanceof IllegalArgumentException);
                    assertTrue("Boolean invalid failure: " + methods[i],
                            param == Byte.TYPE || param == Short.TYPE
                                    || param == Integer.TYPE
                                    || param == Long.TYPE
                                    || param == Character.TYPE
                                    || param == Float.TYPE
                                    || param == Double.TYPE);
                }
            }
        }
    }

    /**
     * java.lang.reflect.Method#toString()
     */
    public void test_toString() {
        // Test for method java.lang.String java.lang.reflect.Method.toString()
        Method mth = null;
        Class[] parms = { int.class, short.class, String.class, boolean.class,
                Object.class, long.class, byte.class, char.class, double.class,
                float.class };
        try {

            mth = TestMethod.class.getDeclaredMethod("printTest", parms);
        } catch (Exception e) {
            fail("Exception during toString test : " + e.getMessage());
        }

        assertTrue(
                "Returned incorrect string for method: " + mth.toString(),
                mth
                        .toString()
                        .equals(
                                "public static final void org.apache.harmony.tests.java.lang.reflect.MethodTest$TestMethod.printTest(int,short,java.lang.String,boolean,java.lang.Object,long,byte,char,double,float)"));
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
    }
}
