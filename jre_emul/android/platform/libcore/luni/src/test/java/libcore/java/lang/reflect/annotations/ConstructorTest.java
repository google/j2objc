/*
 * Copyright (C) 2016 The Android Open Source Project
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

package libcore.java.lang.reflect.annotations;

import junit.framework.TestCase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationA;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationC;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Container;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Repeated;

import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.EXPECT_EMPTY;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.checkAnnotatedElementPresentMethods;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.assertGetDeclaredAnnotation;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.assertIsAnnotationPresent;

public class ConstructorTest extends TestCase {

    private static class Type {
        @AnnotationA
        @AnnotationC
        public Type() {}
    }

    public void testConstructorAnnotations() throws Exception {
        Constructor<Type> constructor = Type.class.getConstructor();
        checkAnnotatedElementPresentMethods(constructor, AnnotationA.class, AnnotationC.class);
    }

    // A class with multiple constructors that differ by their argument count.
    private static class AnnotatedClass {
        @Repeated(1)
        public AnnotatedClass() {}

        @Repeated(1)
        @Repeated(2)
        public AnnotatedClass(int a) {}

        @Container({@Repeated(1)})
        public AnnotatedClass(int a, int b) {}

        @Repeated(1)
        @Container({@Repeated(2), @Repeated(3)})
        public AnnotatedClass(int a, int b, int c) {}

        public AnnotatedClass(int a, int b, int c, int d) {}
    }

    // Tests for isAnnotationPresent and getDeclaredAnnotation.
    public void testDeclaredAnnotation() throws Exception {
        Class<?> c = AnnotatedClass.class;

        Class<? extends Annotation> repeated = Repeated.class;
        checkDeclaredAnnotation(c, 4, repeated, null);
        checkDeclaredAnnotation(c, 3, repeated, "@Repeated(1)");
        checkDeclaredAnnotation(c, 2, repeated, null);
        checkDeclaredAnnotation(c, 1, repeated, null);
        checkDeclaredAnnotation(c, 0, repeated, "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        checkDeclaredAnnotation(c, 4, container, null);
        checkDeclaredAnnotation(c, 3, container, "@Container({@Repeated(2), @Repeated(3)})");
        checkDeclaredAnnotation(c, 2, container, "@Container({@Repeated(1)})");
        checkDeclaredAnnotation(c, 1, container, "@Container({@Repeated(1), @Repeated(2)})");
        checkDeclaredAnnotation(c, 0, container, null);
    }

    private static void checkDeclaredAnnotation(Class<?> c, int constructorArgCount,
            Class<? extends Annotation> annotationType,
            String expectedAnnotationString) throws Exception {
        Constructor constructor = getConstructor(c, constructorArgCount);

        // isAnnotationPresent
        assertIsAnnotationPresent(constructor, annotationType,
                expectedAnnotationString != null);

        // getDeclaredAnnotation
        assertGetDeclaredAnnotation(constructor, annotationType, expectedAnnotationString);
    }

    public void testGetDeclaredAnnotationsByType() throws Exception {
        Class<?> c = AnnotatedClass.class;

        Class<? extends Annotation> repeated = Repeated.class;
        assertGetDeclaredAnnotationsByType(c, 4, repeated, EXPECT_EMPTY);
        assertGetDeclaredAnnotationsByType(c, 3, repeated,
                "@Repeated(1)", "@Repeated(2)", "@Repeated(3)");
        assertGetDeclaredAnnotationsByType(c, 2, repeated, "@Repeated(1)");
        assertGetDeclaredAnnotationsByType(c, 1, repeated, "@Repeated(1)", "@Repeated(2)");
        assertGetDeclaredAnnotationsByType(c, 0, repeated, "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        assertGetDeclaredAnnotationsByType(c, 4, container, EXPECT_EMPTY);
        assertGetDeclaredAnnotationsByType(c, 3, container,
                "@Container({@Repeated(2), @Repeated(3)})");
        assertGetDeclaredAnnotationsByType(c, 2, container, "@Container({@Repeated(1)})");
        assertGetDeclaredAnnotationsByType(c, 1, container,
                "@Container({@Repeated(1), @Repeated(2)})");
        assertGetDeclaredAnnotationsByType(c, 0, container, EXPECT_EMPTY);
    }

    private static void assertGetDeclaredAnnotationsByType(Class<?> c, int constructorArgCount,
            Class<? extends Annotation> annotationType,
            String... expectedAnnotationStrings) throws Exception {
        Constructor<?> constructor = getConstructor(c, constructorArgCount);
        AnnotatedElementTestSupport.assertGetDeclaredAnnotationsByType(
                constructor, annotationType, expectedAnnotationStrings);
    }

    public void testGetAnnotationsByType() throws Exception {
        Class<?> c = AnnotatedClass.class;

        Class<? extends Annotation> repeated = Repeated.class;
        assertGetAnnotationsByType(c, 4, repeated, EXPECT_EMPTY);
        assertGetAnnotationsByType(c, 3, repeated, "@Repeated(1)", "@Repeated(2)", "@Repeated(3)");
        assertGetAnnotationsByType(c, 2, repeated, "@Repeated(1)");
        assertGetAnnotationsByType(c, 1, repeated, "@Repeated(1)", "@Repeated(2)");
        assertGetAnnotationsByType(c, 0, repeated, "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        assertGetAnnotationsByType(c, 4, container, EXPECT_EMPTY);
        assertGetAnnotationsByType(c, 3, container, "@Container({@Repeated(2), @Repeated(3)})");
        assertGetAnnotationsByType(c, 2, container, "@Container({@Repeated(1)})");
        assertGetAnnotationsByType(c, 1, container, "@Container({@Repeated(1), @Repeated(2)})");
        assertGetAnnotationsByType(c, 0, container, EXPECT_EMPTY);
    }

    private static void assertGetAnnotationsByType(Class<?> c, int constructorArgCount,
            Class<? extends Annotation> annotationType,
            String... expectedAnnotationStrings) throws Exception {
        Constructor<?> constructor = getConstructor(c, constructorArgCount);
        AnnotatedElementTestSupport.assertGetAnnotationsByType(
                constructor, annotationType, expectedAnnotationStrings);
    }

    private static Constructor<?> getConstructor(Class<?> c, int constructorArgCount)
            throws NoSuchMethodException {

        Class<?>[] args = new Class[constructorArgCount];
        for (int i = 0; i < constructorArgCount; i++) {
            args[i] = Integer.TYPE;
        }
        return c.getDeclaredConstructor(args);
    }
}
