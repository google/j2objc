package com.strobel.decompiler;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({
    "unchecked",
    "UnusedDeclaration",
    "Convert2Diamond",
    "LocalCanBeFinal",
    "UnnecessaryInterfaceModifier",
    "FieldMayBeFinal"
})
public class AnnotationTests extends DecompilerTest {
    private @interface A {
        String[] value();
        int intValue() default 42;
    }

    private @interface B {
        String value() default "";
        String name() default "";
        char[] characters() default {};
        int integer() default 0;
        double real() default 0.0;
        A nested() default @A("forty-two");
        Class<?> type() default void.class;
    }

    private @interface C {
        public static final int x = 3;
        public static final int y = 3;
        public static final Map<String, Integer> z = new HashMap<String, Integer>();

        String value() default "wibble";

        public static class A {
            private static final int TEST = 3;
            private int y;

            public A(int y) {
                super();
                this.y = y;
            }
        }
    }

    @Test
    public void testAnnotationWithDefault() {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private @interface A {\n" +
            "    String[] value();\n" +
            "    int intValue() default 42;\n" +
            "}\n"
        );
    }

    @Test
    public void testComplexAnnotation() {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private @interface B {\n" +
            "    String value() default \"\";\n" +
            "    String name() default \"\";\n" +
            "    char[] characters() default {};\n" +
            "    int integer() default 0;\n" +
            "    double real() default 0.0;\n" +
            "    A nested() default @A({ \"forty-two\" });\n" +
            "    Class<?> type() default void.class;\n" +
            "}\n"
        );
    }

    @Test
    public void testAnnotationWithInnerClassAndConstants() {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private @interface C {\n" +
            "    public static final int x = 3;\n" +
            "    public static final int y = 3;\n" +
            "    public static final Map<String, Integer> z = new HashMap<String, Integer>();\n" +
            "    String value() default \"wibble\";\n" +
            "    public static class A\n" +
            "    {\n" +
            "        private static final int TEST = 3;\n" +
            "        private int y;\n" +
            "        public A(final int y) {\n" +
            "            this.y = y;\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }
}
