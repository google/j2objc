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

import java.lang.reflect.Method;

import junit.framework.TestCase;

public final class MethodTest extends TestCase {
    public void test_getExceptionTypes() throws Exception {
        Method method = MethodTestHelper.class.getMethod("m1", new Class[0]);
        Class[] exceptions = method.getExceptionTypes();
        assertEquals(1, exceptions.length);
        assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
        // Check that corrupting our array doesn't affect other callers.
        exceptions[0] = NullPointerException.class;
        exceptions = method.getExceptionTypes();
        assertEquals(1, exceptions.length);
        assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
    }

    public void test_getParameterTypes() throws Exception {
        Class[] expectedParameters = new Class[] { Object.class };
        Method method = MethodTestHelper.class.getMethod("m2", expectedParameters);
        Class[] parameters = method.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
        // Check that corrupting our array doesn't affect other callers.
        parameters[0] = String.class;
        parameters = method.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
    }

    public void testGetMethodWithPrivateMethodAndInterfaceMethod() throws Exception {
        assertEquals(InterfaceA.class, Sub.class.getMethod("a").getDeclaringClass());
    }

    public void testGetMethodReturnsIndirectlyImplementedInterface() throws Exception {
        assertEquals(InterfaceA.class, ImplementsC.class.getMethod("a").getDeclaringClass());
        assertEquals(InterfaceA.class, ExtendsImplementsC.class.getMethod("a").getDeclaringClass());
    }

    public void testGetDeclaredMethodReturnsIndirectlyImplementedInterface() throws Exception {
        try {
            ImplementsC.class.getDeclaredMethod("a").getDeclaringClass();
            fail();
        } catch (NoSuchMethodException expected) {
        }
        try {
            ExtendsImplementsC.class.getDeclaredMethod("a").getDeclaringClass();
            fail();
        } catch (NoSuchMethodException expected) {
        }
    }

    public void testGetMethodWithConstructorName() throws Exception {
        try {
            MethodTestHelper.class.getMethod("<init>");
            fail();
        } catch (NoSuchMethodException expected) {
        }
    }

    public void testGetMethodWithNullName() throws Exception {
        try {
            MethodTestHelper.class.getMethod(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testGetMethodWithNullArgumentsArray() throws Exception {
        Method m1 = MethodTestHelper.class.getMethod("m1", (Class[]) null);
        assertEquals(0, m1.getParameterTypes().length);
    }

    public void testGetMethodWithNullArgument() throws Exception {
        try {
            MethodTestHelper.class.getMethod("m2", new Class[] { null });
            fail();
        } catch (NoSuchMethodException expected) {
        }
    }

    public void testGetMethodReturnsInheritedStaticMethod() throws Exception {
        Method b = Sub.class.getMethod("b");
        assertEquals(void.class, b.getReturnType());
    }

    public void testGetDeclaredMethodReturnsPrivateMethods() throws Exception {
        Method method = Super.class.getDeclaredMethod("a");
        assertEquals(void.class, method.getReturnType());
    }

    public void testGetDeclaredMethodDoesNotReturnSuperclassMethods() throws Exception {
        try {
            Sub.class.getDeclaredMethod("a");
            fail();
        } catch (NoSuchMethodException expected) {
        }
    }

    public void testGetDeclaredMethodDoesNotReturnImplementedInterfaceMethods() throws Exception {
        try {
            InterfaceB.class.getDeclaredMethod("a");
            fail();
        } catch (NoSuchMethodException expected) {
        }
    }

    public void testImplementedInterfaceMethodOfAnonymousClass() throws Exception {
        Object anonymous = new InterfaceA() {
            @Override public void a() {
            }
        };
        Method method = anonymous.getClass().getMethod("a");
        assertEquals(anonymous.getClass(), method.getDeclaringClass());
    }

    public void testPublicMethodOfAnonymousClass() throws Exception {
        Object anonymous = new Object() {
            public void a() {
            }
        };
        Method method = anonymous.getClass().getMethod("a");
        assertEquals(anonymous.getClass(), method.getDeclaringClass());
    }

    public void testGetMethodDoesNotReturnPrivateMethodOfAnonymousClass() throws Exception {
        Object anonymous = new Object() {
            private void a() {
            }
        };
        try {
            anonymous.getClass().getMethod("a");
            fail();
        } catch (NoSuchMethodException expected) {
        }
    }

    public void testGetDeclaredMethodReturnsPrivateMethodOfAnonymousClass() throws Exception {
        Object anonymous = new Object() {
            private void a() {
            }
        };
        Method method = anonymous.getClass().getDeclaredMethod("a");
        assertEquals(anonymous.getClass(), method.getDeclaringClass());
    }

    public void testEqualMethodEqualsAndHashCode() throws Exception {
        Method m1 = MethodTestHelper.class.getMethod("m1");
        Method m2 = MethodTestHelper.class.getMethod("m1");
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertEquals(MethodTestHelper.class.getName().hashCode() ^ "m1".hashCode(), m1.hashCode());
    }

    public void testHashCodeSpec() throws Exception {
        Method m1 = MethodTestHelper.class.getMethod("m1");
        assertEquals(MethodTestHelper.class.getName().hashCode() ^ "m1".hashCode(), m1.hashCode());
    }

    public void testDifferentMethodEqualsAndHashCode() throws Exception {
        Method m1 = MethodTestHelper.class.getMethod("m1");
        Method m2 = MethodTestHelper.class.getMethod("m2", Object.class);
        assertFalse(m1.equals(m2));
        assertFalse(m1.hashCode() == m2.hashCode());
    }

    // iOS: toString returns may be different than in Java.
    // http://b/1045939
//    public void testMethodToString() throws Exception {
//        assertEquals("public final native void java.lang.Object.notify()",
//                Object.class.getMethod("notify", new Class[] { }).toString());
//        assertEquals("public java.lang.String java.lang.Object.toString()",
//                Object.class.getMethod("toString", new Class[] { }).toString());
//        assertEquals("public final native void java.lang.Object.wait(long,int)"
//                + " throws java.lang.InterruptedException",
//                Object.class.getMethod("wait", new Class[] { long.class, int.class }).toString());
//        assertEquals("public boolean java.lang.Object.equals(java.lang.Object)",
//                Object.class.getMethod("equals", new Class[] { Object.class }).toString());
//        assertEquals("public static java.lang.String java.lang.String.valueOf(char[])",
//                String.class.getMethod("valueOf", new Class[] { char[].class }).toString());
//        assertEquals( "public java.lang.Process java.lang.Runtime.exec(java.lang.String[])"
//                + " throws java.io.IOException",
//                Runtime.class.getMethod("exec", new Class[] { String[].class }).toString());
//        // http://b/18488857
//        assertEquals(
//                "public int java.lang.String.compareTo(java.lang.Object)",
//                String.class.getMethod("compareTo", Object.class).toString());
//    }

    // Tests that the "varargs" modifier is handled correctly.
    // The underlying constant value for it is the same as for the "transient" field modifier.
    // http://b/18488857
    /* J2ObjC: Android specific test
    public void testVarargsModifier() throws NoSuchMethodException {
        Method stringFormatMethod = String.class.getMethod(
                "format", new Class[] { String.class, Object[].class });
        assertTrue(stringFormatMethod.isVarArgs());
        assertEquals(
                "public static java.lang.String java.lang.String.format("
                        + "java.lang.String,java.lang.Object[])",
                stringFormatMethod.toString());
    }
    */

    public static class MethodTestHelper {
        public void m1() throws IndexOutOfBoundsException { }
        public void m2(Object o) { }
    }

    public static class Super {
        private void a() {}
        public static void b() {}
    }
    public static interface InterfaceA {
        void a();
    }
    public static abstract class Sub extends Super implements InterfaceA {
    }

    public static interface InterfaceB extends InterfaceA {}
    public static interface InterfaceC extends InterfaceB {}
    public static abstract class ImplementsC implements InterfaceC {}
    public static abstract class ExtendsImplementsC extends ImplementsC {}
}
