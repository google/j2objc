package com.strobel.decompiler;

import org.junit.Test;

public class TypeTests extends DecompilerTest {
    @SuppressWarnings("unchecked")
    private static final class A {
        public Class<? extends String> test1(final String s) {
            return s.getClass();
        }

        public Class<String> test2() {
            return String.class;
        }

        public <T> Class<? extends T> test3(final T t) {
            return (Class<? extends T>) t.getClass();
        }
    }

    private static final class B {
        public <T> T[] testClone1(final T[] array) {
            return array.clone();
        }

        public Class<? extends int[]> testClone2(final int[] array) {
            return array.clone().getClass();
        }

        public <T> int testLength1(final T[] array) {
            return array.length;
        }

        public int testLength2(final int[] array) {
            return array.length;
        }
    }

    @Test
    public void testGetClass() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static final class A {\n" +
            "    public Class<? extends String> test1(final String s) {\n" +
            "        return s.getClass();\n" +
            "    }\n" +
            "    public Class<String> test2() {\n" +
            "        return String.class;\n" +
            "    }\n" +
            "    public <T> Class<? extends T> test3(final T t) {\n" +
            "        return (Class<? extends T>)t.getClass();\n" +
            "    }\n" +
            "}\n"
        );
    }
    @Test
    public void testArrayMembers() throws Throwable {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static final class B {\n" +
            "    public <T> T[] testClone1(final T[] array) {\n" +
            "        return array.clone();\n" +
            "    }\n" +
            "    public Class<? extends int[]> testClone2(final int[] array) {\n" +
            "        return array.clone().getClass();\n" +
            "    }" +
            "    public <T> int testLength1(final T[] array) {\n" +
            "        return array.length;\n" +
            "    }\n" +
            "    public int testLength2(final int[] array) {\n" +
            "        return array.length;\n" +
            "    }\n" +
            "}\n"
        );
    }
}
