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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import junit.framework.Assert;
import junit.framework.TestCase;

public final class ReflectionTest extends TestCase {
    String classA = "libcore.java.lang.reflect.ReflectionTest$A";
    String classB = "libcore.java.lang.reflect.ReflectionTest$B";
    String classC = "libcore.java.lang.reflect.ReflectionTest$C";

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

    public void testFieldToString() throws Exception {
        Field fieldOne = C.class.getDeclaredField("fieldOne");
        String fieldOneRaw = "public static " + classA + " " + classC + ".fieldOne";
        assertEquals(fieldOneRaw, fieldOne.toString());
        assertEquals(fieldOneRaw, fieldOne.toGenericString());

        Field fieldTwo = C.class.getDeclaredField("fieldTwo");
        assertEquals("private transient volatile java.util.Map " + classC + ".fieldTwo",
                fieldTwo.toString());
        assertEquals("private transient volatile java.util.Map<" + classA + ", java.lang.String> "
                + classC + ".fieldTwo", fieldTwo.toGenericString());

        Field fieldThree = C.class.getDeclaredField("fieldThree");
        String fieldThreeRaw = "protected java.lang.Object[] " + classC + ".fieldThree";
        assertEquals(fieldThreeRaw, fieldThree.toString());
        String fieldThreeGeneric = "protected K[] " + classC + ".fieldThree";
        assertEquals(fieldThreeGeneric, fieldThree.toGenericString());

        Field fieldFour = C.class.getDeclaredField("fieldFour");
        String fieldFourRaw = "java.util.Map " + classC + ".fieldFour";
        assertEquals(fieldFourRaw, fieldFour.toString());
        String fieldFourGeneric = "java.util.Map<? super java.lang.Integer, java.lang.Integer[]> "
                + classC + ".fieldFour";
        assertEquals(fieldFourGeneric, fieldFour.toGenericString());

        Field fieldFive = C.class.getDeclaredField("fieldFive");
        String fieldFiveRaw = "java.lang.String[][][][][] " + classC + ".fieldFive";
        assertEquals(fieldFiveRaw, fieldFive.toString());
        assertEquals(fieldFiveRaw, fieldFive.toGenericString());
    }

    public void testConstructorToString() throws Exception {
        Constructor constructorOne = C.class.getDeclaredConstructor(A.class);
        String constructorOneRaw = classC + "(" + classA + ") throws " + classB;
        assertEquals(constructorOneRaw, constructorOne.toString());
        assertEquals(constructorOneRaw, constructorOne.toGenericString());

        Constructor constructorTwo = C.class.getDeclaredConstructor(Map.class, Object.class);
        String constructorTwoRaw = "protected " + classC + "(java.util.Map,java.lang.Object)";
        assertEquals(constructorTwoRaw, constructorTwo.toString());
        String constructorTwoGeneric = "protected <T1> " + classC
                + "(java.util.Map<? super " + classA + ", T1>,K)";
        assertEquals(constructorTwoGeneric, constructorTwo.toGenericString());
    }

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

    static class A {}
    static class AList extends ArrayList<A> {}

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
}
