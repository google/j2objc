package com.strobel.decompiler;

import com.strobel.util.EmptyArrayCache;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class TypeInferenceTests extends DecompilerTest {
    private static class A {
        public void test(final String[] array) {
            boolean b = array.length > 0;
            boolean b2 = array.length < 2;

            int n = 0;

            while (n++ <= 127) {
                if (!(b ^ b2)) {
                    n ^= array.length;
                    break;
                }
                b &= true;
                b2 |= false;
            }

            System.out.println(n);
            System.out.println(b);
            System.out.println(b2);
        }
    }

    private static class B {
        public strictfp void test() {
            double n = 9.007199254740992E15;
            double n2 = n * n;
            double n3 = 9.007199254740991E15;
            double n4 = n2 % n3;

            System.out.println(n4);
            System.out.println(n4 == 1.0);
            System.out.println(n4 * 2.7182818459);
        }
    }

    private interface C {
        public static final Integer[] EMPTY_ARRAY = EmptyArrayCache.fromElementType(Integer.class);
    }

    private interface D {
        public static final List<Integer> EMPTY_ARRAY = Arrays.asList(1, 2, 3, 4, 5);
    }

    private static class E {
        Integer[] f(final Integer[] array) {
            return array;
        }

        public void test() {
            this.f(new Integer[0]);
        }
    }

    private static class F {
        public static <T> List<T> safeCopyOf(final Iterable<T> iterable) {
            final ArrayList<T> list = new ArrayList<T>();

            for (T element : iterable) {
                list.add(Objects.requireNonNull(element));
            }

            return list;
        }
    }

    private static class G {
        public static void test(final String[] args) throws Throwable {
            String s = null;
            String s2 = null;

            try {
                try {
                    s2 = args[0];

                    if (args == null) {
                        throw (Exception) (Object) args;
                    }

                    s = args[1];
                }
                catch (ArrayIndexOutOfBoundsException e) {
                }
            }
            catch (Throwable t) {
                System.out.println(t instanceof NullPointerException);
                throw t;
            }

            System.out.println(s2);
            System.out.println(s);
        }
    }

    private static class H {
        private static class Outer<T> {
            private static class Inner1 {
                final Outer outer = null;
            }

            private class Inner2 {
                final Outer<T> outer = null;
            }
        }
    }

    @Test
    public void testBooleanInference() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A {\n" +
            "    public void test(final String[] array) {\n" +
            "        boolean b = array.length > 0;\n" +
            "        boolean b2 = array.length < 2;\n" +
            "        int n = 0;\n" +
            "        while (n++ <= 127) {\n" +
            "            if (!(b ^ b2)) {\n" +
            "                n ^= array.length;\n" +
            "                break;\n" +
            "            }\n" +
            "            b &= true;\n" +
            "            b2 |= false;\n" +
            "        }\n" +
            "        System.out.println(n);\n" +
            "        System.out.println(b);\n" +
            "        System.out.println(b2);\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testDoubleVariables() throws Throwable {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static class B {\n" +
            "    public strictfp void test() {\n" +
            "        final double n = 9.007199254740992E15;\n" +
            "        final double n2 = n * n;\n" +
            "        final double n3 = 9.007199254740991E15;\n" +
            "        final double n4 = n2 % n3;\n" +
            "        System.out.println(n4);\n" +
            "        System.out.println(n4 == 1.0);\n" +
            "        System.out.println(n4 * 2.7182818459);\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testClassArgument() throws Throwable {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private interface C {\n" +
            "    public static final Integer[] EMPTY_ARRAY = EmptyArrayCache.fromElementType(Integer.class);\n" +
            "}\n"
        );
    }

    @Test
    public void testGenericArrayArgument() throws Throwable {
        verifyOutput(
            D.class,
            defaultSettings(),
            "private interface D {\n" +
            "    public static final List<Integer> EMPTY_ARRAY = Arrays.asList(1, 2, 3, 4, 5);\n" +
            "}\n"
        );
    }

    @Test
    public void testMatchingArrayArgument() throws Throwable {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private static class E {\n" +
            "    Integer[] f(final Integer[] array) {\n" +
            "        return array;\n" +
            "    }\n" +
            "    public void test() {\n" +
            "        this.f(new Integer[0]);\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testTypeArgumentsWithLocalTypeVariable() throws Throwable {
        verifyOutput(
            F.class,
            defaultSettings(),
            "private static class F {\n" +
            "    public static <T> List<T> safeCopyOf(final Iterable<T> iterable) {\n" +
            "        final ArrayList<T> list = new ArrayList<T>();\n" +
            "        for (final T element : iterable) {\n" +
            "            list.add(Objects.requireNonNull(element));\n" +
            "        }\n" +
            "        return list;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testVariablesInitializedInTryNotDefinitelyAssigned() throws Throwable {
        verifyOutput(
            G.class,
            defaultSettings(),
            "private static class G {\n" +
            "    public static void test(final String[] args) throws Throwable {\n" +
            "        String s = null;\n" +
            "        String s2 = null;\n" +
            "        try {\n" +
            "            try {\n" +
            "                s2 = args[0];\n" +
            "                if (args == null) {\n" +
            "                    throw (Exception)(Object)args;\n" +
            "                }\n" +
            "                s = args[1];\n" +
            "            }\n" +
            "            catch (ArrayIndexOutOfBoundsException ex) {}\n" +
            "        }\n" +
            "        catch (Throwable t) {\n" +
            "            System.out.println(t instanceof NullPointerException);\n" +
            "            throw t;\n" +
            "        }\n" +
            "        System.out.println(s2);\n" +
            "        System.out.println(s);\n" +
            "    }\n" +
            "}"
        );
    }
}
