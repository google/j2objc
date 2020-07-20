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
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Container;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Repeated;
import libcore.java.lang.reflect.annotations.multipleannotation.MultipleAnnotation;
import libcore.java.lang.reflect.annotations.multipleannotationexplicitsingle.MultipleAnnotationExplicitSingle;
import libcore.java.lang.reflect.annotations.multipleannotationoddity.MultipleAnnotationOddity;
import libcore.java.lang.reflect.annotations.noannotation.NoAnnotation;
import libcore.java.lang.reflect.annotations.singleannotation.SingleAnnotation;

import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.EXPECT_EMPTY;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.assertGetDeclaredAnnotation;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.assertIsAnnotationPresent;

public class PackageTest extends TestCase {

    // Tests for isAnnotationPresent and getDeclaredAnnotation.
    public void testDeclaredAnnotation() throws Exception {
        Class<Repeated> repeated = Repeated.class;
        checkDeclaredAnnotation(NoAnnotation.class, repeated, null);
        checkDeclaredAnnotation(SingleAnnotation.class, repeated, "@Repeated(1)");
        checkDeclaredAnnotation(MultipleAnnotation.class, repeated, null);
        checkDeclaredAnnotation(MultipleAnnotationExplicitSingle.class, repeated, null);
        checkDeclaredAnnotation(MultipleAnnotationOddity.class, repeated, "@Repeated(1)");

        Class<Container> container = Container.class;
        checkDeclaredAnnotation(NoAnnotation.class, container, null);
        checkDeclaredAnnotation(SingleAnnotation.class, container, null);
        checkDeclaredAnnotation(MultipleAnnotation.class, container,
                "@Container({@Repeated(1), @Repeated(2)})");
        checkDeclaredAnnotation(MultipleAnnotationExplicitSingle.class, container,
                "@Container({@Repeated(1)})");
        checkDeclaredAnnotation(MultipleAnnotationOddity.class, container,
                "@Container({@Repeated(2), @Repeated(3)})");
    }

    private static void checkDeclaredAnnotation(
            Class<?> classInPackage, Class<? extends Annotation> annotationType,
            String expectedAnnotationString) throws Exception {

        Package aPackage = classInPackage.getPackage();
        // isAnnotationPresent
        assertIsAnnotationPresent(aPackage, annotationType, expectedAnnotationString != null);

        // getDeclaredAnnotation
        assertGetDeclaredAnnotation(aPackage, annotationType, expectedAnnotationString);
    }

    public void testGetDeclaredAnnotationsByType() throws Exception {
        Class<Repeated> repeated = Repeated.class;

        assertGetDeclaredAnnotationsByType(NoAnnotation.class, repeated, EXPECT_EMPTY);
        assertGetDeclaredAnnotationsByType(SingleAnnotation.class, repeated,
                "@Repeated(1)");
        assertGetDeclaredAnnotationsByType(MultipleAnnotation.class, repeated,
                "@Repeated(1)", "@Repeated(2)");
        assertGetDeclaredAnnotationsByType(MultipleAnnotationExplicitSingle.class, repeated,
                "@Repeated(1)");
        assertGetDeclaredAnnotationsByType(MultipleAnnotationOddity.class, repeated,
                "@Repeated(1)", "@Repeated(2)", "@Repeated(3)");

        Class<Container> container = Container.class;
        assertGetDeclaredAnnotationsByType(NoAnnotation.class, container, EXPECT_EMPTY);
        assertGetDeclaredAnnotationsByType(SingleAnnotation.class, container, EXPECT_EMPTY);
        assertGetDeclaredAnnotationsByType(MultipleAnnotation.class, container,
                "@Container({@Repeated(1), @Repeated(2)})");
        assertGetDeclaredAnnotationsByType(MultipleAnnotationExplicitSingle.class, container,
                "@Container({@Repeated(1)})");
        assertGetDeclaredAnnotationsByType(MultipleAnnotationOddity.class, container,
                "@Container({@Repeated(2), @Repeated(3)})");
    }

    private static void assertGetDeclaredAnnotationsByType(
            Class<?> classInPackage, Class<? extends Annotation> annotationType,
            String... expectedAnnotationStrings) throws Exception {
        Package aPackage = classInPackage.getPackage();
        AnnotatedElementTestSupport.assertGetDeclaredAnnotationsByType(
                aPackage, annotationType, expectedAnnotationStrings);
    }

    public void testGetAnnotationsByType() throws Exception {
        Class<Repeated> repeated = Repeated.class;
        assertGetAnnotationsByType(NoAnnotation.class, repeated, EXPECT_EMPTY);
        assertGetAnnotationsByType(SingleAnnotation.class, repeated, "@Repeated(1)");
        assertGetAnnotationsByType(MultipleAnnotation.class, repeated,
                "@Repeated(1)", "@Repeated(2)");
        assertGetAnnotationsByType(MultipleAnnotationExplicitSingle.class, repeated,
                "@Repeated(1)");
        assertGetAnnotationsByType(MultipleAnnotationOddity.class, repeated,
                "@Repeated(1)", "@Repeated(2)", "@Repeated(3)");

        Class<Container> container = Container.class;
        assertGetAnnotationsByType(NoAnnotation.class, container, EXPECT_EMPTY);
        assertGetAnnotationsByType(SingleAnnotation.class, container, EXPECT_EMPTY);
        assertGetAnnotationsByType(MultipleAnnotation.class, container,
                "@Container({@Repeated(1), @Repeated(2)})");
        assertGetAnnotationsByType(MultipleAnnotationExplicitSingle.class, container,
                "@Container({@Repeated(1)})");
        assertGetAnnotationsByType(MultipleAnnotationOddity.class, container,
                "@Container({@Repeated(2), @Repeated(3)})");
    }

    private static void assertGetAnnotationsByType(Class<?> classInPackage,
            Class<? extends Annotation> annotationType,
            String... expectedAnnotationStrings) throws Exception {
        Package aPackage = classInPackage.getPackage();
        AnnotatedElementTestSupport.assertGetAnnotationsByType(
                aPackage, annotationType, expectedAnnotationStrings);
    }
}
