/*
 * OperatorTests.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.decompiler;

import org.junit.Test;

import java.util.Date;

public class OperatorTests extends DecompilerTest {
    private static class A {
        public String test(final String s, final char c, final byte b, final float n, final Date date) {
            return b + ":" + (int)c+ ":" + c + ":" + s + ":" + n + ":" + date;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private static class B {
        public void test() {
            String s = "";
            s += "james";
        }
    }

    @SuppressWarnings("UnusedAssignment")
    private static class C {
        public void test() {
            int n = 0;
            System.out.println(n++);
        }
    }

    private static class D {
        public void test(final int x) {
            if (x < 5 || x >= 2) {
                System.out.println("ifTrue");
            }
            else {
                System.out.println("ifFalse");
            }
        }
    }

    private static class E {
        public void test(final int x) {
            if (x > 2 && x <= 5) {
                System.out.println("ifTrue");
            }
            else {
                System.out.println("ifFalse");
            }
        }
    }

    private static class F {
        public void test(final int x) {
            System.out.println((x > 0) ? "positive" : ((x < 0) ? "negative" : "zero"));
        }
    }

    private static class G {
        private int x;
        private static int y;
        private int[] a;
        private static int[] b = new int[] { 0 };

        private G() {
            super();
            this.a = new int[] { 0 };
        }

        public int f() {
            return this.x++;
        }

        public int g() {
            return this.a[0]++;
        }

        public int h(int n) {
            return (++this.x + this.a[n++]) / (this.a[++n] + ++this.a[n]) * (++G.b[++G.y]);
        }
    }

    private static class H {
        public String test1(String s) {
            s += "allow compound assignment";
            return s;
        }

        public Object test2(Object o) {
            o = o + "allow compound assignment";
            return o;
        }

        public String test3(String s) {
            s = "forbid compound assignment" + s;
            return s;
        }

        public Object test4(Object o) {
            o = "forbid compound assignment" + o;
            return o;
        }
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    private static class I {
        public byte test(final byte x) {
            return (byte) ~(~x);
        }

        public char test(final char x) {
            return (char) ~(~x);
        }

        public short test(final short x) {
            return (short) ~(~x);
        }

        public int test(final int x) {
            return ~(~x);
        }

        public long test(final long x) {
            return ~(~x);
        }

        public byte altTest(final byte x) {
            return (byte)(x ^ -1);
        }

        public char altTest(final char x) {
            return (char)(x ^ -1);
        }

        public short altTest(final short x) {
            return (short)(x ^ -1);
        }

        public int altTest(final int x) {
            return x ^ -1;
        }

        public long altTest(final long x) {
            return x ^ -1;
        }
    }

    @Test
    public void testStringConcatenation() {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A {\n" +
            "    public String test(final String s, final char c, final byte b, final float n, final Date date) {\n" +
            "        return b + \":\" + (int)c + \":\" + c + \":\" + s + \":\" + n + \":\" + date;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testStringConcatenationToExistingString() {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static class B {\n" +
            "    public void test() {\n" +
            "        String s = \"\";\n" +
            "        s += \"james\";\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testPostIncrementTransform() {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private static class C {\n" +
            "    public void test() {\n" +
            "        int n = 0;\n" +
            "        System.out.println(n++);\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testLogicalOr() {
        verifyOutput(
            D.class,
            defaultSettings(),
            "private static class D {\n" +
            "    public void test(final int x) {\n" +
            "        if (x < 5 || x >= 2) {\n" +
            "            System.out.println(\"ifTrue\");\n" +
            "        }\n" +
            "        else {\n" +
            "            System.out.println(\"ifFalse\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testLogicalAnd() {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private static class E {\n" +
            "    public void test(final int x) {\n" +
            "        if (x > 2 && x <= 5) {\n" +
            "            System.out.println(\"ifTrue\");\n" +
            "        }\n" +
            "        else {\n" +
            "            System.out.println(\"ifFalse\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testTernaryOperator() {
        verifyOutput(
            F.class,
            defaultSettings(),
            "private static class F {\n" +
            "    public void test(final int x) {\n" +
            "        System.out.println((x > 0) ? \"positive\" : ((x < 0) ? \"negative\" : \"zero\"));\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testPostIncrementOptimizations() {
        verifyOutput(
            G.class,
            defaultSettings(),
            "private static class G {\n" +
            "    private int x;\n" +
            "    private static int y;\n" +
            "    private int[] a;\n" +
            "    private static int[] b;\n" +
            "    private G() {\n" +
            "        this.a = new int[] { 0 };\n" +
            "    }\n" +
            "    public int f() {\n" +
            "        return this.x++;\n" +
            "    }\n" +
            "    public int g() {\n" +
            "        return this.a[0]++;\n" +
            "    }\n" +
            "    public int h(int n) {\n" +
            "        return (++this.x + this.a[n++]) / (this.a[++n] + ++this.a[n]) * ++G.b[++G.y];\n" +
            "    }\n" +
            "    static {\n" +
            "        G.b = new int[] { 0 };\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testRightSideStringConcatNotWrittenAsCompoundAssignment() {
        verifyOutput(
            H.class,
            defaultSettings(),
            "private static class H {\n" +
            "    public String test1(String s) {\n" +
            "        s += \"allow compound assignment\";\n" +
            "        return s;\n" +
            "    }\n" +
            "    public Object test2(Object o) {\n" +
            "        o += \"allow compound assignment\";\n" +
            "        return o;\n" +
            "    }\n" +
            "    public String test3(String s) {\n" +
            "        s = \"forbid compound assignment\" + s;\n" +
            "        return s;\n" +
            "    }\n" +
            "    public Object test4(Object o) {\n" +
            "        o = \"forbid compound assignment\" + o;\n" +
            "        return o;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testExclusiveOrToBitwiseNotSimplification() {
        verifyOutput(
            I.class,
            defaultSettings(),
            "private static class I {\n" +
            "    public byte test(final byte x) {\n" +
            "        return (byte)~(~x);\n" +
            "    }\n" +
            "    public char test(final char x) {\n" +
            "        return (char)~(~x);\n" +
            "    }\n" +
            "    public short test(final short x) {\n" +
            "        return (short)~(~x);\n" +
            "    }\n" +
            "    public int test(final int x) {\n" +
            "        return ~(~x);\n" +
            "    }\n" +
            "    public long test(final long x) {\n" +
            "        return ~(~x);\n" +
            "    }\n" +
            "    public byte altTest(final byte x) {\n" +
            "        return (byte)~x;\n" +
            "    }\n" +
            "    public char altTest(final char x) {\n" +
            "        return (char)~x;\n" +
            "    }\n" +
            "    public short altTest(final short x) {\n" +
            "        return (short)~x;\n" +
            "    }\n" +
            "    public int altTest(final int x) {\n" +
            "        return ~x;\n" +
            "    }\n" +
            "    public long altTest(final long x) {\n" +
            "        return ~x;\n" +
            "    }\n" +
            "}\n"
        );
    }
}
