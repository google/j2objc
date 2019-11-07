package com.strobel.decompiler;

import org.junit.Test;

@SuppressWarnings({ "unused", "UnnecessaryBoxing" })
public class BoxingTests extends DecompilerTest {
    @SuppressWarnings("InfiniteRecursion")
    private static class A {
        void a(final Integer i) {
            this.a(i);
            this.b(i);
            this.c(i);
            this.d((double) i);
            this.e((short) i.intValue());
            this.f((short) i.intValue());
        }

        void b(final int i) {
            this.a(i);
            this.b(i);
            this.c((double) i);
            this.d((double) i);
            this.e((short) i);
            this.f((short) i);
        }

        void c(final double d) {
            this.a((int) d);
            this.b((int) d);
            this.c(d);
            this.d(d);
            this.e((short) d);
            this.f((short) d);
        }

        void d(final Double d) {
            this.a((int) d.doubleValue());
            this.b((int) d.doubleValue());
            this.c(d);
            this.d(d);
            this.e((short) d.doubleValue());
            this.f((short) d.doubleValue());
        }

        void e(final Short s) {
            this.a((int) s);
            this.b(s);
            this.c(s);
            this.d((double) s);
            this.e(s);
            this.f(s);
        }

        void f(final short s) {
            this.a((int) s);
            this.b((int) s);
            this.c((double) s);
            this.d((double) s);
            this.e(s);
            this.f(s);
        }
    }

    private static class B {
        boolean test(final Integer n) {
            return Integer.valueOf(n.intValue()) != null;
        }
    }

    @SuppressWarnings("UnusedParameters")
    private static class C {
        void test(final Object o) {
        }

        void test(final Integer i) {
        }

        void test(final int i) {
        }

        void t(final int x) {
            test(Integer.valueOf(x));
            test((Object) Integer.valueOf(x));
            test(x);
        }
    }

    private static class D {
        boolean t(final Integer i, final int j) {
            return (i == Integer.valueOf(j) && i == j);
        }
    }

    @SuppressWarnings({ "UnusedDeclaration", "RedundantCast" })
    private static class E {
        void t(final boolean b) {
            final Double d = 4.3;
            final Integer i = 3;
            final short s = (short) (b ? i : (int) Integer.valueOf((int) d.doubleValue()));
        }
    }

    @SuppressWarnings("RedundantCast")
    private static class F {
        Object value() {
            return null;
        }

        String test1() {
            final Object value = this.value();
            if (value instanceof Character && (char) value == '\u000b') {
                return "'\\013'";
            }
            return null;
        }

        String test2() {
            final Character value = (Character) this.value();
            if (value == '\u000b') {
                return "'\\013'";
            }
            return null;
        }
    }

    @SuppressWarnings("UnusedParameters")
    private static class G {
        Float valueF() {
            return null;
        }

        Integer valueI() {
            return null;
        }

        void testI(final int i) {
        }

        void testF(final float f) {
        }

        void test1() {
            testF(valueI());
            testF((float) valueI());
            testF(valueI().floatValue());
        }

        void test2() {
            testI(valueF().intValue());
        }
    }

    @Test
    public void testImplicitBoxingTranslation() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A {\n" +
            "    void a(final Integer i) {\n" +
            "        this.a(i);\n" +
            "        this.b(i);\n" +
            "        this.c(i);\n" +
            "        this.d((double)i);\n" +
            "        this.e((short)(int)i);\n" +
            "        this.f((short)(int)i);\n" +
            "    }\n" +
            "    void b(final int i) {\n" +
            "        this.a(i);\n" +
            "        this.b(i);\n" +
            "        this.c(i);\n" +
            "        this.d((double)i);\n" +
            "        this.e((short)i);\n" +
            "        this.f((short)i);\n" +
            "    }\n" +
            "    void c(final double d) {\n" +
            "        this.a((int)d);\n" +
            "        this.b((int)d);\n" +
            "        this.c(d);\n" +
            "        this.d(d);\n" +
            "        this.e((short)d);\n" +
            "        this.f((short)d);\n" +
            "    }\n" +
            "    void d(final Double d) {\n" +
            "        this.a((int)(double)d);\n" +
            "        this.b((int)(double)d);\n" +
            "        this.c(d);\n" +
            "        this.d(d);\n" +
            "        this.e((short)(double)d);\n" +
            "        this.f((short)(double)d);\n" +
            "    }\n" +
            "    void e(final Short s) {\n" +
            "        this.a((int)s);\n" +
            "        this.b(s);\n" +
            "        this.c(s);\n" +
            "        this.d((double)s);\n" +
            "        this.e(s);\n" +
            "        this.f(s);\n" +
            "    }\n" +
            "    void f(final short s) {\n" +
            "        this.a((int)s);\n" +
            "        this.b(s);\n" +
            "        this.c(s);\n" +
            "        this.d((double)s);\n" +
            "        this.e(s);\n" +
            "        this.f(s);\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testExceptionalUnboxingNotOmitted() throws Exception {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static class B {\n" +
            "    boolean test(final Integer n) {\n" +
            "        return Integer.valueOf(n) != null;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testNoImproperCastRemoval() throws Exception {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private static class C {\n" +
            "    void test(final Object o) {\n" +
            "    }\n" +
            "    void test(final Integer i) {\n" +
            "    }\n" +
            "    void test(final int i) {\n" +
            "    }\n" +
            "    void t(final int x) {\n" +
            "        this.test(Integer.valueOf(x));\n" +
            "        this.test((Object)x);\n" +
            "        this.test(x);\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testBoxedToBoxedBinaryComparison() throws Exception {
        //
        // If `j` is too big for the boxing cache, this will create a new object,
        // in which case the first test may FAIL even though `i == j` (unless it's
        // incorrectly decompiled to `i == j && i == j`).
        //
        verifyOutput(
            D.class,
            defaultSettings(),
            "private static class D {\n" +
            "    boolean t(final Integer i, final int j) {\n" +
            "        return i == Integer.valueOf(j) && i == j;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testBoxedVersusUnboxedInTernary() throws Exception {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private static class E {\n" +
            "    void t(final boolean b) {\n" +
            "        final Double d = 4.3;\n" +
            "        final Integer i = 3;\n" +
            "        final short s = (short)(b ? i : ((int)(double)d));\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testUnboxedVersusPrimitiveComparison() throws Exception {
        verifyOutput(
            F.class,
            defaultSettings(),
            "private static class F {\n" +
            "    Object value() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    String test1() {\n" +
            "        final Object value = this.value();\n" +
            "        if (value instanceof Character && (char)value == '\\u000b') {\n" +
            "            return \"'\\\\013'\";\n" +
            "        }\n" +
            "        return null;\n" +
            "    }\n" +
            "    String test2() {\n" +
            "        final Character value = (Character)this.value();\n" +
            "        if (value == '\\u000b') {\n" +
            "            return \"'\\\\013'\";\n" +
            "        }\n" +
            "        return null;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testKeepRequiredUnboxingCalls() throws Exception {
        verifyOutput(
            G.class,
            defaultSettings(),
            "private static class G {\n" +
            "    Float valueF() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    Integer valueI() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    void testI(final int i) {\n" +
            "    }\n" +
            "    void testF(final float f) {\n" +
            "    }\n" +
            "    void test1() {\n" +
            "        this.testF(this.valueI());\n" +
            "        this.testF(this.valueI());\n" +
            "        this.testF(this.valueI());\n" +
            "    }\n" +
            "    void test2() {\n" +
            "        this.testI(this.valueF().intValue());\n" +
            "    }\n" +
            "}\n"
        );
    }
}
