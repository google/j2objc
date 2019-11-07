/*
 * SwitchTests.java
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

public class SwitchTests extends DecompilerTest {
    private static class A {
        public void test(final int i) {
            switch (i) {
                case 1:
                    System.out.print("1");
                    break;
                case 0:
                    System.out.print("0");
                    break;
                case -1:
                    System.out.print("-1");
                    break;
                default:
                    System.out.print("Bad Value");
                    break;
            }
            System.out.println("after switch");
        }
    }

    private static class B {
        public void test(final int i) {
            switch (i) {
                case -2:
                case -3:
                case 1:
                    System.out.print("1");
                case 0:
                    System.out.print("0");
                case -1:
                    System.out.print("-1");
                default:
                    System.out.print("end of fall through");
            }
            System.out.println("after switch");
        }
    }

    private static class C {
        public void test(final String s) {
            switch (s.toLowerCase()) {
                case "1":
                case "2":
                case "3":
                    System.out.println(s);
                    break;

                //
                // Include two strings with a hash code collision ("Aa", "BB").
                //
                case "Aa":
                case "BB":
                    System.out.println(s.toUpperCase());
                    break;

                default:
                    System.out.println(s);
                    break;
            }
            System.out.println("after switch");
        }
    }

    private static class E {
        public int test() {
        outer:
            do {
            inner:
                for (int x = 0; x < 10; ++x) {
                    switch (x) {
                        case 1:
                            break;
                        case 2:
                            continue outer;
                        case 3:
                            continue inner;
                        case 4:
                            break outer;
                        case 5:
                            break inner;
                        default:
                            System.out.println("default");
                            break;
                    }
                    System.out.println("after switch");
                }
                System.out.println("after inner loop");
            }
            while (Boolean.parseBoolean("false"));
            return -1;
        }
    }

    private static class F {
        public void f(final int x) {
            switch (x) {
                case 0:
                    System.out.println("0");
                case 1:
                    System.out.println("1");
                    break;
                case 2:
                    System.out.println("2");
                default:
            }
            System.out.println("Test");
        }

        public void g(final int x) {
            switch (x) {
                case 0:
                    System.out.println("0");
                case 1:
                    System.out.println("1");
                    break;
                case 2:
                    System.out.println("2");
            }
            System.out.println("Test");
        }
    }

    private static class G {
        public static void test(final String args[]) {
            int x = -1;

            switch (args.length % -5) {
                case 3:
                    x += 3;
                case 1:
                    x--;
                    if (x == -2) {
                        break;
                    }
                case 0:
                    x += (x << x);
                    break;
                case 2:
                default:
                    x = x ^ (int) 0xABCD000L;
                case 4:
                    x *= 4;
                    break;
            }

            System.out.println(x);
        }
    }

    @Test
    public void testSimpleSwitch() {
        verifyOutput(
            A.class,
            createSettings(OPTION_FLATTEN_SWITCH_BLOCKS),
            "private static class A {\n" +
            "    public void test(final int i) {\n" +
            "        switch (i) {\n" +
            "            case 1:\n" +
            "                System.out.print(\"1\");\n" +
            "                break;\n" +
            "            case 0:\n" +
            "                System.out.print(\"0\");\n" +
            "                break;\n" +
            "            case -1:\n" +
            "                System.out.print(\"-1\");\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.print(\"Bad Value\");\n" +
            "                break;\n" +
            "        }\n" +
            "        System.out.println(\"after switch\");\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testSwitchFallThrough() {
        //
        // The decompiler is expected to forgo the 'default' label in cases where all
        // case blocks have edges connecting to the default block in a CFG.  This is
        // normally the desired behavior, though in the rare cases of total fall-through,
        // it results in slightly different code.
        //

        verifyOutput(
            B.class,
            createSettings(OPTION_FLATTEN_SWITCH_BLOCKS),
            "private static class B {\n" +
            "    public void test(final int i) {\n" +
            "        switch (i) {\n" +
            "            case -3:\n" +
            "            case -2:\n" +
            "            case 1:\n" +
            "                System.out.print(\"1\");\n" +
            "            case 0:\n" +
            "                System.out.print(\"0\");\n" +
            "            case -1:\n" +
            "                System.out.print(\"-1\");\n" +
            "                break;\n" +
            "        }\n" +
            "        System.out.print(\"end of fall through\");\n" +
            "        System.out.println(\"after switch\");\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testStringSwitch() {
        verifyOutput(
            C.class,
            createSettings(OPTION_FLATTEN_SWITCH_BLOCKS),
            "private static class C {\n" +
            "    public void test(final String s) {\n" +
            "        final String lowerCase = s.toLowerCase();\n" +
            "        switch (lowerCase) {\n" +
            "            case \"1\":\n" +
            "            case \"2\":\n" +
            "            case \"3\":\n" +
            "                System.out.println(s);\n" +
            "                break;\n" +
            "            case \"Aa\":\n" +
            "            case \"BB\":\n" +
            "                System.out.println(s.toUpperCase());\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(s);\n" +
            "                break;\n" +
            "        }\n" +
            "        System.out.println(\"after switch\");\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnumSwitch() {
        verifyOutput(
            SwitchTests$D.class,
            createSettings(OPTION_EXCLUDE_NESTED | OPTION_FLATTEN_SWITCH_BLOCKS),
            "final class SwitchTests$D {\n" +
            "    public void test(final Color color) {\n" +
            "        switch (color) {\n" +
            "            case BLUE:\n" +
            "                System.out.println(\"blue\");\n" +
            "                break;\n" +
            "            case RED:\n" +
            "                System.out.println(\"red\");\n" +
            "                break;\n" +
            "            default:\n" +
            "                System.out.println(\"other\");\n" +
            "                break;\n" +
            "        }\n" +
            "        System.out.println(\"after switch\");\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testCaseBreaksOuterLoop() {
        verifyOutput(
            E.class,
            createSettings(OPTION_FLATTEN_SWITCH_BLOCKS),
            "private static class E {\n" +
            "    public int test() {\n" +
            "    Label_0097:\n" +
            "        do {\n" +
            "        Label_0081:\n" +
            "            for (int x = 0; x < 10; ++x) {\n" +
            "                switch (x) {\n" +
            "                    case 1:\n" +
            "                        break;\n" +
            "                    case 2:\n" +
            "                        continue Label_0097;\n" +
            "                    case 3:\n" +
            "                        continue;\n" +
            "                    case 4:\n" +
            "                        break Label_0097;\n" +
            "                    case 5:\n" +
            "                        break Label_0081;\n" +
            "                    default:\n" +
            "                        System.out.println(\"default\");\n" +
            "                        break;\n" +
            "                }\n" +
            "                System.out.println(\"after switch\");\n" +
            "            }\n" +
            "            System.out.println(\"after inner loop\");\n" +
            "        } while (Boolean.parseBoolean(\"false\"));\n" +
            "        return -1;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testMultipleMethodsWithFallThrough() {
        //
        // Case 0 branches to the same offset in both methods.  This caused problems
        // due to a scope-related bug in BreakTargetRelocation.
        //

        verifyOutput(
            F.class,
            createSettings(OPTION_FLATTEN_SWITCH_BLOCKS),
            "private static class F {\n" +
            "    public void f(final int x) {\n" +
            "        switch (x) {\n" +
            "            case 0:\n" +
            "                System.out.println(\"0\");\n" +
            "            case 1:\n" +
            "                System.out.println(\"1\");\n" +
            "                break;\n" +
            "            case 2:\n" +
            "                System.out.println(\"2\");\n" +
            "                break;\n" +
            "        }\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "    public void g(final int x) {\n" +
            "        switch (x) {\n" +
            "            case 0:\n" +
            "                System.out.println(\"0\");\n" +
            "            case 1:\n" +
            "                System.out.println(\"1\");\n" +
            "                break;\n" +
            "            case 2:\n" +
            "                System.out.println(\"2\");\n" +
            "                break;\n" +
            "        }\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testSwitchFallThrough2() {
        verifyOutput(
            G.class,
            createSettings(OPTION_FLATTEN_SWITCH_BLOCKS),
            "private static class G {\n" +
            "    public static void test(final String[] args) {\n" +
            "        int x = -1;\n" +
            "        switch (args.length % -5) {\n" +
            "            case 3:\n" +
            "                x += 3;\n" +
            "            case 1:\n" +
            "                if (--x == -2) {\n" +
            "                    break;\n" +
            "                }\n" +
            "            case 0:\n" +
            "                x += x << x;\n" +
            "                break;\n" +
            "            default:\n" +
            "                x ^= 0xABCD000;\n" +
            "            case 4:\n" +
            "                x *= 4;\n" +
            "                break;\n" +
            "        }\n" +
            "        System.out.println(x);" +
            "    }\n" +
            "}\n"
        );
    }
}

//
// For the moment, we need to declare this test class out here to ensure the generated
// SwitchMap type is in scope.
//
final class SwitchTests$D {
    public enum Color {
        RED,
        BLUE;
    }

    public void test(final Color color) {
        switch (color) {
            case BLUE:
                System.out.println("blue");
                break;
            case RED:
                System.out.println("red");
                break;
            default:
                System.out.println("other");
                break;
        }
        System.out.println("after switch");
    }
}
