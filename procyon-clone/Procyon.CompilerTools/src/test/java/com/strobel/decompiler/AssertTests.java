package com.strobel.decompiler;

import org.junit.Test;

public class AssertTests extends DecompilerTest {
    private static class A {
        public void test(final String s) {
            assert s.equals("foo");
        }
    }

    private static class B {
        public void test(final String s) {
            assert s.equals("foo") : "Expected 'foo'.";
        }
    }

    private static class C {
        public void test(final String s) {
            assert s.equals("foo") : "Expected 'foo', got: " + s;
        }
    }

    private static class D {
        public void test() {
            assert false;
        }
    }

    private static class E {
        public void test() {
            assert true;
        }
    }

    private static class F {
        public void test() {
            assert false : "false";
        }
    }

    private static class G {
        public void test() {
            assert true : "true";
        }
    }

    @Test
    public void testSimpleAssert() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A {\n" +
            "    public void test(final String s) {\n" +
            "        assert s.equals(\"foo\");\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testAssertWithLiteralMessage() throws Throwable {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static class B {\n" +
            "    public void test(final String s) {\n" +
            "        assert s.equals(\"foo\") : \"Expected 'foo'.\";\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testAssertWithExpressionMessage() throws Throwable {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private static class C {\n" +
            "    public void test(final String s) {\n" +
            "        assert s.equals(\"foo\") : \"Expected 'foo', got: \" + s;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testAssertFalse() throws Throwable {
        verifyOutput(
            D.class,
            defaultSettings(),
            "private static class D {\n" +
            "    public void test() {\n" +
            "        assert false;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testAssertTrue() throws Throwable {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private static class E {\n" +
            "    public void test() {\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testAssertFalseWithMessage() throws Throwable {
        verifyOutput(
            F.class,
            defaultSettings(),
            "private static class F {\n" +
            "    public void test() {\n" +
            "        assert false : \"false\";\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testAssertTrueWithMessage() throws Throwable {
        verifyOutput(
            G.class,
            defaultSettings(),
            "private static class G {\n" +
            "    public void test() {\n" +
            "    }\n" +
            "}\n"
        );
    }
}
