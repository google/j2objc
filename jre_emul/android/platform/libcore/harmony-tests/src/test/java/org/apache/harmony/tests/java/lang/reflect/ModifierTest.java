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

import java.lang.reflect.Modifier;

public class ModifierTest extends junit.framework.TestCase {

    private static final int ALL_FLAGS = 0x7FF;

    /**
     * java.lang.reflect.Modifier#Modifier()
     */
    public void test_Constructor() {
        // Test for method java.lang.reflect.Modifier()
        new Modifier();
    }

    /**
     * java.lang.reflect.Modifier#isAbstract(int)
     */
    public void test_isAbstractI() {
        // Test for method boolean java.lang.reflect.Modifier.isAbstract(int)
        assertTrue("ABSTRACT returned false", Modifier.isAbstract(ALL_FLAGS));
        assertTrue("ABSTRACT returned false", Modifier
                .isAbstract(Modifier.ABSTRACT));
        assertTrue("Non-ABSTRACT returned true", !Modifier
                .isAbstract(Modifier.TRANSIENT));
    }

    /**
     * java.lang.reflect.Modifier#isFinal(int)
     */
    public void test_isFinalI() {
        // Test for method boolean java.lang.reflect.Modifier.isFinal(int)
        assertTrue("FINAL returned false", Modifier.isFinal(ALL_FLAGS));
        assertTrue("FINAL returned false", Modifier.isFinal(Modifier.FINAL));
        assertTrue("Non-FINAL returned true", !Modifier
                .isFinal(Modifier.TRANSIENT));
    }

    /**
     * java.lang.reflect.Modifier#isInterface(int)
     */
    public void test_isInterfaceI() {
        // Test for method boolean java.lang.reflect.Modifier.isInterface(int)
        assertTrue("INTERFACE returned false", Modifier.isInterface(ALL_FLAGS));
        assertTrue("INTERFACE returned false", Modifier
                .isInterface(Modifier.INTERFACE));
        assertTrue("Non-INTERFACE returned true", !Modifier
                .isInterface(Modifier.TRANSIENT));
    }

    /**
     * java.lang.reflect.Modifier#isNative(int)
     */
    public void test_isNativeI() {
        // Test for method boolean java.lang.reflect.Modifier.isNative(int)
        assertTrue("NATIVE returned false", Modifier.isNative(ALL_FLAGS));
        assertTrue("NATIVE returned false", Modifier.isNative(Modifier.NATIVE));
        assertTrue("Non-NATIVE returned true", !Modifier
                .isNative(Modifier.TRANSIENT));
    }

    /**
     * java.lang.reflect.Modifier#isPrivate(int)
     */
    public void test_isPrivateI() {
        // Test for method boolean java.lang.reflect.Modifier.isPrivate(int)
        assertTrue("PRIVATE returned false", Modifier.isPrivate(ALL_FLAGS));
        assertTrue("PRIVATE returned false", Modifier
                .isPrivate(Modifier.PRIVATE));
        assertTrue("Non-PRIVATE returned true", !Modifier
                .isPrivate(Modifier.TRANSIENT));
    }

    /**
     * java.lang.reflect.Modifier#isProtected(int)
     */
    public void test_isProtectedI() {
        // Test for method boolean java.lang.reflect.Modifier.isProtected(int)
        assertTrue("PROTECTED returned false", Modifier.isProtected(ALL_FLAGS));
        assertTrue("PROTECTED returned false", Modifier
                .isProtected(Modifier.PROTECTED));
        assertTrue("Non-PROTECTED returned true", !Modifier
                .isProtected(Modifier.TRANSIENT));
    }

    /**
     * java.lang.reflect.Modifier#isPublic(int)
     */
    public void test_isPublicI() {
        // Test for method boolean java.lang.reflect.Modifier.isPublic(int)
        assertTrue("PUBLIC returned false", Modifier.isPublic(ALL_FLAGS));
        assertTrue("PUBLIC returned false", Modifier.isPublic(Modifier.PUBLIC));
        assertTrue("Non-PUBLIC returned true", !Modifier
                .isPublic(Modifier.TRANSIENT));
    }

    /**
     * java.lang.reflect.Modifier#isStatic(int)
     */
    public void test_isStaticI() {
        // Test for method boolean java.lang.reflect.Modifier.isStatic(int)
        assertTrue("STATIC returned false", Modifier.isStatic(ALL_FLAGS));
        assertTrue("STATIC returned false", Modifier.isStatic(Modifier.STATIC));
        assertTrue("Non-STATIC returned true", !Modifier
                .isStatic(Modifier.TRANSIENT));
    }

    /**
     * java.lang.reflect.Modifier#isStrict(int)
     */
    public void test_isStrictI() {
        // Test for method boolean java.lang.reflect.Modifier.isStrict(int)
        assertTrue("STRICT returned false", Modifier.isStrict(Modifier.STRICT));
        assertTrue("Non-STRICT returned true", !Modifier
                .isStrict(Modifier.TRANSIENT));
    }

    /**
     * java.lang.reflect.Modifier#isSynchronized(int)
     */
    public void test_isSynchronizedI() {
        // Test for method boolean
        // java.lang.reflect.Modifier.isSynchronized(int)
        assertTrue("Synchronized returned false", Modifier
                .isSynchronized(ALL_FLAGS));
        assertTrue("Non-Synchronized returned true", !Modifier
                .isSynchronized(Modifier.VOLATILE));
    }

    /**
     * java.lang.reflect.Modifier#isTransient(int)
     */
    public void test_isTransientI() {
        // Test for method boolean java.lang.reflect.Modifier.isTransient(int)
        assertTrue("Transient returned false", Modifier.isTransient(ALL_FLAGS));
        assertTrue("Transient returned false", Modifier
                .isTransient(Modifier.TRANSIENT));
        assertTrue("Non-Transient returned true", !Modifier
                .isTransient(Modifier.VOLATILE));
    }

    /**
     * java.lang.reflect.Modifier#isVolatile(int)
     */
    public void test_isVolatileI() {
        // Test for method boolean java.lang.reflect.Modifier.isVolatile(int)
        assertTrue("Volatile returned false", Modifier.isVolatile(ALL_FLAGS));
        assertTrue("Volatile returned false", Modifier
                .isVolatile(Modifier.VOLATILE));
        assertTrue("Non-Volatile returned true", !Modifier
                .isVolatile(Modifier.TRANSIENT));
    }

    /**
     * java.lang.reflect.Modifier#toString(int)
     */
    public void test_toStringI() {
        // Test for method java.lang.String
        // java.lang.reflect.Modifier.toString(int)
        assertTrue("Returned incorrect string value: "
                + Modifier.toString(java.lang.reflect.Modifier.PUBLIC
                + java.lang.reflect.Modifier.ABSTRACT), Modifier
                .toString(
                        java.lang.reflect.Modifier.PUBLIC
                                + java.lang.reflect.Modifier.ABSTRACT).equals(
                        "public abstract"));

        int i = 0xFFF;
        String modification = "public protected private abstract static final transient "
                + "volatile synchronized native strictfp interface";
        assertTrue("Returned incorrect string value", Modifier.toString(i)
                .equals(modification));
    }

    public void test_Constants_Value() {
        assertEquals(1024, Modifier.ABSTRACT);
        assertEquals(16, Modifier.FINAL);
        assertEquals(512, Modifier.INTERFACE);
        assertEquals(256, Modifier.NATIVE);
        assertEquals(2, Modifier.PRIVATE);
        assertEquals(4, Modifier.PROTECTED);
        assertEquals(1, Modifier.PUBLIC);
        assertEquals(8, Modifier.STATIC);
        assertEquals(2048, Modifier.STRICT);
        assertEquals(32, Modifier.SYNCHRONIZED);
        assertEquals(128, Modifier.TRANSIENT);
        assertEquals(64, Modifier.VOLATILE);
    }

    abstract class AbstractClazz {
    }

    final class FinalClazz {
    }

    static class StaticClazz {
    }

    interface InterfaceClazz {
    }

    public class PublicClazz {
    }

    protected class ProtectedClazz {
    }

    private class PrivateClazz {
    }

    public abstract class PublicAbstractClazz {
    }

    protected abstract class ProtectedAbstractClazz {
    }

    private abstract class PrivateAbstractClazz {
    }

    public final class PublicFinalClazz {
    }

    protected final class ProtectedFinalClazz {
    }

    private final class PrivateFinalClazz {
    }

    public static class PublicStaticClazz {
    }

    protected static class ProtectedStaticClazz {
    }

    private static class PrivateStaticClazz {
    }

    public interface PublicInterface {
    }

    protected interface ProtectedInterface {
    }

    private interface PrivateInterface {
    }

    static abstract class StaticAbstractClazz {
    }

    public static abstract class PublicStaticAbstractClazz {
    }

    protected static abstract class ProtectedStaticAbstractClazz {
    }

    private static abstract class PrivateStaticAbstractClazz {
    }

    static final class StaticFinalClazz {
    }

    public static final class PublicStaticFinalClazz {
    }

    protected static final class ProtectedStaticFinalClazz {
    }

    private static final class PrivateStaticFinalClazz {
    }

    static interface StaticInterface {
    }

    public static interface PublicStaticInterface {
    }

    protected static interface ProtectedStaticInterface {
    }

    private static interface PrivateStaticInterface {
    }

    static abstract interface StaticAbstractInterface {
    }

    public static abstract interface PublicStaticAbstractInterface {
    }

    protected static abstract interface ProtectedStaticAbstractInterface {
    }

    private static abstract interface PrivateStaticAbstractInterface {
    }

    public void test_Class_Modifier() {
        assertEquals(Modifier.ABSTRACT, AbstractClazz.class.getModifiers());
        assertEquals(Modifier.FINAL, FinalClazz.class.getModifiers());
        assertEquals(Modifier.STATIC, StaticClazz.class.getModifiers());
        assertEquals(Modifier.INTERFACE + Modifier.STATIC + Modifier.ABSTRACT,
                InterfaceClazz.class.getModifiers());

        assertEquals(Modifier.PUBLIC, PublicClazz.class.getModifiers());
        assertEquals(Modifier.PROTECTED, ProtectedClazz.class.getModifiers());
        assertEquals(Modifier.PRIVATE, PrivateClazz.class.getModifiers());

        assertEquals(Modifier.PUBLIC + Modifier.ABSTRACT,
                PublicAbstractClazz.class.getModifiers());
        assertEquals(Modifier.PROTECTED + Modifier.ABSTRACT,
                ProtectedAbstractClazz.class.getModifiers());
        assertEquals(Modifier.PRIVATE + Modifier.ABSTRACT,
                PrivateAbstractClazz.class.getModifiers());

        assertEquals(Modifier.PUBLIC + Modifier.FINAL, PublicFinalClazz.class
                .getModifiers());
        assertEquals(Modifier.PROTECTED + Modifier.FINAL,
                ProtectedFinalClazz.class.getModifiers());
        assertEquals(Modifier.PRIVATE + Modifier.FINAL, PrivateFinalClazz.class
                .getModifiers());

        assertEquals(Modifier.PUBLIC + Modifier.STATIC, PublicStaticClazz.class
                .getModifiers());
        assertEquals(Modifier.PROTECTED + Modifier.STATIC,
                ProtectedStaticClazz.class.getModifiers());
        assertEquals(Modifier.PRIVATE + Modifier.STATIC,
                PrivateStaticClazz.class.getModifiers());

        assertEquals(Modifier.PUBLIC + Modifier.INTERFACE + Modifier.STATIC
                + Modifier.ABSTRACT, PublicInterface.class.getModifiers());
        assertEquals(Modifier.STATIC + Modifier.FINAL, StaticFinalClazz.class
                .getModifiers());
        assertEquals(Modifier.PRIVATE + Modifier.INTERFACE + Modifier.STATIC
                + Modifier.ABSTRACT, PrivateInterface.class.getModifiers());

        assertEquals(Modifier.STATIC + Modifier.ABSTRACT,
                StaticAbstractClazz.class.getModifiers());
        assertEquals(Modifier.PUBLIC + Modifier.STATIC + Modifier.ABSTRACT,
                PublicStaticAbstractClazz.class.getModifiers());
        assertEquals(Modifier.PROTECTED + Modifier.STATIC + Modifier.ABSTRACT,
                ProtectedStaticAbstractClazz.class.getModifiers());
        assertEquals(Modifier.PRIVATE + Modifier.STATIC + Modifier.ABSTRACT,
                PrivateStaticAbstractClazz.class.getModifiers());

        assertEquals(Modifier.STATIC + Modifier.FINAL, StaticFinalClazz.class
                .getModifiers());
        assertEquals(Modifier.PUBLIC + Modifier.STATIC + Modifier.FINAL,
                PublicStaticFinalClazz.class.getModifiers());
        assertEquals(Modifier.PROTECTED + Modifier.STATIC + Modifier.FINAL,
                ProtectedStaticFinalClazz.class.getModifiers());
        assertEquals(Modifier.PRIVATE + Modifier.STATIC + Modifier.FINAL,
                PrivateStaticFinalClazz.class.getModifiers());

        assertEquals(Modifier.INTERFACE + Modifier.STATIC + Modifier.ABSTRACT,
                StaticInterface.class.getModifiers());
        assertEquals(Modifier.PUBLIC + Modifier.INTERFACE + Modifier.STATIC
                + Modifier.ABSTRACT, PublicStaticInterface.class.getModifiers());
        assertEquals(Modifier.PROTECTED + Modifier.INTERFACE + Modifier.STATIC
                + Modifier.ABSTRACT, ProtectedStaticInterface.class
                .getModifiers());
        assertEquals(Modifier.PRIVATE + Modifier.INTERFACE + Modifier.STATIC
                + Modifier.ABSTRACT, PrivateStaticInterface.class
                .getModifiers());

        assertEquals(Modifier.INTERFACE + Modifier.STATIC + Modifier.ABSTRACT,
                StaticAbstractInterface.class.getModifiers());
        assertEquals(Modifier.PUBLIC + Modifier.INTERFACE + Modifier.STATIC
                + Modifier.ABSTRACT, PublicStaticAbstractInterface.class
                .getModifiers());
        assertEquals(Modifier.PROTECTED + Modifier.INTERFACE + Modifier.STATIC
                + Modifier.ABSTRACT, ProtectedStaticAbstractInterface.class
                .getModifiers());
        assertEquals(Modifier.PRIVATE + Modifier.INTERFACE + Modifier.STATIC
                + Modifier.ABSTRACT, PrivateStaticAbstractInterface.class
                .getModifiers());
    }

    static abstract class MethodClass {

        public abstract void publicAbstractMethod();

        public static void publicStaticMethod() {
        }

        public final void publicFinalMethod() {
        }

        public static final void publicStaticFinalMethod() {
        }
    }

    public void test_Method_Modifier() throws Exception {
        assertEquals(Modifier.PUBLIC + Modifier.ABSTRACT, MethodClass.class
                .getMethod("publicAbstractMethod", new Class[0]).getModifiers());
        assertEquals(Modifier.PUBLIC + Modifier.STATIC, MethodClass.class
                .getMethod("publicStaticMethod", new Class[0]).getModifiers());

        assertEquals(Modifier.PUBLIC + Modifier.FINAL, MethodClass.class
                .getMethod("publicFinalMethod", new Class[0]).getModifiers());

        assertEquals(Modifier.PUBLIC + Modifier.STATIC + Modifier.FINAL,
                MethodClass.class.getMethod("publicStaticFinalMethod",
                        new Class[0]).getModifiers());
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
