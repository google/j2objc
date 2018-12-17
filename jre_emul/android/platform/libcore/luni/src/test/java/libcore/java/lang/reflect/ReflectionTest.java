/*
 * Copyright (C) 2010 The Android Open Source Project
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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import junit.framework.TestCase;

public final class ReflectionTest extends TestCase {
    String classA = "libcore.java.lang.reflect.ReflectionTest$A";
    String classB = "libcore.java.lang.reflect.ReflectionTest$B";
    String classC = "libcore.java.lang.reflect.ReflectionTest$C";

    public void testClassGetSuperclass() {
        assertEquals(AbstractList.class, ArrayList.class.getSuperclass());
        assertEquals(AbstractCollection.class, AbstractList.class.getSuperclass());
        assertEquals(AbstractCollection.class, AbstractList.class.getSuperclass());
        assertEquals(Object.class, AbstractCollection.class.getSuperclass());
        assertNull(Object.class.getSuperclass());
    }

    public void testPrimitiveGetSuperclass() {
        assertNull(boolean.class.getSuperclass());
        assertNull(int.class.getSuperclass());
        assertNull(double.class.getSuperclass());
        assertNull(void.class.getSuperclass());
    }

    public void testInterfaceGetSuperclass() {
        assertNull(Comparable.class.getSuperclass());
        assertNull(DefinesMember.class.getSuperclass());
        assertNull(ExtendsDefinesMember.class.getSuperclass());
    }

    /**
     * http://code.google.com/p/android/issues/detail?id=6636
     */
    public void testGenericSuperclassToString() throws Exception {
        assertEquals("java.util.ArrayList<" + classA + ">",
                AList.class.getGenericSuperclass().toString());
    }

    public void testClassGetName() {
        assertEquals("int", int.class.getName());
        assertEquals("[I", int[].class.getName());
        assertEquals("java.lang.String", String.class.getName());
        assertEquals("[Ljava.lang.String;", String[].class.getName());
        assertEquals("libcore.java.lang.reflect.ReflectionTest", getClass().getName());
        assertEquals(getClass().getName() + "$A", A.class.getName());
        assertEquals(getClass().getName() + "$B", B.class.getName());
        assertEquals(getClass().getName() + "$DefinesMember", DefinesMember.class.getName());
    }

    public void testClassGetCanonicalName() {
        assertEquals("int", int.class.getCanonicalName());
        assertEquals("int[]", int[].class.getCanonicalName());
        assertEquals("java.lang.String", String.class.getCanonicalName());
        assertEquals("java.lang.String[]", String[].class.getCanonicalName());
        assertEquals("libcore.java.lang.reflect.ReflectionTest", getClass().getCanonicalName());
        assertEquals(getClass().getName() + ".A", A.class.getCanonicalName());
        assertEquals(getClass().getName() + ".B", B.class.getCanonicalName());
        assertEquals(getClass().getName() + ".DefinesMember",
                DefinesMember.class.getCanonicalName());
    }

    // TODO(tball): enable and fix.
//    public void testFieldToString() throws Exception {
//        Field fieldOne = C.class.getDeclaredField("fieldOne");
//        String fieldOneRaw = "public static " + classA + " " + classC + ".fieldOne";
//        assertEquals(fieldOneRaw, fieldOne.toString());
//        assertEquals(fieldOneRaw, fieldOne.toGenericString());
//
//        Field fieldTwo = C.class.getDeclaredField("fieldTwo");
//        assertEquals("private transient volatile java.util.Map " + classC + ".fieldTwo",
//                fieldTwo.toString());
//        assertEquals("private transient volatile java.util.Map<" + classA + ", java.lang.String> "
//                + classC + ".fieldTwo", fieldTwo.toGenericString());
//
//        Field fieldThree = C.class.getDeclaredField("fieldThree");
//        String fieldThreeRaw = "protected java.lang.Object[] " + classC + ".fieldThree";
//        assertEquals(fieldThreeRaw, fieldThree.toString());
//        String fieldThreeGeneric = "protected K[] " + classC + ".fieldThree";
//        assertEquals(fieldThreeGeneric, fieldThree.toGenericString());
//
//        Field fieldFour = C.class.getDeclaredField("fieldFour");
//        String fieldFourRaw = "java.util.Map " + classC + ".fieldFour";
//        assertEquals(fieldFourRaw, fieldFour.toString());
//        String fieldFourGeneric = "java.util.Map<? super java.lang.Integer, java.lang.Integer[]> "
//                + classC + ".fieldFour";
//        assertEquals(fieldFourGeneric, fieldFour.toGenericString());
//
//        Field fieldFive = C.class.getDeclaredField("fieldFive");
//        String fieldFiveRaw = "java.lang.String[][][][][] " + classC + ".fieldFive";
//        assertEquals(fieldFiveRaw, fieldFive.toString());
//        assertEquals(fieldFiveRaw, fieldFive.toGenericString());
//    }

    // TODO(tball): enable and fix.
//    public void testConstructorToString() throws Exception {
//        Constructor constructorOne = C.class.getDeclaredConstructor(A.class);
//        String constructorOneRaw = classC + "(" + classA + ") throws " + classB;
//        assertEquals(constructorOneRaw, constructorOne.toString());
//        assertEquals(constructorOneRaw, constructorOne.toGenericString());
//
//        Constructor constructorTwo = C.class.getDeclaredConstructor(Map.class, Object.class);
//        String constructorTwoRaw = "protected " + classC + "(java.util.Map,java.lang.Object)";
//        assertEquals(constructorTwoRaw, constructorTwo.toString());
//        String constructorTwoGeneric = "protected <T1> " + classC
//                + "(java.util.Map<? super " + classA + ", T1>,K)";
//        assertEquals(constructorTwoGeneric, constructorTwo.toGenericString());
//    }

    public void testMethodToString() throws Exception {
        Method methodOne = C.class.getDeclaredMethod("methodOne", A.class, C.class);
        String methodOneRaw = "protected final synchronized " + classA + " "
                + classC + ".methodOne(" + classA + "," + classC + ") throws " + classB;
        assertEquals(methodOneRaw, methodOne.toString());
        assertEquals(methodOneRaw, methodOne.toGenericString());

        Method methodTwo = C.class.getDeclaredMethod("methodTwo", List.class);
        String methodTwoRaw = "public abstract java.util.Map "
                + classC + ".methodTwo(java.util.List)";
        assertEquals(methodTwoRaw, methodTwo.toString());
        String methodTwoGeneric = "public abstract java.util.Map<" + classA + ", java.lang.String> "
                + classC + ".methodTwo(java.util.List<" + classA + ">)";
        assertEquals(methodTwoGeneric, methodTwo.toGenericString());

        Method methodThree = C.class.getDeclaredMethod("methodThree", A.class, Set.class);
        String methodThreeRaw = "private static java.util.Map "
                + classC + ".methodThree(" + classA + ",java.util.Set)";
        assertEquals(methodThreeRaw, methodThree.toString());
        String methodThreeGeneric = "private static <T1,T2> java.util.Map<T1, ?> "
                + classC + ".methodThree(T1,java.util.Set<? super T2>)";
        assertEquals(methodThreeGeneric, methodThree.toGenericString());

        Method methodFour = C.class.getDeclaredMethod("methodFour", Set.class);
        String methodFourRaw = "public java.lang.Comparable " + classC + ".methodFour(java.util.Set)";
        assertEquals(methodFourRaw, methodFour.toString());
        String methodFourGeneric = "public <T> T " + classC + ".methodFour(java.util.Set<T>)";
        assertEquals(methodFourGeneric, methodFour.toGenericString());
    }

    public void testTypeVariableWithMultipleBounds() throws Exception {
        TypeVariable t = C.class.getDeclaredMethod("methodFour", Set.class).getTypeParameters()[0];
        assertEquals("T", t.toString());

        Type[] bounds = t.getBounds();
        ParameterizedType comparableT = (ParameterizedType) bounds[0];
        assertEquals(Comparable.class, comparableT.getRawType());
        assertEquals("T", ((TypeVariable) comparableT.getActualTypeArguments()[0]).getName());
        assertEquals(3, bounds.length);
        assertEquals(Serializable.class, bounds[1]);
        assertEquals(RandomAccess.class, bounds[2]);
    }

    public void testGetFieldNotFound() throws Exception {
        try {
            D.class.getField("noField");
            fail();
        } catch (NoSuchFieldException expected) {
        }
    }

    public void testGetDeclaredFieldNotFound() throws Exception {
        try {
            D.class.getDeclaredField("noField");
            fail();
        } catch (NoSuchFieldException expected) {
        }
    }

    public void testGetFieldNull() throws Exception {
        try {
            D.class.getField(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testGetDeclaredFieldNull() throws Exception {
        try {
            D.class.getDeclaredField(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testGetFieldIsRecursive() throws Exception {
        Field field = D.class.getField("fieldOne");
        assertEquals(C.class, field.getDeclaringClass());
    }

    public void testGetDeclaredFieldIsNotRecursive() {
        try {
            D.class.getDeclaredField("fieldOne");
            fail();
        } catch (NoSuchFieldException expected) {
        }
    }

    public void testGetFieldIsPublicOnly() throws Exception {
        C.class.getField("fieldOne"); // public
        try {
            C.class.getField("fieldTwo"); // private
            fail();
        } catch (NoSuchFieldException expected) {
        }
        try {
            C.class.getField("fieldThree"); // protected
            fail();
        } catch (NoSuchFieldException expected) {
        }
        try {
            C.class.getField("fieldFour"); // package-private
            fail();
        } catch (NoSuchFieldException expected) {
        }
    }

    public void testGetDeclaredFieldIsAllVisibilities() throws Exception {
        C.class.getDeclaredField("fieldOne"); // public
        C.class.getDeclaredField("fieldTwo"); // private
        C.class.getDeclaredField("fieldThree"); // protected
        C.class.getDeclaredField("fieldFour"); // package-private
    }

    public void testGetFieldViaExtendsThenImplements() throws Exception {
        Field field = ExtendsImplementsDefinesMember.class.getField("field");
        assertEquals(DefinesMember.class, field.getDeclaringClass());
    }

    public void testGetFieldViaImplementsThenExtends() throws Exception {
        Field field = ImplementsExtendsDefinesMember.class.getField("field");
        assertEquals(DefinesMember.class, field.getDeclaringClass());
    }

    public void testGetFieldsViaExtendsThenImplements() throws Exception {
        Field[] fields = ExtendsImplementsDefinesMember.class.getFields();
        assertTrue(names(fields).contains("field"));
    }

    public void testGetFieldsViaImplementsThenExtends() throws Exception {
        Field[] fields = ImplementsExtendsDefinesMember.class.getFields();
        assertTrue(names(fields).contains("field"));
    }

    public void testGetMethodViaExtendsThenImplements() throws Exception {
        Method method = ExtendsImplementsDefinesMember.class.getMethod("method");
        assertEquals(DefinesMember.class, method.getDeclaringClass());
    }

    public void testGetMethodViaImplementsThenExtends() throws Exception {
        Method method = ImplementsExtendsDefinesMember.class.getMethod("method");
        assertEquals(DefinesMember.class, method.getDeclaringClass());
    }

    public void testGetMethodsViaExtendsThenImplements() throws Exception {
        Method[] methods = ExtendsImplementsDefinesMember.class.getMethods();
        assertTrue(names(methods).contains("method"));
    }

    public void testGetMethodsViaImplementsThenExtends() throws Exception {
        Method[] methods = ImplementsExtendsDefinesMember.class.getMethods();
        assertTrue(names(methods).contains("method"));
    }

    public void testGetMethodsContainsNoDuplicates() throws Exception {
        Method[] methods = ExtendsAndImplementsDefinesMember.class.getMethods();
        assertEquals(1, count(names(methods), "method"));
    }

    public void testGetFieldsContainsNoDuplicates() throws Exception {
        Field[] fields = ExtendsAndImplementsDefinesMember.class.getFields();
        assertEquals(1, count(names(fields), "field"));
    }

    public void testIsLocalClass() {
        A methodLevelAnonymous = new A() {};
        class Local {}
        class $Local$1 {}
        assertFalse(ReflectionTest.class.isLocalClass());
        assertFalse(A.class.isLocalClass());
        assertFalse($Dollar$1.class.isLocalClass());
        assertFalse(CLASS_LEVEL_ANONYMOUS.getClass().isLocalClass());
        assertFalse(methodLevelAnonymous.getClass().isLocalClass());
        assertTrue(Local.class.isLocalClass());
        assertTrue($Local$1.class.isLocalClass());
        assertFalse(int.class.isLocalClass());
        assertFalse(Object.class.isLocalClass());
    }

    public void testIsAnonymousClass() {
        A methodLevelAnonymous = new A() {};
        class Local {}
        class $Local$1 {}
        assertFalse(ReflectionTest.class.isAnonymousClass());
        assertFalse(A.class.isAnonymousClass());
        assertFalse($Dollar$1.class.isAnonymousClass());
        assertTrue(CLASS_LEVEL_ANONYMOUS.getClass().isAnonymousClass());
        assertTrue(methodLevelAnonymous.getClass().isAnonymousClass());
        assertFalse(Local.class.isAnonymousClass());
        assertFalse($Local$1.class.isAnonymousClass());
        assertFalse(int.class.isAnonymousClass());
        assertFalse(Object.class.isAnonymousClass());
    }

    /**
     * Class.isEnum() erroneously returned true for indirect descendants of
     * Enum. http://b/1062200.
     */
    public void testClassIsEnum() {
        Class<?> trafficClass = TrafficLights.class;
        Class<?> redClass = TrafficLights.RED.getClass();
        Class<?> yellowClass = TrafficLights.YELLOW.getClass();
        Class<?> greenClass = TrafficLights.GREEN.getClass();
        assertSame(trafficClass, redClass);
        assertNotSame(trafficClass, yellowClass);
        assertNotSame(trafficClass, greenClass);
        assertNotSame(yellowClass, greenClass);
        assertTrue(trafficClass.isEnum());
        assertTrue(redClass.isEnum());
        assertFalse(yellowClass.isEnum());
        assertFalse(greenClass.isEnum());
        assertNotNull(trafficClass.getEnumConstants());
        assertNull(yellowClass.getEnumConstants());
        assertNull(greenClass.getEnumConstants());
    }

    static class $Dollar$1 {}
    static class A {}
    static class AList extends ArrayList<A> {}
    static A CLASS_LEVEL_ANONYMOUS = new A() {};

    static class B extends Exception {}

    public static abstract class C<K> {
        public static A fieldOne;
        private transient volatile Map<A, String> fieldTwo;
        protected K[] fieldThree;
        Map<? super Integer, Integer[]> fieldFour;
        String[][][][][] fieldFive;

        C(A a) throws B {}
        protected <T1 extends A> C(Map<? super A, T1> a, K s) {}

        protected final synchronized A methodOne(A parameterOne, C parameterTwo) throws B {
            return null;
        }
        public abstract Map<A, String> methodTwo(List<A> onlyParameter);
        @Deprecated /** this annotation is used because it has runtime retention */
        private static <T1 extends A, T2> Map<T1, ?> methodThree(T1 t, Set<? super T2> t2s) {
            return null;
        }
        public <T extends Comparable<T> & Serializable & RandomAccess> T methodFour(Set<T> t) {
            return null;
        }
    }

    public static class D extends C<String> {
        public D(A a) throws B {
            super(a);
        }
        @Override public Map<A, String> methodTwo(List<A> onlyParameter) {
            return null;
        }
    }

    interface DefinesMember {
        String field = "s";
        void method();
    }
    static abstract class ImplementsDefinesMember implements DefinesMember {}
    static abstract class ExtendsImplementsDefinesMember extends ImplementsDefinesMember {}
    interface ExtendsDefinesMember extends DefinesMember {}
    static abstract class ImplementsExtendsDefinesMember implements ExtendsDefinesMember {}
    static abstract class ExtendsAndImplementsDefinesMember extends ImplementsDefinesMember
            implements DefinesMember {}

    private List<String> names(Member[] methods) {
        List<String> result = new ArrayList<String>();
        for (Member method : methods) {
            result.add(method.getName());
        }
        return result;
    }

    private int count(List<?> list, Object element) {
        int result = 0;
        for (Object o : list) {
            if (o.equals(element)) {
                result++;
            }
        }
        return result;
    }

    enum TrafficLights {
        RED,
        YELLOW {},
        GREEN {
            @SuppressWarnings("unused")
            int i;
            @SuppressWarnings("unused")
            void foobar() {}
        }
    }

    public void testGetEnclosingClass() {
        assertNull(ReflectionTest.class.getEnclosingClass());
        assertEquals(ReflectionTest.class, Foo.class.getEnclosingClass());
        assertEquals(ReflectionTest.class, HasMemberClassesInterface.class.getEnclosingClass());
        assertEquals(HasMemberClassesInterface.class,
                HasMemberClassesInterface.D.class.getEnclosingClass());
        assertEquals(ReflectionTest.class, Foo.class.getEnclosingClass());
    }

    public void testGetDeclaringClass() {
        assertNull(ReflectionTest.class.getDeclaringClass());
        assertEquals(ReflectionTest.class, Foo.class.getDeclaringClass());
        assertEquals(ReflectionTest.class, HasMemberClassesInterface.class.getDeclaringClass());
        assertEquals(HasMemberClassesInterface.class,
                HasMemberClassesInterface.D.class.getDeclaringClass());
    }

    public void testGetEnclosingClassIsTransitiveForClassesDefinedInAMethod() {
        class C {}
        assertEquals(ReflectionTest.class, C.class.getEnclosingClass());
    }

    public void testGetDeclaringClassIsNotTransitiveForClassesDefinedInAMethod() {
        class C {}
        assertEquals(null, C.class.getDeclaringClass());
    }

    public void testGetEnclosingMethodIsNotTransitive() {
        class C {
            class D {}
        }
        assertEquals(null, C.D.class.getEnclosingMethod());
    }

    private static final Object staticAnonymous = new Object() {};

    public void testStaticFieldAnonymousClass() {
        // The class declared in the <clinit> is enclosed by the <clinit>'s class.
        // http://b/11245138
        assertEquals(ReflectionTest.class, staticAnonymous.getClass().getEnclosingClass());
        // However, because it is anonymous, it has no declaring class.
        // https://code.google.com/p/android/issues/detail?id=61003
        assertNull(staticAnonymous.getClass().getDeclaringClass());
        // Because the class is declared in <clinit> which is not exposed through reflection,
        // it has no enclosing method or constructor.
        assertNull(staticAnonymous.getClass().getEnclosingMethod());
        assertNull(staticAnonymous.getClass().getEnclosingConstructor());
    }

    public void testGetEnclosingMethodOfTopLevelClass() {
        assertNull(ReflectionTest.class.getEnclosingMethod());
    }

    public void testGetEnclosingConstructorOfTopLevelClass() {
        assertNull(ReflectionTest.class.getEnclosingConstructor());
    }

    public void testClassEnclosedByConstructor() throws Exception {
        Foo foo = new Foo("string");
        assertEquals(Foo.class, foo.c.getEnclosingClass());
        assertEquals(Foo.class.getDeclaredConstructor(String.class),
                foo.c.getEnclosingConstructor());
        assertNull(foo.c.getEnclosingMethod());
        assertNull(foo.c.getDeclaringClass());
    }

    public void testClassEnclosedByMethod() throws Exception {
        Foo foo = new Foo();
        foo.foo("string");
        assertEquals(Foo.class, foo.c.getEnclosingClass());
        assertNull(foo.c.getEnclosingConstructor());
        assertEquals(Foo.class.getDeclaredMethod("foo", String.class),
                foo.c.getEnclosingMethod());
        assertNull(foo.c.getDeclaringClass());
    }

    public void testGetClasses() throws Exception {
        // getClasses() doesn't include classes inherited from interfaces!
        assertSetEquals(HasMemberClasses.class.getClasses(),
                HasMemberClassesSuperclass.B.class, HasMemberClasses.H.class);
    }

    public void testGetDeclaredClasses() throws Exception {
        assertSetEquals(HasMemberClasses.class.getDeclaredClasses(),
                HasMemberClasses.G.class, HasMemberClasses.H.class, HasMemberClasses.I.class,
                HasMemberClasses.J.class, HasMemberClasses.K.class, HasMemberClasses.L.class);
    }

    public void testConstructorGetExceptions() throws Exception {
        assertSetEquals(HasThrows.class.getConstructor().getExceptionTypes(),
                IOException.class, InvocationTargetException.class, IllegalStateException.class);
        assertSetEquals(HasThrows.class.getConstructor(Void.class).getExceptionTypes());
    }

    public void testClassMethodGetExceptions() throws Exception {
        assertSetEquals(HasThrows.class.getMethod("foo").getExceptionTypes(),
                IOException.class, InvocationTargetException.class, IllegalStateException.class);
        assertSetEquals(HasThrows.class.getMethod("foo", Void.class).getExceptionTypes());
    }

    public void testProxyMethodGetExceptions() throws Exception {
        InvocationHandler emptyInvocationHandler = new InvocationHandler() {
            @Override public Object invoke(Object proxy, Method method, Object[] args) {
                return null;
            }
        };

        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[] { ThrowsInterface.class }, emptyInvocationHandler);
        assertSetEquals(proxy.getClass().getMethod("foo").getExceptionTypes(),
                IOException.class, InvocationTargetException.class, IllegalStateException.class);
        assertSetEquals(proxy.getClass().getMethod("foo", Void.class).getExceptionTypes());
    }

    public void testClassModifiers() {
        int modifiers = ReflectionTest.class.getModifiers();
        assertTrue(Modifier.isPublic(modifiers));
        assertFalse(Modifier.isProtected(modifiers));
        assertFalse(Modifier.isPrivate(modifiers));
        assertFalse(Modifier.isAbstract(modifiers));
        assertFalse(Modifier.isStatic(modifiers));
        assertTrue(Modifier.isFinal(modifiers));
        assertFalse(Modifier.isStrict(modifiers));
    }

    public void testInnerClassModifiers() {
        int modifiers = Foo.class.getModifiers();
        assertFalse(Modifier.isPublic(modifiers));
        assertFalse(Modifier.isProtected(modifiers));
        assertTrue(Modifier.isPrivate(modifiers));
        assertFalse(Modifier.isAbstract(modifiers));
        assertTrue(Modifier.isStatic(modifiers));
        assertFalse(Modifier.isFinal(modifiers));
        assertFalse(Modifier.isStrict(modifiers));
    }

    public void testAnonymousClassModifiers() {
        int modifiers = staticAnonymous.getClass().getModifiers();
        assertFalse(Modifier.isPublic(modifiers));
        assertFalse(Modifier.isProtected(modifiers));
        assertFalse(Modifier.isPrivate(modifiers));
        assertFalse(Modifier.isAbstract(modifiers));
        // http://b/62290080
        // OpenJDK 9b08 changed the behavior of Modifier.isStatic(modifiers)
        // to return false, consistent with JLS 15.9.5 ("An anonymous class
        // is never static"). Earlier versions of OpenJDK returned true.
        // This test accepts either behavior.
        Modifier.isStatic(modifiers); // return value is ignored
        // J2ObjC: recent versions of the JDK return false, but some versions return true.
        // This test accepts either behavior.
        Modifier.isFinal(modifiers); // return value is ignored
        assertFalse(Modifier.isStrict(modifiers));
    }

    public void testInnerClassName() {
        assertEquals("ReflectionTest", ReflectionTest.class.getSimpleName());
        assertEquals("Foo", Foo.class.getSimpleName());
        assertEquals("", staticAnonymous.getClass().getSimpleName());
    }

    private static class Foo {
        Class<?> c;
        private Foo() {
        }
        private Foo(String s) {
            c = new Object() {}.getClass();
        }
        private Foo(int i) {
            c = new Object() {}.getClass();
        }
        private void foo(String s) {
            c = new Object() {}.getClass();
        }
        private void foo(int i) {
            c = new Object() {}.getClass();
        }
    }

    static class HasMemberClassesSuperclass {
        class A {}
        public class B {}
        static class C {}
    }

    public interface HasMemberClassesInterface {
        class D {}
        public class E {}
        static class F {}
    }

    public static class HasMemberClasses extends HasMemberClassesSuperclass
            implements HasMemberClassesInterface {
        class G {}
        public class H {}
        static class I {}
        enum J {}
        interface K {}
        @interface L {}
    }

    public static class HasThrows {
        public HasThrows() throws IOException, InvocationTargetException, IllegalStateException {}
        public HasThrows(Void v) {}
        public void foo() throws IOException, InvocationTargetException, IllegalStateException {}
        public void foo(Void v) {}
    }

    public static interface ThrowsInterface {
        void foo() throws IOException, InvocationTargetException, IllegalStateException;
        void foo(Void v);
    }

    private void assertSetEquals(Object[] actual, Object... expected) {
        Set<Object> actualSet = new HashSet<Object>(Arrays.asList(actual));
        Set<Object> expectedSet = new HashSet<Object>(Arrays.asList(expected));
        assertEquals(expectedSet, actualSet);
    }
}
