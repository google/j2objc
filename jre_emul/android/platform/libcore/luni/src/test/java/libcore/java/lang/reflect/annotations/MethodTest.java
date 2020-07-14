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
import java.lang.reflect.Method;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationB;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationC;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Container;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Repeated;

import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.EXPECT_EMPTY;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.checkAnnotatedElementPresentMethods;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.assertGetDeclaredAnnotation;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.assertIsAnnotationPresent;

public class MethodTest extends TestCase {

    private static class Type {
        @AnnotationB
        @AnnotationC
        public void method(String parameter1, String parameter2) {}
    }

    public void testMethodAnnotations() throws Exception {
        Method method = Type.class.getMethod("method", String.class, String.class);
        checkAnnotatedElementPresentMethods(method, AnnotationB.class, AnnotationC.class);
    }

    private static class AnnotatedClass {
        @Repeated(1)
        public void singleAnnotation() {}

        @Repeated(1)
        @Repeated(2)
        public void multipleAnnotation() {}

        @Container({@Repeated(1)})
        public void multipleAnnotationExplicitSingle() {}

        @Repeated(1)
        @Container({@Repeated(2), @Repeated(3)})
        public void multipleAnnotationOddity() {}

        public void noAnnotation() {}
    }

    // Tests for isAnnotationPresent and getDeclaredAnnotation.
    public void testDeclaredAnnotation() throws Exception {
        Class<?> c = AnnotatedClass.class;

        Class<? extends Annotation> repeated = Repeated.class;
        checkDeclaredAnnotation(c, "noAnnotation", repeated, null);
        checkDeclaredAnnotation(c, "multipleAnnotationOddity", repeated, "@Repeated(1)");
        checkDeclaredAnnotation(c, "multipleAnnotationExplicitSingle", repeated, null);
        checkDeclaredAnnotation(c, "multipleAnnotation", repeated, null);
        checkDeclaredAnnotation(c, "singleAnnotation", repeated, "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        checkDeclaredAnnotation(c, "noAnnotation", container, null);
        checkDeclaredAnnotation(c, "multipleAnnotationOddity", container,
                "@Container({@Repeated(2), @Repeated(3)})");
        checkDeclaredAnnotation(c, "multipleAnnotationExplicitSingle", container,
                "@Container({@Repeated(1)})");
        checkDeclaredAnnotation(c, "multipleAnnotation", container,
                "@Container({@Repeated(1), @Repeated(2)})");
        checkDeclaredAnnotation(c, "singleAnnotation", container, null);
    }

    private static void checkDeclaredAnnotation(
            Class<?> c, String methodName, Class<? extends Annotation> annotationType,
            String expectedAnnotationString) throws Exception {
        Method method = c.getDeclaredMethod(methodName);

        // isAnnotationPresent
        assertIsAnnotationPresent(method, annotationType, expectedAnnotationString != null);

        // getDeclaredAnnotation
        assertGetDeclaredAnnotation(method, annotationType, expectedAnnotationString);
    }

    public void testGetDeclaredAnnotationsByType() throws Exception {
        Class<?> c = AnnotatedClass.class;

        Class<? extends Annotation> repeated = Repeated.class;
        assertGetDeclaredAnnotationsByType(c, repeated, "noAnnotation", EXPECT_EMPTY);
        assertGetDeclaredAnnotationsByType(c, repeated, "multipleAnnotationOddity",
                "@Repeated(1)", "@Repeated(2)", "@Repeated(3)");
        assertGetDeclaredAnnotationsByType(c, repeated, "multipleAnnotationExplicitSingle",
                "@Repeated(1)");
        assertGetDeclaredAnnotationsByType(c, repeated, "multipleAnnotation",
                "@Repeated(1)", "@Repeated(2)");
        assertGetDeclaredAnnotationsByType(c, repeated, "singleAnnotation", "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        assertGetDeclaredAnnotationsByType(c, container, "noAnnotation", EXPECT_EMPTY);
        assertGetDeclaredAnnotationsByType(c, container, "multipleAnnotationOddity",
                "@Container({@Repeated(2), @Repeated(3)})");
        assertGetDeclaredAnnotationsByType(c, container, "multipleAnnotationExplicitSingle",
                "@Container({@Repeated(1)})");
        assertGetDeclaredAnnotationsByType(c, container, "multipleAnnotation",
                "@Container({@Repeated(1), @Repeated(2)})");
        assertGetDeclaredAnnotationsByType(c, container, "singleAnnotation", EXPECT_EMPTY);
    }

    private static void assertGetDeclaredAnnotationsByType(
            Class<?> c, Class<? extends Annotation> annotationType, String methodName,
            String... expectedAnnotationStrings) throws Exception {
        Method method = c.getDeclaredMethod(methodName);
        AnnotatedElementTestSupport.assertGetDeclaredAnnotationsByType(
                method, annotationType, expectedAnnotationStrings);
    }

    public void testGetAnnotationsByType() throws Exception {
        Class<?> c = AnnotatedClass.class;

        Class<? extends Annotation> repeated = Repeated.class;
        assertGetAnnotationsByType(c, repeated, "noAnnotation", EXPECT_EMPTY);
        assertGetAnnotationsByType(c, repeated, "multipleAnnotationOddity",
                "@Repeated(1)", "@Repeated(2)", "@Repeated(3)");
        assertGetAnnotationsByType(c, repeated, "multipleAnnotationExplicitSingle",
                "@Repeated(1)");
        assertGetAnnotationsByType(c, repeated, "multipleAnnotation",
                "@Repeated(1)", "@Repeated(2)");
        assertGetAnnotationsByType(c, repeated, "singleAnnotation", "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        assertGetAnnotationsByType(c, container, "noAnnotation", EXPECT_EMPTY);
        assertGetAnnotationsByType(c, container, "multipleAnnotationOddity",
                "@Container({@Repeated(2), @Repeated(3)})");
        assertGetAnnotationsByType(c, container, "multipleAnnotationExplicitSingle",
                "@Container({@Repeated(1)})");
        assertGetAnnotationsByType(c, container, "multipleAnnotation",
                "@Container({@Repeated(1), @Repeated(2)})");
        assertGetAnnotationsByType(c, container, "singleAnnotation", EXPECT_EMPTY);
    }

    private static void assertGetAnnotationsByType(
            Class<?> c, Class<? extends Annotation> annotationType,
            String methodName, String... expectedAnnotationStrings) throws Exception {
        Method method = c.getDeclaredMethod(methodName);
        AnnotatedElementTestSupport.assertGetAnnotationsByType(
                method, annotationType, expectedAnnotationStrings);
    }
}
