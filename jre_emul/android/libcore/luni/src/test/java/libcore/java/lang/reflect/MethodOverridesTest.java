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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

public class MethodOverridesTest extends TestCase {

    public void testName() throws NoSuchMethodException {
        Method method = StringBuilder.class.getMethod("append", char.class);
        assertEquals("append", method.getName());
    }

    public void testParameterTypes() throws NoSuchMethodException {
        Method method = StringBuilder.class.getMethod("append", char.class);
        assertEquals(Arrays.<Class<?>>asList(char.class),
                Arrays.asList(method.getParameterTypes()));
    }

    public void testDeclaringClass() throws NoSuchMethodException {
        Method method = StringBuilder.class.getMethod("append", char.class);
        assertEquals(StringBuilder.class, method.getDeclaringClass());
    }

    public void testReturnType() throws NoSuchMethodException {
        Method method = StringBuilder.class.getMethod("append", char.class);
        assertEquals(StringBuilder.class, method.getReturnType());
    }

    public void testThrownExceptions() throws NoSuchMethodException {
        Method method = StringBuilder.class.getMethod("append", char.class);
        assertEquals(Collections.<Class<?>>emptyList(), Arrays.asList(method.getExceptionTypes()));
    }

    public void testGetMethodsIncludesInheritedMethods() {
        Set<String> signatures = signatures(Sub.class.getMethods());
        assertContains(signatures, "void notOverridden[] throws []");
    }

    public void testGetDeclaredMethodsDoesNotIncludeInheritedMethods() {
        Set<String> signatures = signatures(Sub.class.getDeclaredMethods());
        assertFalse(signatures.contains("void notOverridden[] throws []"));
    }

    public void testGetDeclaringClassReturnsOverridingClass() throws NoSuchMethodException {
        assertEquals(Sub.class, Sub.class.getMethod("unchanged").getDeclaringClass());
        assertEquals(Sub.class, Sub.class.getDeclaredMethod("unchanged").getDeclaringClass());
    }

    public void testGetMethodsDoesNotIncludeExceptionChanges() throws NoSuchMethodException {
        Set<String> signatures = signatures(Sub.class.getMethods());
        assertContains(signatures, "void thrower[] throws []");
        assertFalse(signatures.contains("void thrower[] throws [java.lang.Exception]"));
        assertEquals(Sub.class, Sub.class.getMethod("thrower").getDeclaringClass());
    }

    public void testGetMethodsIncludesSyntheticMethods() throws NoSuchMethodException {
        Set<String> signatures = signatures(Sub.class.getMethods());
        assertContains(signatures, "java.lang.String returner[] throws []");
        assertContains(signatures, "java.lang.Object returner[] throws []");

        Method method = Sub.class.getMethod("returner");
        assertEquals(Sub.class, method.getDeclaringClass());
        assertFalse(method.isSynthetic());
    }

    public void testGetDeclaredMethodsIncludesSyntheticMethods() throws NoSuchMethodException {
        Set<String> signatures = signatures(Sub.class.getDeclaredMethods());
        assertContains(signatures, "java.lang.String returner[] throws []");
        assertContains(signatures, "java.lang.Object returner[] throws []");

        Method method = Sub.class.getMethod("returner");
        assertEquals(Sub.class, method.getDeclaringClass());
        assertFalse(method.isSynthetic());
    }

    public void testSubclassChangesVisibility() throws NoSuchMethodException {
        Method[] methods = Sub.class.getMethods();
        int count = 0;
        for (Method method : methods) {
            if (signature(method).equals("void visibility[] throws []")) {
                assertEquals(Sub.class, method.getDeclaringClass());
                assertFalse(method.isSynthetic());
                count++;
            }
        }
        assertEquals(1, count);

        Method method = Sub.class.getMethod("visibility");
        assertEquals(Sub.class, method.getDeclaringClass());
        assertFalse(method.isSynthetic());
    }

    public void testMoreVisibleSubclassChangesVisibility() throws NoSuchMethodException {
        Method[] methods = PublicSub.class.getMethods();
        int count = 0;
        for (Method method : methods) {
            if (signature(method).equals("void unchanged[] throws []")) {
                assertEquals(PublicSub.class, method.getDeclaringClass());
                assertTrue(method.isSynthetic());
                count++;
            }
        }
        assertEquals(1, count);

        Method method = PublicSub.class.getMethod("unchanged");
        assertEquals(PublicSub.class, method.getDeclaringClass());
        assertTrue(method.isSynthetic());
    }

    public static class Super {
        public void notOverridden() {}
        public void unchanged() {}
        public void thrower() throws Exception {}
        public Object returner() {
            return null;
        }
        protected void visibility() {}
    }

    public static class Sub extends Super {
        @Override public void unchanged() {}
        @Override public void thrower() {}
        @Override public String returner() {
            return null;
        }
        @Override public void visibility() {}
    }

    static class PackageSuper {
        public void unchanged() {}
    }

    public static class PublicSub extends PackageSuper {}

    /**
     * Returns a method signature of this form:
     * {@code java.lang.String concat[class java.lang.String] throws []}.
     */
    private String signature(Method method) {
        return method.getReturnType().getName() + " " + method.getName()
                + Arrays.toString(method.getParameterTypes())
                + " throws " + Arrays.toString(method.getExceptionTypes());
    }

    private Set<String> signatures(Method[] methods) {
        Set<String> signatures = new HashSet<String>();
        for (Method method : methods) {
            signatures.add(signature(method));
        }
        return signatures;
    }

    private <T> void assertContains(Collection<T> elements, T value) {
        assertTrue("Expected " + value + " in " + elements, elements.contains(value));
    }
}
