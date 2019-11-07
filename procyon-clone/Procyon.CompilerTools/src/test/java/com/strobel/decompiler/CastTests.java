package com.strobel.decompiler;

import org.junit.Test;

public class CastTests extends DecompilerTest {
    @SuppressWarnings({ "RedundantCast", "BoxingBoxedValue" })
    private static final class A {
        @SuppressWarnings("BoxingBoxedValue")
        public static void test() {
            final Character c1 = '1';
            final Character c2 = '2';
            final Object o = '3';

            // A cast on either operand is required to force a primitive comparison; not redundant.
            System.out.println((char) c1 == c2);
            System.out.println(c1 == (char) c2);

            // If one operand is a primitive, and the other is a wrapper, the wrapper need not be cast.
            System.out.println((char) c1 == '*');
            System.out.println('*' == (char) c1);

            // The cast on the Object is required to force a primitive comparison; not redundant.
            System.out.println((char) o == '*');
            System.out.println('*' == (char) o);

            // The cast on the Object is required to force a primitive comparison; not redundant.
            System.out.println((Character) o == '*');
            System.out.println('*' == (Character) o);

            // The cast on the Object triggers an implicit unboxing of the wrapper; not redundant.
            System.out.println((char) o == c1);
            System.out.println(c1 == (char) o);

            // A cast on the Object is required for a primitive comparison, but the wrapper cast is redundant.
            System.out.println((char) o == (char) c1);
            System.out.println((char) c1 == (char) o);

            // Although a reference comparison, the cast on the wrapper has a side effect; not redundant.
//            System.out.println(o == (char) c1);
//            System.out.println((char) c1 == o);
            // Original version no longer compiles with Java 8; rewritten as follows (bytecode is equivalent):
            System.out.println(o == Character.valueOf(c1));
            System.out.println(Character.valueOf(c1) == o);
        }
    }

    private static final class B {
        public static short test(final short a, final short b) {
            final short c = (short) (a + b);
            System.out.println(c);
            return c;
        }
    }

    private static class C {
        public short test(final short x) {
            return (short) -x;
        }
    }

    private static class D {
        public short test(final boolean b) {
            for (short n = (short) (b ? -1 : 0); b; ++n) {
                System.out.println(n);
            }
            return 1;
        }
    }

    private static class E {
        public int valueI() {
            return Integer.MAX_VALUE;
        }

        public float valueF() {
            return 1.0f;
        }

        public long valueL() {
            return 5L;
        }

        public void test() {
            // A cast on at least one of the integer values is required, to prevent potential overflow
            System.out.println(valueI() * (long)valueI());
            System.out.println((long)valueI() * valueI());
            System.out.println((long)valueI() * (long)valueI());

            // To perform floating-point arithmetic with two integer operands, at least one of them needs to be cast.
            System.out.println(valueI() / (float)valueI());
            System.out.println((float)valueI() / valueL());
            System.out.println((float)valueI() / (float)valueI());

            // Just an integer division
            System.out.println(valueI() / valueI());

            // These casts are redundant
            System.out.println(valueF() * (float)valueI());
            System.out.println(valueI() + valueF());
            System.out.println(valueF() - (float)valueL());
            System.out.println(valueI() / valueF());
        }
    }

    @Test
    public void testPrimitiveComparisonCastAnalysis() {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static final class A {\n" +
            "    public static void test() {\n" +
            "        final Character c1 = '1';\n" +
            "        final Character c2 = '2';\n" +
            "        final Object o = '3';\n" +
            "        System.out.println(c1 == (char)c2);\n" +
            "        System.out.println(c1 == (char)c2);\n" +
            "        System.out.println(c1 == '*');\n" +
            "        System.out.println('*' == c1);\n" +
            "        System.out.println((char)o == '*');\n" +
            "        System.out.println('*' == (char)o);\n" +
            "        System.out.println((char)o == '*');\n" +
            "        System.out.println('*' == (char)o);\n" +
            "        System.out.println((char)o == c1);\n" +
            "        System.out.println(c1 == (char)o);\n" +
            "        System.out.println((char)o == c1);\n" +
            "        System.out.println(c1 == (char)o);\n" +
            "        System.out.println(o == Character.valueOf(c1));\n" +
            "        System.out.println(Character.valueOf(c1) == o);\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testShortIntegerAdditionRetainsCast() {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static final class B {\n" +
            "    public static short test(final short a, final short b) {\n" +
            "        final short c = (short)(a + b);\n" +
            "        System.out.println(c);\n" +
            "        return c;\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testShortNegationRetainsCast() {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private static class C {\n" +
            "    public short test(final short x) {\n" +
            "        return (short)(-x);\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testLiteralTernaryAsShortRetainsCast() {
        verifyOutput(
            D.class,
            defaultSettings(),
            "private static class D {\n" +
            "    public short test(final boolean b) {\n" +
            "        short n = (short)(b ? -1 : 0);\n" +
            "        while (b) {\n" +
            "            System.out.println(n);\n" +
            "            ++n;\n" +
            "        }\n" +
            "        return 1;\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testBinaryOperatorResultTypeRetainsCast() {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private static class E {\n" +
            "    public int valueI() {\n" +
            "        return Integer.MAX_VALUE;\n" +
            "    }\n" +
            "    public float valueF() {\n" +
            "        return 1.0f;\n" +
            "    }\n" +
            "    public long valueL() {\n" +
            "        return 5L;\n" +
            "    }\n" +
            "    public void test() {\n" +
            "        System.out.println(this.valueI() * (long)this.valueI());\n" +
            "        System.out.println(this.valueI() * (long)this.valueI());\n" +
            "        System.out.println(this.valueI() * (long)this.valueI());\n" +
            "        System.out.println(this.valueI() / (float)this.valueI());\n" +
            "        System.out.println(this.valueI() / (float)this.valueL());\n" +
            "        System.out.println(this.valueI() / (float)this.valueI());\n" +
            "        System.out.println(this.valueI() / this.valueI());\n" +
            "        System.out.println(this.valueF() * this.valueI());\n" +
            "        System.out.println(this.valueI() + this.valueF());\n" +
            "        System.out.println(this.valueF() - this.valueL());\n" +
            "        System.out.println(this.valueI() / this.valueF());\n" +
            "    }\n" +
            "}"
        );
    }
}


