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
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Set;

public class ConstructorTest extends junit.framework.TestCase {


    @Retention(RetentionPolicy.RUNTIME)
    @Target( {ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    static @interface ConstructorTestAnnotationRuntime0 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target( {ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    static @interface ConstructorTestAnnotationRuntime1 {
    }

    @Retention(RetentionPolicy.CLASS)
    @Target( {ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    static @interface ConstructorTestAnnotationClass0 {
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target( {ElementType.CONSTRUCTOR, ElementType.PARAMETER})
    static @interface ConstructorTestAnnotationSource0 {
    }

    static class ConstructorTestHelper extends Object {
        int cval;

        @ConstructorTestAnnotationRuntime0
        @ConstructorTestAnnotationRuntime1
        @ConstructorTestAnnotationClass0
        @ConstructorTestAnnotationSource0
        public ConstructorTestHelper() throws IndexOutOfBoundsException {
            cval = 99;
        }

        public ConstructorTestHelper(
                @ConstructorTestAnnotationRuntime0
                @ConstructorTestAnnotationRuntime1
                @ConstructorTestAnnotationClass0
                @ConstructorTestAnnotationSource0 Object x) {
        }

        public ConstructorTestHelper(String... x) {
        }

        private ConstructorTestHelper(int a) {
        }

        protected ConstructorTestHelper(long a) {
        }

        public int check() {
            return cval;
        }
    }

    static class GenericConstructorTestHelper<T, S extends T, E extends Exception> {
        public GenericConstructorTestHelper(T t, S s) {}
        public GenericConstructorTestHelper() throws E{}
    }

    static class NoPublicConstructorTestHelper {
        // This class has no public constructor.
    }

//    Used to test synthetic constructor.
//
//    static class Outer {
//        private Outer(){}
//        class Inner {
//            {new Outer();}
//        }
//    }

    public void test_getParameterAnnotations() throws Exception {
        Constructor<ConstructorTestHelper> ctor1 = ConstructorTestHelper.class
                .getConstructor(Object.class);
        Annotation[][] paramAnnotations = ctor1.getParameterAnnotations();
        assertEquals("Annotations for wrong number of parameters returned", 1,
                paramAnnotations.length);
        assertEquals("Wrong number of annotations returned", 2,
                paramAnnotations[0].length);

        Set<Class<?>> ignoreOrder = new HashSet<Class<?>>();
        ignoreOrder.add(paramAnnotations[0][0].annotationType());
        ignoreOrder.add(paramAnnotations[0][1].annotationType());

        assertTrue("Missing ConstructorTestAnnotationRuntime0", ignoreOrder
                .contains(ConstructorTestAnnotationRuntime0.class));
        assertTrue("Missing ConstructorTestAnnotationRuntime1", ignoreOrder
                .contains(ConstructorTestAnnotationRuntime1.class));
    }


    public void test_getDeclaredAnnotations() throws Exception {
        Constructor<ConstructorTestHelper> ctor1 = null;
        ctor1 = ConstructorTestHelper.class.getConstructor(new Class[0]);
        Annotation[] annotations = ctor1.getDeclaredAnnotations();
        assertEquals("Wrong number of annotations returned", 2,
                annotations.length);
        Set<Class<?>> ignoreOrder = new HashSet<Class<?>>();
        ignoreOrder.add(annotations[0].annotationType());
        ignoreOrder.add(annotations[1].annotationType());

        assertTrue("Missing ConstructorTestAnnotationRuntime0", ignoreOrder
                .contains(ConstructorTestAnnotationRuntime0.class));
        assertTrue("Missing ConstructorTestAnnotationRuntime1", ignoreOrder
                .contains(ConstructorTestAnnotationRuntime1.class));
    }

    public void test_isVarArgs() throws Exception {
        Constructor<ConstructorTestHelper> varArgCtor = ConstructorTestHelper.class
                .getConstructor(String[].class);
        assertTrue("Vararg constructor not recognized", varArgCtor.isVarArgs());

        Constructor<ConstructorTestHelper> nonVarArgCtor = ConstructorTestHelper.class
                .getConstructor(Object.class);
        assertFalse("Non vararg constructor recognized as vararg constructor",
                nonVarArgCtor.isVarArgs());
    }

    public void test_hashCode() throws Exception {
        Constructor<ConstructorTestHelper> constructor = ConstructorTestHelper.class
                .getConstructor();
        assertEquals(
                "The constructor's hashCode is not equal to the hashCode of the name of the declaring class",
                ConstructorTestHelper.class.getName().hashCode(), constructor
                        .hashCode());
    }

    @SuppressWarnings("unchecked")
    public void test_toGenericString() throws Exception {
        Constructor<GenericConstructorTestHelper> genericCtor = GenericConstructorTestHelper.class
                .getConstructor(Object.class, Object.class);
        assertEquals(
                "Wrong generic string returned",
                "public org.apache.harmony.tests.java.lang.reflect.ConstructorTest$GenericConstructorTestHelper(T,S)",
                genericCtor.toGenericString());
        Constructor<GenericConstructorTestHelper> ctor = GenericConstructorTestHelper.class
                .getConstructor();
        assertEquals(
                "Wrong generic string returned",
                "public org.apache.harmony.tests.java.lang.reflect.ConstructorTest$GenericConstructorTestHelper() throws E",
                ctor.toGenericString());
    }

    public void test_equalsLjava_lang_Object() {
        Constructor<ConstructorTestHelper> ctor1 = null, ctor2 = null;
        try {
            ctor1 = ConstructorTestHelper.class.getConstructor(
                    new Class[0]);
            ctor2 = ConstructorTestHelper.class.getConstructor(Object.class);
        } catch (Exception e) {
            fail("Exception during equals test : " + e.getMessage());
        }
        assertTrue("Different Contructors returned equal", !ctor1.equals(ctor2));
    }

    public void test_getDeclaringClass() {
        boolean val = false;
        try {
            Class<? extends ConstructorTestHelper> pclass = new ConstructorTestHelper().getClass();
            Constructor<? extends ConstructorTestHelper> ctor = pclass.getConstructor(new Class[0]);
            val = ctor.getDeclaringClass().equals(pclass);
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
        assertTrue("Returned incorrect declaring class", val);
    }

    public void test_getExceptionTypes() {
        // Test for method java.lang.Class []
        // java.lang.reflect.Constructor.getExceptionTypes()
        Class[] exceptions = null;
        Class<? extends IndexOutOfBoundsException> ex = null;
        try {
            Constructor<? extends ConstructorTestHelper> ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(new Class[0]);
            exceptions = ctor.getExceptionTypes();
            ex = new IndexOutOfBoundsException().getClass();
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
        assertEquals("Returned exception list of incorrect length",
                1, exceptions.length);
        assertTrue("Returned incorrect exception", exceptions[0].equals(ex));
    }

    public void test_getModifiers() {
        // Test for method int java.lang.reflect.Constructor.getModifiers()
        int mod = 0;
        try {
            Constructor<? extends ConstructorTestHelper> ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(new Class[0]);
            mod = ctor.getModifiers();
            assertTrue("Returned incorrect modifers for public ctor",
                    ((mod & Modifier.PUBLIC) == Modifier.PUBLIC)
                            && ((mod & Modifier.PRIVATE) == 0));
        } catch (NoSuchMethodException e) {
            fail("Exception during test : " + e.getMessage());
        }
        try {
            Class[] cl = { int.class };
            Constructor<? extends ConstructorTestHelper> ctor = new ConstructorTestHelper().getClass()
                    .getDeclaredConstructor(cl);
            mod = ctor.getModifiers();
            assertTrue("Returned incorrect modifers for private ctor",
                    ((mod & Modifier.PRIVATE) == Modifier.PRIVATE)
                            && ((mod & Modifier.PUBLIC) == 0));
        } catch (NoSuchMethodException e) {
            fail("Exception during test : " + e.getMessage());
        }
        try {
            Class[] cl = { long.class };
            Constructor<? extends ConstructorTestHelper> ctor = new ConstructorTestHelper().getClass()
                    .getDeclaredConstructor(cl);
            mod = ctor.getModifiers();
            assertTrue("Returned incorrect modifers for private ctor",
                    ((mod & Modifier.PROTECTED) == Modifier.PROTECTED)
                            && ((mod & Modifier.PUBLIC) == 0));
        } catch (NoSuchMethodException e) {
            fail("NoSuchMethodException during test : " + e.getMessage());
        }
    }

    public void test_getName() {
        // Test for method java.lang.String
        // java.lang.reflect.Constructor.getName()
        try {
            Constructor<? extends ConstructorTestHelper> ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(new Class[0]);
            assertTrue(
                    "Returned incorrect name: " + ctor.getName(),
                    ctor
                            .getName()
                            .equals(
                                    "org.apache.harmony.tests.java.lang.reflect.ConstructorTest$ConstructorTestHelper"));
        } catch (Exception e) {
            fail("Exception obtaining contructor : " + e.getMessage());
        }
    }

    public void test_getParameterTypes() {
        // Test for method java.lang.Class []
        // java.lang.reflect.Constructor.getParameterTypes()
        Class[] types = null;
        try {
            Constructor<? extends ConstructorTestHelper> ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(new Class[0]);
            types = ctor.getParameterTypes();
        } catch (Exception e) {
            fail("Exception during getParameterTypes test:"
                    + e.toString());
        }
        assertEquals("Incorrect parameter returned", 0, types.length);

        Class[] parms = null;
        try {
            parms = new Class[1];
            parms[0] = new Object().getClass();
            Constructor<? extends ConstructorTestHelper> ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(parms);
            types = ctor.getParameterTypes();
        } catch (Exception e) {
            fail("Exception during getParameterTypes test:"
                    + e.toString());
        }
        assertTrue("Incorrect parameter returned", types[0].equals(parms[0]));
    }

    @SuppressWarnings("unchecked")
    public void test_getGenericParameterTypes() {
        Type[] types = null;
        try {
            Constructor<? extends ConstructorTestHelper> ctor = new ConstructorTestHelper()
                    .getClass().getConstructor(new Class[0]);
            types = ctor.getGenericParameterTypes();
        } catch (Exception e) {
            fail("Exception during getParameterTypes test:" + e.toString());
        }
        assertEquals("Incorrect parameter returned", 0, types.length);

        Class<?>[] parms = null;
        try {
            parms = new Class[] {Object.class};
            Constructor<? extends ConstructorTestHelper> ctor = new ConstructorTestHelper()
                    .getClass().getConstructor(parms);
            types = ctor.getGenericParameterTypes();
        } catch (Exception e) {
            fail("Exception during getParameterTypes test:" + e.toString());
        }
        assertTrue("Incorrect parameter returned", types[0].equals(parms[0]));


        try {
            Constructor<GenericConstructorTestHelper> constructor = GenericConstructorTestHelper.class
                    .getConstructor(Object.class, Object.class);
            types = constructor.getGenericParameterTypes();
        } catch (Exception e) {
            fail("Exception during getParameterTypes test:" + e.toString());
        }

        assertEquals("Wrong number of parameter types returned", 2,
                types.length);

        assertEquals("Wrong number of parameter types returned", "T",
                ((TypeVariable)types[0]).getName());
        assertEquals("Wrong number of parameter types returned", "S",
                ((TypeVariable)types[1]).getName());
    }

    @SuppressWarnings("unchecked")
    public void test_getGenericExceptionTypes() {
        Type[] types = null;

        try {
            Constructor<? extends ConstructorTestHelper> ctor = new ConstructorTestHelper()
                    .getClass().getConstructor(new Class[0]);
            types = ctor.getGenericExceptionTypes();
        } catch (Exception e) {
            fail("Exception during getGenericExceptionTypes test:" + e.toString());
        }
        assertEquals("Wrong number of exception types returned", 1, types.length);


        try {
            Constructor<GenericConstructorTestHelper> constructor = GenericConstructorTestHelper.class
                    .getConstructor();
            types = constructor.getGenericExceptionTypes();
        } catch (Exception e) {
            fail("Exception during getGenericExceptionTypes test:"
                    + e.toString());
        }

        assertEquals("Wrong number of exception types returned", 1,
                types.length);

        assertEquals("Wrong exception name returned.", "E",
                ((TypeVariable)types[0]).getName());

    }


    public void test_newInstance$Ljava_lang_Object() {
        // Test for method java.lang.Object
        // java.lang.reflect.Constructor.newInstance(java.lang.Object [])

        ConstructorTestHelper test = null;
        try {
            Constructor<? extends ConstructorTestHelper> ctor = new ConstructorTestHelper().getClass()
                    .getConstructor(new Class[0]);
            test = ctor.newInstance((Object[])null);
        } catch (Exception e) {
            fail("Failed to create instance : " + e.getMessage());
        }
        assertEquals("improper instance created", 99, test.check());
    }

    public void test_toString() {
        // Test for method java.lang.String
        // java.lang.reflect.Constructor.toString()
        Class[] parms = null;
        Constructor<? extends ConstructorTestHelper> ctor = null;
        try {
            parms = new Class[1];
            parms[0] = new Object().getClass();
            ctor = new ConstructorTestHelper().getClass().getConstructor(parms);
        } catch (Exception e) {
            fail("Exception during getParameterTypes test:"
                    + e.toString());
        }
        assertTrue(
                "Returned incorrect string representation: " + ctor.toString(),
                ctor
                        .toString()
                        .equals(
                                "public org.apache.harmony.tests.java.lang.reflect.ConstructorTest$ConstructorTestHelper(java.lang.Object)"));
    }

    public void test_getConstructor() throws Exception {
        // Passing new Class[0] should be equivalent to (Class[]) null.
        Class<ConstructorTestHelper> c2 = ConstructorTestHelper.class;
        assertEquals(c2.getConstructor(new Class[0]), c2.getConstructor((Class[]) null));
        assertEquals(c2.getDeclaredConstructor(new Class[0]),
                     c2.getDeclaredConstructor((Class[]) null));

        // We can get a non-public constructor via getDeclaredConstructor...
        Class<NoPublicConstructorTestHelper> c1 = NoPublicConstructorTestHelper.class;
        c1.getDeclaredConstructor((Class[]) null);
        // ...but not with getConstructor (which only returns public constructors).
        try {
            c1.getConstructor((Class[]) null);
            fail("Should throw NoSuchMethodException");
        } catch (NoSuchMethodException ex) {
            // Expected.
        }
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
