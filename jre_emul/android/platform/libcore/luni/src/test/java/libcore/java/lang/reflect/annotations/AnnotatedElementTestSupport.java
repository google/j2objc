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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Utility methods and annotation definitions for use when testing implementations of
 * AnnotatedElement.
 *
 * <p>For compactness, the repeated annotation methods that take strings use a format based on Java
 * syntax rather than the toString() of annotations. For example, "@Repeated(1)" rather than
 * "@libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Repeated(value=1)". Use
 * {@link #EXPECT_EMPTY} to indicate "no annotationed expected".
 */
public class AnnotatedElementTestSupport {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationA {}

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationB {}

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationC {}

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationD {}

    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD,
            ElementType.PARAMETER, ElementType.PACKAGE })
    public @interface Container {
        Repeated[] value();
    }

    @Repeatable(Container.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD,
            ElementType.PARAMETER, ElementType.PACKAGE })
    public @interface Repeated {
        int value();
    }

    /**
     * A named constant that can be used with assert methods below that take
     * "String[] expectedAnnotationStrings" as their final argument to indicate "none".
     */
    public static final String[] EXPECT_EMPTY = new String[0];

    private AnnotatedElementTestSupport() {
    }

    /**
     * Test the {@link AnnotatedElement} methods associated with "presence". i.e. methods that
     * deal with annotations being "present" (i.e. "direct" or "inherited" annotations, not
     * "indirect").
     *
     * <p>Asserts that calling {@link AnnotatedElement#getAnnotations()} on the supplied element
     * returns annotations of the supplied expected classes.
     *
     * <p>Where the expected classes contains some subset from
     * {@link AnnotationA}, {@link AnnotationB}, {@link AnnotationC}, {@link AnnotationD} this
     * method also asserts that {@link AnnotatedElement#isAnnotationPresent(Class)} and
     * {@link AnnotatedElement#getAnnotation(Class)} works as expected.
     *
     * <p>This method also confirms that {@link AnnotatedElement#isAnnotationPresent(Class)} and
     * {@link AnnotatedElement#getAnnotation(Class)} work correctly with a {@code null} argument.
     */
    static void checkAnnotatedElementPresentMethods(
            AnnotatedElement element, Class<? extends Annotation>... expectedAnnotations) {
        Set<Class<? extends Annotation>> actualTypes = annotationsToTypes(element.getAnnotations());
        Set<Class<? extends Annotation>> expectedTypes = set(expectedAnnotations);
        assertEquals(expectedTypes, actualTypes);

        // getAnnotations() should be consistent with isAnnotationPresent() and getAnnotation()
        assertPresent(expectedTypes.contains(AnnotationA.class), element, AnnotationA.class);
        assertPresent(expectedTypes.contains(AnnotationB.class), element, AnnotationB.class);
        assertPresent(expectedTypes.contains(AnnotationC.class), element, AnnotationC.class);
        assertPresent(expectedTypes.contains(AnnotationD.class), element, AnnotationD.class);

        try {
            element.isAnnotationPresent(null);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            element.getAnnotation(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * Test the {@link AnnotatedElement} methods associated with "direct" annotations.
     *
     * <p>Asserts that calling {@link AnnotatedElement#getDeclaredAnnotations()} on the supplied
     * element returns annotations of the supplied expected classes.
     *
     * <p>Where the expected classes contains some subset from
     * {@link AnnotationA}, {@link AnnotationB} and {@link AnnotationC}, this method also asserts
     * that {@link AnnotatedElement#getDeclaredAnnotation(Class)} works as expected.
     *
     * <p>This method also confirms that {@link AnnotatedElement#isAnnotationPresent(Class)} and
     * {@link AnnotatedElement#getAnnotation(Class)} work correctly with a {@code null} argument.
     */
    static void checkAnnotatedElementDirectMethods(
            AnnotatedElement element,
            Class<? extends Annotation>... expectedDeclaredAnnotations) {
        Set<Class<? extends Annotation>> actualTypes = annotationsToTypes(element.getDeclaredAnnotations());
        Set<Class<? extends Annotation>> expectedTypes = set(expectedDeclaredAnnotations);
        assertEquals(expectedTypes, actualTypes);

        assertDeclared(expectedTypes.contains(AnnotationA.class), element, AnnotationA.class);
        assertDeclared(expectedTypes.contains(AnnotationB.class), element, AnnotationB.class);
        assertDeclared(expectedTypes.contains(AnnotationC.class), element, AnnotationC.class);

        try {
            element.getDeclaredAnnotation(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * Extracts the annotation types ({@link Annotation#annotationType()} from the supplied
     * annotations.
     */
    static Set<Class<? extends Annotation>> annotationsToTypes(Annotation[] annotations) {
        Set<Class<? extends Annotation>> result = new HashSet<Class<? extends Annotation>>();
        for (Annotation annotation : annotations) {
            result.add(annotation.annotationType());
        }
        return result;
    }

    private static void assertPresent(boolean present, AnnotatedElement element,
            Class<? extends Annotation> annotation) {
        if (present) {
            assertNotNull(element.getAnnotation(annotation));
            assertTrue(element.isAnnotationPresent(annotation));
        } else {
            assertNull(element.getAnnotation(annotation));
            assertFalse(element.isAnnotationPresent(annotation));
        }
    }

    private static void assertDeclared(boolean present, AnnotatedElement element,
            Class<? extends Annotation> annotation) {
        if (present) {
            assertNotNull(element.getDeclaredAnnotation(annotation));
        } else {
            assertNull(element.getDeclaredAnnotation(annotation));
        }
    }

    @SafeVarargs
    static <T> Set<T> set(T... instances) {
        return new HashSet<>(Arrays.asList(instances));
    }

    /**
     * Asserts that {@link AnnotatedElement#isAnnotationPresent(Class)} returns the expected result.
     */
    static void assertIsAnnotationPresent(
            AnnotatedElement element, Class<? extends Annotation> annotationType,
            boolean expected) {
        assertEquals("element.isAnnotationPresent() for " + element + " and " + annotationType,
                expected, element.isAnnotationPresent(annotationType));
    }

    /**
     * Asserts that {@link AnnotatedElement#getDeclaredAnnotation(Class)} returns the expected
     * result. The result is specified using a String. See {@link AnnotatedElementTestSupport} for
     * the string syntax.
     */
    static void assertGetDeclaredAnnotation(AnnotatedElement annotatedElement,
            Class<? extends Annotation> annotationType, String expectedAnnotationString) {
        Annotation annotation = annotatedElement.getDeclaredAnnotation(annotationType);
        assertAnnotationMatches(annotation, expectedAnnotationString);
    }

    /**
     * Asserts that {@link AnnotatedElement#getDeclaredAnnotationsByType(Class)} returns the
     * expected result. The result is specified using a String. See
     * {@link AnnotatedElementTestSupport} for the string syntax.
     */
    static void assertGetDeclaredAnnotationsByType(
            AnnotatedElement annotatedElement, Class<? extends Annotation> annotationType,
            String[] expectedAnnotationStrings) {
        Annotation[] annotations = annotatedElement.getDeclaredAnnotationsByType(annotationType);
        assertAnnotationsMatch(annotations, expectedAnnotationStrings);
    }

    /**
     * Asserts that {@link AnnotatedElement#getAnnotationsByType(Class)} returns the
     * expected result. The result is specified using a String. See
     * {@link AnnotatedElementTestSupport} for the string syntax.
     */
    static void assertGetAnnotationsByType(AnnotatedElement annotatedElement,
            Class<? extends Annotation> annotationType, String[] expectedAnnotationStrings)
            throws Exception {
        Annotation[] annotations = annotatedElement.getAnnotationsByType(annotationType);
        assertAnnotationsMatch(annotations, expectedAnnotationStrings);
    }

    private static void assertAnnotationMatches(
            Annotation annotation, String expectedAnnotationString) {
        if (expectedAnnotationString == null) {
            assertNull(annotation);
        } else {
            assertNotNull(annotation);
            assertEquals(expectedAnnotationString, createAnnotationTestString(annotation));
        }
    }

    /**
     * Asserts that the supplied annotations match the expectation Strings. See
     * {@link AnnotatedElementTestSupport} for the string syntax.
     */
    static void assertAnnotationsMatch(Annotation[] annotations,
            String[] expectedAnnotationStrings) {

        // Due to Android's dex format insisting that Annotations are sorted by name the ordering of
        // annotations is determined by the (simple?) name of the Annotation, not just the order
        // that they are defined in the source. Tests have to be sensitive to that when handling
        // mixed usage of "Container" and "Repeated" - the "Container" annotations will be
        // discovered before "Repeated" due to their sort ordering.
        //
        // This code assumes that repeated annotations with the same name will be specified in the
        // source their natural sort order when attributes are considered, just to make the testing
        // simpler.
        // e.g. @Repeated(1) @Repeated(2), never @Repeated(2) @Repeated(1)

        // Sorting the expected and actual strings _should_ work providing the assumptions above
        // hold. It may mask random ordering issues but it's harder to deal with that while the
        // source ordering is no observed. Providing no developers are ascribing meaning to the
        // relative order of annotations things should be ok.
        Arrays.sort(expectedAnnotationStrings);

        String[] actualAnnotationStrings = createAnnotationTestStrings(annotations);
        Arrays.sort(actualAnnotationStrings);

        assertEquals(
                Arrays.asList(expectedAnnotationStrings),
                Arrays.asList(actualAnnotationStrings));
    }

    private static String[] createAnnotationTestStrings(Annotation[] annotations) {
        String[] annotationStrings = new String[annotations.length];
        for (int i = 0; i < annotations.length; i++) {
            annotationStrings[i] = createAnnotationTestString(annotations[i]);
        }
        return annotationStrings;
    }

    private static String createAnnotationTestString(Annotation annotation) {
        return "@" + annotation.annotationType().getSimpleName()
                + createArgumentsTestString(annotation);
    }

    private static String createArgumentsTestString(Annotation annotation) {
        if (annotation instanceof Repeated) {
            Repeated repeated = (Repeated) annotation;
            return "(" + repeated.value() + ")";
        } else if (annotation instanceof Container) {
            Container container = (Container) annotation;
            String[] repeatedValues = createAnnotationTestStrings(container.value());
            StringJoiner joiner = new StringJoiner(", ", "{", "}");
            for (String repeatedValue : repeatedValues) {
                joiner.add(repeatedValue);
            }
            String repeatedValuesString = joiner.toString();
            return "(" +  repeatedValuesString + ")";
        }
        return "";
    }
}
