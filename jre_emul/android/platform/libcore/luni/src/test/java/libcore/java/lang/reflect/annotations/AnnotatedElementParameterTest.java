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
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationB;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationC;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.AnnotationD;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Container;
import libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.Repeated;

import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.EXPECT_EMPTY;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.assertGetDeclaredAnnotation;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.assertIsAnnotationPresent;
import static libcore.java.lang.reflect.annotations.AnnotatedElementTestSupport.checkAnnotatedElementPresentMethods;

/**
 * Tests for the {@link java.lang.reflect.AnnotatedElement} methods from the {@link Parameter}
 * objects obtained from both {@link Constructor} and {@link Method}.
 */
public class AnnotatedElementParameterTest extends TestCase {

    private static class MethodClass {
        public void methodWithoutAnnotatedParameters(String parameter1, String parameter2) {}

        public void methodWithAnnotatedParameters(@AnnotationB @AnnotationD String parameter1,
                @AnnotationC @AnnotationD String parameter2) {}
    }

    public void testMethodParameterAnnotations() throws Exception {
        Class<?> c = MethodClass.class;
        {
            Parameter[] parameters = c.getDeclaredMethod(
                    "methodWithoutAnnotatedParameters", String.class, String.class).getParameters();
            Parameter parameter0 = parameters[0];
            checkAnnotatedElementPresentMethods(parameter0);

            Parameter parameter1 = parameters[1];
            checkAnnotatedElementPresentMethods(parameter1);
        }
        {
            Parameter[] parameters = c.getDeclaredMethod(
                    "methodWithAnnotatedParameters", String.class, String.class).getParameters();

            Parameter parameter0 = parameters[0];
            checkAnnotatedElementPresentMethods(parameter0, AnnotationB.class, AnnotationD.class);

            Parameter parameter1 = parameters[1];
            checkAnnotatedElementPresentMethods(parameter1, AnnotationC.class, AnnotationD.class);
        }
    }

    private static class ConstructorClass {
        // No annotations.
        public ConstructorClass(Integer parameter1, Integer parameter2) {}

        // Annotations.
        public ConstructorClass(@AnnotationB @AnnotationD String parameter1,
                @AnnotationC @AnnotationD String parameter2) {}
    }

    public void testConstructorParameterAnnotations() throws Exception {
        Class<?> c = ConstructorClass.class;
        {
            Parameter[] parameters =
                    c.getDeclaredConstructor(Integer.class, Integer.class).getParameters();
            Parameter parameter0 = parameters[0];
            checkAnnotatedElementPresentMethods(parameter0);

            Parameter parameter1 = parameters[1];
            checkAnnotatedElementPresentMethods(parameter1);
        }
        {
            Parameter[] parameters =
                    c.getDeclaredConstructor(String.class, String.class).getParameters();

            Parameter parameter0 = parameters[0];
            checkAnnotatedElementPresentMethods(parameter0, AnnotationB.class, AnnotationD.class);

            Parameter parameter1 = parameters[1];
            checkAnnotatedElementPresentMethods(parameter1, AnnotationC.class, AnnotationD.class);
        }
    }

    private static class AnnotatedMethodClass {
        void noAnnotation(String p0) {}

        void multipleAnnotationOddity(
                @Repeated(1) @Container({@Repeated(2), @Repeated(3)}) String p0) {}

        void multipleAnnotationExplicitSingle(@Container({@Repeated(1)}) String p0) {}

        void multipleAnnotation(@Repeated(1) @Repeated(2) String p0) {}

        void singleAnnotation(@Repeated(1) String p0) {}

        static void staticSingleAnnotation(@Repeated(1) String p0) {}

        static Method getMethodWithoutAnnotations() throws Exception {
            return AnnotatedMethodClass.class.getDeclaredMethod("noAnnotation", String.class);
        }

        static Method getMethodMultipleAnnotationOddity() throws Exception {
            return AnnotatedMethodClass.class.getDeclaredMethod(
                    "multipleAnnotationOddity", String.class);
        }

        static Method getMethodMultipleAnnotationExplicitSingle() throws Exception {
            return AnnotatedMethodClass.class.getDeclaredMethod(
                    "multipleAnnotationExplicitSingle", String.class);
        }

        static Method getMethodMultipleAnnotation() throws Exception {
            return AnnotatedMethodClass.class.getDeclaredMethod("multipleAnnotation", String.class);
        }

        static Method getMethodSingleAnnotation() throws Exception {
            return AnnotatedMethodClass.class.getDeclaredMethod("singleAnnotation", String.class);
        }

        static Method getMethodStaticSingleAnnotation() throws Exception {
            return AnnotatedMethodClass.class.getDeclaredMethod("staticSingleAnnotation",
                    String.class);
        }
    }

    private static abstract class AnnotatedMethodAbstractClass {
        abstract void abstractSingleAnnotation(@Repeated(1) String p0);

        static Method getMethodAbstractSingleAnnotation() throws Exception {
            return AnnotatedMethodAbstractClass.class.getDeclaredMethod(
                    "abstractSingleAnnotation", String.class);
        }
    }

    // Tests for isAnnotationPresent and getDeclaredAnnotation.
    public void testMethodDeclaredAnnotation() throws Exception {
        Class<? extends Annotation> repeated = Repeated.class;
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodClass.getMethodWithoutAnnotations(),
                repeated, null);
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodClass.getMethodMultipleAnnotationOddity(),
                repeated, "@Repeated(1)");
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodClass.getMethodMultipleAnnotationExplicitSingle(),
                repeated, null);
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodClass.getMethodMultipleAnnotation(),
                repeated, null);
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodClass.getMethodSingleAnnotation(),
                repeated, "@Repeated(1)");
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodClass.getMethodStaticSingleAnnotation(),
                repeated, "@Repeated(1)");
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodAbstractClass.getMethodAbstractSingleAnnotation(),
                repeated, "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodClass.getMethodWithoutAnnotations(),
                container, null);
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodClass.getMethodMultipleAnnotationOddity(),
                container, "@Container({@Repeated(2), @Repeated(3)})");
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodClass.getMethodMultipleAnnotationExplicitSingle(),
                container, "@Container({@Repeated(1)})");
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodClass.getMethodMultipleAnnotation(),
                container, "@Container({@Repeated(1), @Repeated(2)})");
        checkParameter0DeclaredAnnotation(
                AnnotatedMethodClass.getMethodSingleAnnotation(),
                container, null);
    }

    private static class AnnotatedConstructorClass {
        public AnnotatedConstructorClass(Boolean p0) {}

        public AnnotatedConstructorClass(
                @Repeated(1) @Container({@Repeated(2), @Repeated(3)}) Long p0) {}

        public AnnotatedConstructorClass(@Container({@Repeated(1)}) Double p0) {}

        public AnnotatedConstructorClass(@Repeated(1) @Repeated(2) Integer p0) {}

        public AnnotatedConstructorClass(@Repeated(1) String p0) {}

        static Constructor<?> getConstructorWithoutAnnotations() throws Exception {
            return AnnotatedConstructorClass.class.getDeclaredConstructor(Boolean.class);
        }

        static Constructor<?> getConstructorMultipleAnnotationOddity() throws Exception {
            return AnnotatedConstructorClass.class.getDeclaredConstructor(Long.class);
        }

        static Constructor<?> getConstructorMultipleAnnotationExplicitSingle()
                throws Exception {
            return AnnotatedConstructorClass.class.getDeclaredConstructor(Double.class);
        }

        static Constructor<?> getConstructorSingleAnnotation() throws Exception {
            return AnnotatedConstructorClass.class.getDeclaredConstructor(String.class);
        }

        static Constructor<?> getConstructorMultipleAnnotation() throws Exception {
            return AnnotatedConstructorClass.class.getDeclaredConstructor(Integer.class);
        }
    }

    // Tests for isAnnotationPresent and getDeclaredAnnotation.
    public void testConstructorDeclaredAnnotation() throws Exception {
        Class<? extends Annotation> repeated = Repeated.class;
        checkParameter0DeclaredAnnotation(
                AnnotatedConstructorClass.getConstructorWithoutAnnotations(),
                repeated, null);
        checkParameter0DeclaredAnnotation(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationOddity(),
                repeated, "@Repeated(1)");
        checkParameter0DeclaredAnnotation(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationExplicitSingle(),
                repeated, null);
        checkParameter0DeclaredAnnotation(
                AnnotatedConstructorClass.getConstructorMultipleAnnotation(),
                repeated, null);
        checkParameter0DeclaredAnnotation(
                AnnotatedConstructorClass.getConstructorSingleAnnotation(),
                repeated, "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        checkParameter0DeclaredAnnotation(
                AnnotatedConstructorClass.getConstructorWithoutAnnotations(),
                container, null);
        checkParameter0DeclaredAnnotation(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationOddity(),
                container, "@Container({@Repeated(2), @Repeated(3)})");
        checkParameter0DeclaredAnnotation(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationExplicitSingle(),
                container, "@Container({@Repeated(1)})");
        checkParameter0DeclaredAnnotation(
                AnnotatedConstructorClass.getConstructorMultipleAnnotation(),
                container, "@Container({@Repeated(1), @Repeated(2)})");
        checkParameter0DeclaredAnnotation(
                AnnotatedConstructorClass.getConstructorSingleAnnotation(),
                container, null);
    }

    private static void checkParameter0DeclaredAnnotation(
            Executable executable, Class<? extends Annotation> annotationType,
            String expectedAnnotationString) throws Exception {
        Parameter parameter = executable.getParameters()[0];

        // isAnnotationPresent
        assertIsAnnotationPresent(parameter, annotationType, expectedAnnotationString != null);

        // getDeclaredAnnotation
        assertGetDeclaredAnnotation(parameter, annotationType, expectedAnnotationString);
    }

    public void testMethodGetDeclaredAnnotationsByType() throws Exception {
        Class<? extends Annotation> repeated = Repeated.class;
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedMethodClass.getMethodWithoutAnnotations(),
                repeated, EXPECT_EMPTY);
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotationOddity(),
                repeated, "@Repeated(1)", "@Repeated(2)", "@Repeated(3)");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotationExplicitSingle(),
                repeated, "@Repeated(1)");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotation(),
                repeated, "@Repeated(1)", "@Repeated(2)");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedMethodClass.getMethodSingleAnnotation(),
                repeated, "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedMethodClass.getMethodWithoutAnnotations(),
                container, EXPECT_EMPTY);
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotationOddity(),
                container, "@Container({@Repeated(2), @Repeated(3)})");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotationExplicitSingle(),
                container, "@Container({@Repeated(1)})");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotation(),
                container, "@Container({@Repeated(1), @Repeated(2)})");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedMethodClass.getMethodSingleAnnotation(),
                container, EXPECT_EMPTY);
    }

    public void testConstructorGetDeclaredAnnotationsByType() throws Exception {
        Class<? extends Annotation> repeated = Repeated.class;
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedConstructorClass.getConstructorWithoutAnnotations(),
                repeated, EXPECT_EMPTY);
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationOddity(),
                repeated, "@Repeated(1)", "@Repeated(2)", "@Repeated(3)");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationExplicitSingle(),
                repeated, "@Repeated(1)");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotation(),
                repeated, "@Repeated(1)", "@Repeated(2)");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedConstructorClass.getConstructorSingleAnnotation(),
                repeated, "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedConstructorClass.getConstructorWithoutAnnotations(),
                container, EXPECT_EMPTY);
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationOddity(),
                container, "@Container({@Repeated(2), @Repeated(3)})");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationExplicitSingle(),
                container, "@Container({@Repeated(1)})");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotation(),
                container, "@Container({@Repeated(1), @Repeated(2)})");
        checkParameter0GetDeclaredAnnotationsByType(
                AnnotatedConstructorClass.getConstructorSingleAnnotation(),
                container, EXPECT_EMPTY);
    }

    private static void checkParameter0GetDeclaredAnnotationsByType(
            Executable executable, Class<? extends Annotation> annotationType,
            String... expectedAnnotationStrings) throws Exception {
        Parameter parameter = executable.getParameters()[0];
        AnnotatedElementTestSupport.assertGetDeclaredAnnotationsByType(
                parameter, annotationType, expectedAnnotationStrings);
    }

    public void testMethodGetAnnotationsByType() throws Exception {
        Class<? extends Annotation> repeated = Repeated.class;
        checkParameter0GetAnnotationsByType(
                AnnotatedMethodClass.getMethodWithoutAnnotations(),
                repeated, EXPECT_EMPTY);
        checkParameter0GetAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotationOddity(),
                repeated, "@Repeated(1)", "@Repeated(2)", "@Repeated(3)");
        checkParameter0GetAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotationExplicitSingle(),
                repeated, "@Repeated(1)");
        checkParameter0GetAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotation(),
                repeated, "@Repeated(1)", "@Repeated(2)");
        checkParameter0GetAnnotationsByType(
                AnnotatedMethodClass.getMethodSingleAnnotation(),
                repeated, "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        checkParameter0GetAnnotationsByType(
                AnnotatedMethodClass.getMethodWithoutAnnotations(),
                container, EXPECT_EMPTY);
        checkParameter0GetAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotationOddity(),
                container, "@Container({@Repeated(2), @Repeated(3)})");
        checkParameter0GetAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotationExplicitSingle(),
                container, "@Container({@Repeated(1)})");
        checkParameter0GetAnnotationsByType(
                AnnotatedMethodClass.getMethodMultipleAnnotation(),
                container, "@Container({@Repeated(1), @Repeated(2)})");
        checkParameter0GetAnnotationsByType(
                AnnotatedMethodClass.getMethodSingleAnnotation(),
                container, EXPECT_EMPTY);
    }

    public void testConstructorGetAnnotationsByType() throws Exception {
        Class<? extends Annotation> repeated = Repeated.class;
        checkParameter0GetAnnotationsByType(
                AnnotatedConstructorClass.getConstructorWithoutAnnotations(),
                repeated, EXPECT_EMPTY);
        checkParameter0GetAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationOddity(),
                repeated, "@Repeated(1)", "@Repeated(2)", "@Repeated(3)");
        checkParameter0GetAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationExplicitSingle(),
                repeated, "@Repeated(1)");
        checkParameter0GetAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotation(),
                repeated, "@Repeated(1)", "@Repeated(2)");
        checkParameter0GetAnnotationsByType(
                AnnotatedConstructorClass.getConstructorSingleAnnotation(),
                repeated, "@Repeated(1)");

        Class<? extends Annotation> container = Container.class;
        checkParameter0GetAnnotationsByType(
                AnnotatedConstructorClass.getConstructorWithoutAnnotations(),
                container, EXPECT_EMPTY);
        checkParameter0GetAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationOddity(),
                container, "@Container({@Repeated(2), @Repeated(3)})");
        checkParameter0GetAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotationExplicitSingle(),
                container, "@Container({@Repeated(1)})");
        checkParameter0GetAnnotationsByType(
                AnnotatedConstructorClass.getConstructorMultipleAnnotation(),
                container, "@Container({@Repeated(1), @Repeated(2)})");
        checkParameter0GetAnnotationsByType(
                AnnotatedConstructorClass.getConstructorSingleAnnotation(),
                container, EXPECT_EMPTY);
    }

    private static void checkParameter0GetAnnotationsByType(
            Executable executable, Class<? extends Annotation> annotationType,
            String... expectedAnnotationStrings) throws Exception {
        Parameter parameter = executable.getParameters()[0];
        AnnotatedElementTestSupport.assertGetAnnotationsByType(
                parameter, annotationType, expectedAnnotationStrings);
    }

    /**
     * As an inner class the constructor will actually have two parameters: the first, referencing
     * the enclosing object, is inserted by the compiler.
     */
    class InnerClass {
        InnerClass(@Repeated(1) String p1) {}
    }

    /** Special case testing for a compiler-generated constructor parameter. */
    public void testImplicitConstructorParameters_singleAnnotation() throws Exception {
        Constructor<InnerClass> constructor =
                InnerClass.class.getDeclaredConstructor(
                        AnnotatedElementParameterTest.class, String.class);
        Parameter[] parameters = constructor.getParameters();

        // The compiler-generated constructor should have no annotations.
        Parameter parameter0 = parameters[0];
        AnnotatedElementTestSupport.assertGetAnnotationsByType(
                parameter0, Repeated.class, new String[0]);
        AnnotatedElementTestSupport.assertGetDeclaredAnnotationsByType(
                parameter0, Repeated.class, new String[0]);
        AnnotatedElementTestSupport.assertGetDeclaredAnnotation(
                parameter0, Repeated.class, null);
        AnnotatedElementTestSupport.assertIsAnnotationPresent(parameter0, Repeated.class, false);

        // The annotation should remain on the correct parameter.
        Parameter parameter1 = parameters[1];
        AnnotatedElementTestSupport.assertGetAnnotationsByType(
                parameter1, Repeated.class, new String[] {"@Repeated(1)"});
        AnnotatedElementTestSupport.assertGetDeclaredAnnotationsByType(
                parameter1, Repeated.class, new String[] {"@Repeated(1)"});
        AnnotatedElementTestSupport.assertGetDeclaredAnnotation(
                parameter1, Repeated.class, "@Repeated(1)");
        AnnotatedElementTestSupport.assertIsAnnotationPresent(
                parameter1, Repeated.class, true);
    }
}
