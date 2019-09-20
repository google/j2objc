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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

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
        Class[] expectedParameters = new Class[0];
        Method method = MethodTestHelper.class.getMethod("m1", expectedParameters);
        assertEquals(0, method.getParameterTypes().length);

        expectedParameters = new Class[] { Object.class };
        method = MethodTestHelper.class.getMethod("m2", expectedParameters);
        Class[] parameters = method.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
        // Check that corrupting our array doesn't affect other callers.
        parameters[0] = String.class;
        parameters = method.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
    }

    public void test_getParameterCount() throws Exception {
        Class[] expectedParameters = new Class[0];
        Method method = MethodTestHelper.class.getMethod("m1", expectedParameters);
        assertEquals(0, method.getParameterCount());

        expectedParameters = new Class[] { Object.class };
        method = MethodTestHelper.class.getMethod("m2", expectedParameters);
        int count = method.getParameterCount();
        assertEquals(1, count);
    }

    public void test_getParameters() throws Exception {
        Class[] expectedParameters = new Class[0];
        Method method = MethodTestHelper.class.getMethod("m1", expectedParameters);
        assertEquals(0, method.getParameters().length);

        expectedParameters = new Class[] { Object.class };
        method = MethodTestHelper.class.getMethod("m2", expectedParameters);

        // Test the information available via other Method methods. See ParameterTest and
        // annotations.ParameterTest for more in-depth Parameter testing.
        Parameter[] parameters = method.getParameters();
        assertEquals(1, parameters.length);
        assertEquals(Object.class, parameters[0].getType());

        // Check that corrupting our array doesn't affect other callers.
        parameters[0] = null;
        parameters = method.getParameters();
        assertEquals(1, parameters.length);
        assertEquals(Object.class, parameters[0].getType());
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
    public interface InterfaceA {
        void a();
    }
    public static abstract class Sub extends Super implements InterfaceA {
    }

    public interface InterfaceB extends InterfaceA {}
    public interface InterfaceC extends InterfaceB {}
    public static abstract class ImplementsC implements InterfaceC {}
    public static abstract class ExtendsImplementsC extends ImplementsC {}

    // Static interface method reflection.

    public interface InterfaceWithStatic {
        static String staticMethod() {
            return identifyCaller();
        }
    }

    public void testStaticInterfaceMethod_getMethod() throws Exception {
        Method method = InterfaceWithStatic.class.getMethod("staticMethod");
        assertFalse(method.isDefault());
        assertEquals(Modifier.PUBLIC | Modifier.STATIC, method.getModifiers());
        assertEquals(InterfaceWithStatic.class, method.getDeclaringClass());
    }

    public void testStaticInterfaceMethod_getDeclaredMethod() throws Exception {
        Method declaredMethod = InterfaceWithStatic.class.getDeclaredMethod("staticMethod");
        assertFalse(declaredMethod.isDefault());
        assertEquals(Modifier.PUBLIC | Modifier.STATIC, declaredMethod.getModifiers());
        assertEquals(InterfaceWithStatic.class, declaredMethod.getDeclaringClass());
    }

    public void testStaticInterfaceMethod_invoke() throws Exception {
        String interfaceWithStaticClassName = InterfaceWithStatic.class.getName();
        assertEquals(interfaceWithStaticClassName, InterfaceWithStatic.staticMethod());

        Method method = InterfaceWithStatic.class.getMethod("staticMethod");
        assertEquals(interfaceWithStaticClassName, method.invoke(null));
        assertEquals(interfaceWithStaticClassName, method.invoke(new InterfaceWithStatic() {}));
    }

    public void testStaticInterfaceMethod_setAccessible() throws Exception {
        String interfaceWithStaticClassName = InterfaceWithStatic.class.getName();
        Method method = InterfaceWithStatic.class.getMethod("staticMethod");
        method.setAccessible(false);
        // No effect expected.
        assertEquals(interfaceWithStaticClassName, method.invoke(null));
    }

    // Default method reflection.

    public interface InterfaceWithDefault {
        default String defaultMethod() {
            return identifyCaller();
        }
    }

    public static class ImplementationWithDefault implements InterfaceWithDefault {
    }

    public void testDefaultMethod_getDeclaredMethod_interface() throws Exception {
        Class<InterfaceWithDefault> interfaceWithDefaultClass = InterfaceWithDefault.class;
        Method defaultMethod = interfaceWithDefaultClass.getDeclaredMethod("defaultMethod");
        assertEquals(InterfaceWithDefault.class, defaultMethod.getDeclaringClass());
        assertTrue(defaultMethod.isDefault());
    }

    public void testDefaultMethod_inheritance() throws Exception {
        Class<InterfaceWithDefault> interfaceWithDefaultClass = InterfaceWithDefault.class;
        String interfaceWithDefaultClassName = interfaceWithDefaultClass.getName();
        Method defaultMethod = interfaceWithDefaultClass.getDeclaredMethod("defaultMethod");

        InterfaceWithDefault anon = new InterfaceWithDefault() {};
        Class<?> anonClass = anon.getClass();
        Method inheritedDefaultMethod = anonClass.getMethod("defaultMethod");
        assertEquals(inheritedDefaultMethod, defaultMethod);

        // Check invocation behavior.
        assertEquals(interfaceWithDefaultClassName, defaultMethod.invoke(anon));
        assertEquals(interfaceWithDefaultClassName, inheritedDefaultMethod.invoke(anon));
        assertEquals(interfaceWithDefaultClassName, anon.defaultMethod());

        // Check other method properties.
        assertEquals(InterfaceWithDefault.class, inheritedDefaultMethod.getDeclaringClass());
        assertTrue(inheritedDefaultMethod.isDefault());

        // Confirm the method is not considered declared on the anonymous class.
        assertNull(getDeclaredMethodOrNull(anonClass, "defaultMethod"));
    }

    public void testDefaultMethod_override() throws Exception {
        Class<InterfaceWithDefault> interfaceWithDefaultClass = InterfaceWithDefault.class;
        Method defaultMethod = interfaceWithDefaultClass.getDeclaredMethod("defaultMethod");

        InterfaceWithDefault anon = new InterfaceWithDefault() {
            @Override public String defaultMethod() {
                return identifyCaller();
            }
        };

        Class<? extends InterfaceWithDefault> anonClass = anon.getClass();
        String anonymousClassName = anonClass.getName();

        Method overriddenDefaultMethod = getDeclaredMethodOrNull(anonClass, "defaultMethod");
        assertNotNull(overriddenDefaultMethod);
        assertFalse(overriddenDefaultMethod.equals(defaultMethod));

        // Check invocation behavior.
        assertEquals(anonymousClassName, defaultMethod.invoke(anon));
        assertEquals(anonymousClassName, overriddenDefaultMethod.invoke(anon));
        assertEquals(anonymousClassName, anon.defaultMethod());

        // Check other method properties.
        assertEquals(anonClass, overriddenDefaultMethod.getDeclaringClass());
        assertFalse(overriddenDefaultMethod.isDefault());
    }

    public void testDefaultMethod_setAccessible() throws Exception {
        InterfaceWithDefault anon = new InterfaceWithDefault() {};

        Method defaultMethod = anon.getClass().getMethod("defaultMethod");
        defaultMethod.setAccessible(false);
        // setAccessible(false) should have no effect.
        assertEquals(InterfaceWithDefault.class.getName(), defaultMethod.invoke(anon));

        InterfaceWithDefault anon2 = new InterfaceWithDefault() {
            @Override public String defaultMethod() {
                return identifyCaller();
            }
        };

        Class<? extends InterfaceWithDefault> anon2Class = anon2.getClass();
        Method overriddenDefaultMethod = anon2Class.getDeclaredMethod("defaultMethod");
        overriddenDefaultMethod.setAccessible(false);
        // setAccessible(false) should have no effect.
        assertEquals(anon2Class.getName(), overriddenDefaultMethod.invoke(anon2));
    }

    interface InterfaceWithReAbstractedMethod extends InterfaceWithDefault {
        // Re-abstract a default method.
        @Override String defaultMethod();
    }

    public void testDefaultMethod_reabstracted() throws Exception {
        Class<InterfaceWithReAbstractedMethod> subclass = InterfaceWithReAbstractedMethod.class;

        Method reabstractedDefaultMethod = subclass.getMethod("defaultMethod");
        assertFalse(reabstractedDefaultMethod.isDefault());
        assertEquals(reabstractedDefaultMethod, subclass.getDeclaredMethod("defaultMethod"));
        assertEquals(subclass, reabstractedDefaultMethod.getDeclaringClass());
    }

    public void testDefaultMethod_reimplementedInClass() throws Exception {
        InterfaceWithDefault impl = new InterfaceWithReAbstractedMethod() {
            // Implement a reabstracted default method.
            @Override public String defaultMethod() {
                return identifyCaller();
            }
        };
        Class<?> implClass = impl.getClass();
        String implClassName = implClass.getName();

        Method implClassDefaultMethod = getDeclaredMethodOrNull(implClass, "defaultMethod");
        assertEquals(implClassDefaultMethod, implClass.getMethod("defaultMethod"));

        // Check invocation behavior.
        assertEquals(implClassName, impl.defaultMethod());
        assertEquals(implClassName, implClassDefaultMethod.invoke(impl));

        // Check other method properties.
        assertEquals(implClass, implClassDefaultMethod.getDeclaringClass());
        assertFalse(implClassDefaultMethod.isDefault());
    }

    interface InterfaceWithRedefinedMethods extends InterfaceWithReAbstractedMethod {
        // Reimplement an abstracted default method.
        @Override default String defaultMethod() {
            return identifyCaller();
        }
    }

    public void testDefaultMethod_reimplementInInterface() throws Exception {
        Class<?> interfaceClass = InterfaceWithRedefinedMethods.class;
        String interfaceClassName = interfaceClass.getName();

        // NOTE: The line below defines an anonymous class that implements
        // InterfaceWithReDefinedMethods (and does not need to provide any declarations).
        // See the {}.
        InterfaceWithDefault impl = new InterfaceWithRedefinedMethods() {};
        Class<?> implClass = impl.getClass();

        Method implClassDefaultMethod = implClass.getMethod("defaultMethod");
        assertNull(getDeclaredMethodOrNull(implClass, "defaultMethod"));

        // Check invocation behavior.
        assertEquals(interfaceClassName, impl.defaultMethod());
        assertEquals(interfaceClassName, implClassDefaultMethod.invoke(impl));

        // Check other method properties.
        assertEquals(interfaceClass, implClassDefaultMethod.getDeclaringClass());
        assertTrue(implClassDefaultMethod.isDefault());
    }

    public void testDefaultMethod_invoke() throws Exception {
        InterfaceWithDefault impl1 = new InterfaceWithRedefinedMethods() {};
        InterfaceWithDefault impl2 = new InterfaceWithReAbstractedMethod() {
            @Override public String defaultMethod() {
                return identifyCaller();
            }
        };
        InterfaceWithDefault impl3 = new InterfaceWithDefault() {};

        Class[] classes = {
            InterfaceWithRedefinedMethods.class,
            impl1.getClass(),
            InterfaceWithReAbstractedMethod.class,
            impl2.getClass(),
            InterfaceWithDefault.class,
            impl3.getClass(),
        };
        Object[] instances = { impl1, impl2, impl3 };

        // Attempt to invoke all declarations of defaultMethod() on a selection of instances.
        for (Class<?> clazz : classes) {
            Method method = clazz.getMethod("defaultMethod");
            for (Object instance : instances) {
                if (method.getDeclaringClass().isAssignableFrom(instance.getClass())) {
                    Method trueMethod = instance.getClass().getMethod("defaultMethod");
                    // All implementations of defaultMethod return the class where the method is
                    // declared, enabling us to tell if the correct implementation has been called.
                    Class<?> declaringClass = trueMethod.getDeclaringClass();
                    assertEquals(declaringClass.getName(), method.invoke(instance));
                } else {
                    try {
                        method.invoke(instance);
                        fail();
                    } catch (IllegalArgumentException expected) {
                    }
                }
            }
        }
    }

    interface OtherInterfaceWithDefault {
        default String defaultMethod() {
            return identifyCaller();
        }
    }

    public void testDefaultMethod_superSyntax() throws Exception {
        class ImplementationSuperUser implements InterfaceWithDefault, OtherInterfaceWithDefault {
            @Override public String defaultMethod() {
                return identifyCaller() + ":" +
                        InterfaceWithDefault.super.defaultMethod() + ":" +
                        OtherInterfaceWithDefault.super.defaultMethod();
            }
        }

        String implementationSuperUserClassName = ImplementationSuperUser.class.getName();
        String interfaceWithDefaultClassName = InterfaceWithDefault.class.getName();
        String otherInterfaceWithDefaultClassName = OtherInterfaceWithDefault.class.getName();
        String expectedReturnValue = implementationSuperUserClassName + ":" +
            interfaceWithDefaultClassName + ":" + otherInterfaceWithDefaultClassName;
        ImplementationSuperUser obj = new ImplementationSuperUser();
        assertEquals(expectedReturnValue, obj.defaultMethod());

        Method defaultMethod = ImplementationSuperUser.class.getMethod("defaultMethod");
        assertEquals(expectedReturnValue, defaultMethod.invoke(obj));
    }

    /* J2ObjC: enable and fix.
    public void testProxyWithDefaultMethods() throws Exception {
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                assertSame(InterfaceWithDefault.class, method.getDeclaringClass());
                return identifyCaller();
            }
        };

        InterfaceWithDefault proxyWithDefaultMethod = (InterfaceWithDefault) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] { InterfaceWithDefault.class },
                invocationHandler);
        String invocationHandlerClassName = invocationHandler.getClass().getName();

        // Check the proxy implements the default method.
        Class<? extends InterfaceWithDefault> proxyClass = proxyWithDefaultMethod.getClass();
        Method defaultMethod = proxyClass.getMethod("defaultMethod");
        assertEquals(proxyClass, defaultMethod.getDeclaringClass());
        assertFalse(defaultMethod.isDefault());

        // The default method is intercepted like anything else.
        assertEquals(invocationHandlerClassName, proxyWithDefaultMethod.defaultMethod());
    } */

    private static Method getDeclaredMethodOrNull(Class<?> clazz, String methodName) {
        try {
            Method m = clazz.getDeclaredMethod(methodName);
            assertNotNull(m);
            return m;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Keep this package-protected or public to avoid the introduction of synthetic methods that
     * throw off the offset.
     */
    static String identifyCaller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        int i = 0;
        while (!stack[i++].getMethodName().equals("identifyCaller")) {}
        return stack[i].getClassName();
    }
}
