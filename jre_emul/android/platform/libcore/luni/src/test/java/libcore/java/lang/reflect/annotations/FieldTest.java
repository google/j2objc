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
import java.lang.reflect.Field;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationA;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationD;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Container;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Repeated;

import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.EXPECT_EMPTY;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.checkAnnotatedElementPresentMethods;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.assertGetDeclaredAnnotation;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.assertIsAnnotationPresent;

public class FieldTest extends TestCase {

    private static class Type {
        @AnnotationA
        @AnnotationD
        public String field;
    }

    public void testFieldAnnotations() throws Exception {
        Field field = Type.class.getField("field");
        checkAnnotatedElementPresentMethods(field, AnnotationA.class, AnnotationD.class);
    }

    private static class AnnotatedClass {
        @Repeated(1)
        private Object singleAnnotation;

        @Repeated(1)
        @Repeated(2)
        private Object multipleAnnotation;

        @Container({@Repeated(1)})
        private Object multipleAnnotationExplicitSingle;

        @Repeated(1)
        @Container({@Repeated(2), @Repeated(3)})
        private Object multipleAnnotationOddity;

        private Object noAnnotation;
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
            Class<?> c, String fieldName, Class<? extends Annotation> annotationType,
            String expectedAnnotationString) throws Exception {
        Field field = c.getDeclaredField(fieldName);

        // isAnnotationPresent
        assertIsAnnotationPresent(field, annotationType, expectedAnnotationString != null);

        // getDeclaredAnnotation
        assertGetDeclaredAnnotation(field, annotationType, expectedAnnotationString);
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
            Class<?> c, Class<? extends Annotation> annotationType, String fieldName,
            String... expectedAnnotationStrings) throws Exception {
        Field field = c.getDeclaredField(fieldName);
        AnnotatedElementTestSupport.assertGetDeclaredAnnotationsByType(
                field, annotationType, expectedAnnotationStrings);
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
            String fieldName, String... expectedAnnotationStrings) throws Exception {
        Field field = c.getDeclaredField(fieldName);
        AnnotatedElementTestSupport.assertGetAnnotationsByType(
                field, annotationType, expectedAnnotationStrings);
    }
}
