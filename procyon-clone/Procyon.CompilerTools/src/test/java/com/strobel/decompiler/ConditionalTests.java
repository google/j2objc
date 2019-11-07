package com.strobel.decompiler;

import org.junit.Test;

import java.util.List;
import java.util.Set;

public class ConditionalTests extends DecompilerTest {
    private static class A {
        public boolean test(final List<Object> list, final Set<Object> set) {
            if (list == null) {
                if (set == null) {
                    System.out.println("a");
                }
                else {
                    System.out.println("b");
                }
            }
            else if (set == null) {
                System.out.println("c");
            }
            else if (list.isEmpty()) {
                if (set.isEmpty()) {
                    System.out.println("d");
                }
                else {
                    System.out.println("e");
                }
            }
            else if (set.size() < list.size()) {
                System.out.println("f");
            }
            else {
                System.out.println("g");
            }
            return true;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static class B {
        public boolean test(final List<Object> list, final Set<Object> set) {
            if (list == null) {
                if (set == null) {
                    System.out.println("B");
                }
                else {
                }
            }
            else if (set == null) {
                if (list.isEmpty()) {
                    System.out.println("E");
                }
                else {
                }
            }

            return true;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class C {
        public boolean test1(final boolean a, final boolean b, boolean c) {
            System.out.println((b && a == (c = b) && b) || !c);
            return c;
        }

        public boolean test2(final boolean a, final boolean b, boolean c) {
            System.out.println((b && a == (c = b)) || !c);
            return c;
        }

        public boolean test3(final boolean a, final boolean b, boolean c) {
            System.out.println(b && a || (c = b) || !c);
            return c;
        }

        public boolean test4(final boolean a, final boolean b, boolean c) {
            System.out.println(b && (c = a) || !c);
            return c;
        }

        public boolean test5(final boolean a, final boolean b, boolean c) {
            System.out.println(b || (c = a) || !c);
            return c;
        }

        public boolean test6(final boolean a, final boolean b, boolean c) {
            System.out.println(b && (c = a));
            return c;
        }

        public boolean test7(final boolean a, final boolean b, boolean c) {
            System.out.println(b || (c = a));
            return c;
        }

        public boolean test8(final boolean a, final boolean b, boolean c) {
            System.out.println(b && a == (c = b) && b && c);
            return c;
        }
    }

    private static class D {
        public boolean test(final boolean a, final boolean b, final boolean c, final boolean d) {
            return (a ? b : c) ? d : (c ? b : a);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class E {
        public boolean test1(final boolean a, final boolean b, final boolean c) {
            System.out.println(a || (c ? a : b));
            return c;
        }

        public boolean test2(final boolean a, final boolean b, final boolean c) {
            System.out.println(a && (c ? a : b));
            return c;
        }

        public boolean test3(final boolean a, final boolean b, final boolean c) {
            System.out.println(!a || (c ? a : b));
            return c;
        }

        public boolean test4(final boolean a, final boolean b, final boolean c) {
            System.out.println(!a && (c ? a : b));
            return c;
        }

        public boolean test5(final boolean a, final boolean b, final boolean c) {
            System.out.println(a && (c ? (b ? a : c) : (b ? c : a)));
            return c;
        }

        public boolean test6(final boolean a, final boolean b, final boolean c) {
            System.out.println(a || (c ? (b ? a : c) : (b ? c : a)));
            return c;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class F {
        private boolean c;

        public boolean test1(final boolean a, final boolean b) {
            System.out.println((b && a == (this.c = b) && b) || !this.c);
            return this.c;
        }

        public boolean test2(final boolean a, final boolean b) {
            System.out.println((b && a == (this.c = b)) || !this.c);
            return this.c;
        }

        public boolean test3(final boolean a, final boolean b) {
            System.out.println((b && a) || (this.c = b) || !this.c);
            return this.c;
        }

        public boolean test4(final boolean a, final boolean b) {
            System.out.println((b && (this.c = a)) || !this.c);
            return this.c;
        }

        public boolean test5(final boolean a, final boolean b) {
            System.out.println(b || (this.c = a) || !this.c);
            return this.c;
        }

        public boolean test6(final boolean a, final boolean b) {
            System.out.println(b && (this.c = a));
            return this.c;
        }

        public boolean test7(final boolean a, final boolean b) {
            System.out.println(b || (this.c = a));
            return this.c;
        }

        public boolean test8(final boolean a, final boolean b) {
            System.out.println(b && a == (this.c = b) && b && this.c);
            return this.c;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class G {
        private static boolean c;

        public boolean test1(final boolean a, final boolean b) {
            System.out.println((b && a == (G.c = b) && b) || !G.c);
            return G.c;
        }

        public boolean test2(final boolean a, final boolean b) {
            System.out.println((b && a == (G.c = b)) || !G.c);
            return G.c;
        }

        public boolean test3(final boolean a, final boolean b) {
            System.out.println((b && a) || (G.c = b) || !G.c);
            return G.c;
        }

        public boolean test4(final boolean a, final boolean b) {
            System.out.println((b && (G.c = a)) || !G.c);
            return G.c;
        }

        public boolean test5(final boolean a, final boolean b) {
            System.out.println(b || (G.c = a) || !G.c);
            return G.c;
        }

        public boolean test6(final boolean a, final boolean b) {
            System.out.println(b && (G.c = a));
            return G.c;
        }

        public boolean test7(final boolean a, final boolean b) {
            System.out.println(b || (G.c = a));
            return G.c;
        }

        public boolean test8(final boolean a, final boolean b) {
            System.out.println(b && a == (G.c = b) && b && G.c);
            return G.c;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class H {
        public boolean test1(final boolean a, final boolean b, final boolean[] c) {
            System.out.println((b && a == (c[0] = b) && b) || !c[0]);
            return c[0];
        }

        public boolean test2(final boolean a, final boolean b, final boolean[] c) {
            System.out.println((b && a == (c[0] = b)) || !c[0]);
            return c[0];
        }

        public boolean test3(final boolean a, final boolean b, final boolean[] c) {
            System.out.println((b && a) || (c[0] = b) || !c[0]);
            return c[0];
        }

        public boolean test4(final boolean a, final boolean b, final boolean[] c) {
            System.out.println((b && (c[0] = a)) || !c[0]);
            return c[0];
        }

        public boolean test5(final boolean a, final boolean b, final boolean[] c) {
            System.out.println(b || (c[0] = a) || !c[0]);
            return c[0];
        }

        public boolean test6(final boolean a, final boolean b, final boolean[] c) {
            System.out.println(b && (c[0] = a));
            return c[0];
        }

        public boolean test7(final boolean a, final boolean b, final boolean[] c) {
            System.out.println(b || (c[0] = a));
            return c[0];
        }

        public boolean test8(final boolean a, final boolean b, final boolean[] c) {
            System.out.println(b && a == (c[0] = b) && b && c[0]);
            return c[0];
        }
    }

    @SuppressWarnings({ "ConstantConditions", "UnusedDeclaration" })
    private static class I {
        private boolean[] c;

        public boolean test1(final boolean a, final boolean b) {
            System.out.println((b && a == (this.c[0] = b) && b) || !this.c[0]);
            return this.c[0];
        }

        public boolean test2(final boolean a, final boolean b) {
            System.out.println((b && a == (this.c[0] = b)) || !this.c[0]);
            return this.c[0];
        }

        public boolean test3(final boolean a, final boolean b) {
            System.out.println((b && a) || (this.c[0] = b) || !this.c[0]);
            return this.c[0];
        }

        public boolean test4(final boolean a, final boolean b) {
            System.out.println((b && (this.c[0] = a)) || !this.c[0]);
            return this.c[0];
        }

        public boolean test5(final boolean a, final boolean b) {
            System.out.println(b || (this.c[0] = a) || !this.c[0]);
            return this.c[0];
        }

        public boolean test6(final boolean a, final boolean b) {
            System.out.println(b && (this.c[0] = a));
            return this.c[0];
        }

        public boolean test7(final boolean a, final boolean b) {
            System.out.println(b || (this.c[0] = a));
            return this.c[0];
        }

        public boolean test8(final boolean a, final boolean b) {
            System.out.println(b && a == (this.c[0] = b) && b && this.c[0]);
            return this.c[0];
        }
    }

    @SuppressWarnings({ "ConstantConditions", "UnusedDeclaration" })
    private static class J {
        private static boolean[] c;

        public boolean test1(final boolean a, final boolean b) {
            System.out.println((b && a == (J.c[0] = b) && b) || !J.c[0]);
            return J.c[0];
        }

        public boolean test2(final boolean a, final boolean b) {
            System.out.println((b && a == (J.c[0] = b)) || !J.c[0]);
            return J.c[0];
        }

        public boolean test3(final boolean a, final boolean b) {
            System.out.println((b && a) || (J.c[0] = b) || !J.c[0]);
            return J.c[0];
        }

        public boolean test4(final boolean a, final boolean b) {
            System.out.println((b && (J.c[0] = a)) || !J.c[0]);
            return J.c[0];
        }

        public boolean test5(final boolean a, final boolean b) {
            System.out.println(b || (J.c[0] = a) || !J.c[0]);
            return J.c[0];
        }

        public boolean test6(final boolean a, final boolean b) {
            System.out.println(b && (J.c[0] = a));
            return J.c[0];
        }

        public boolean test7(final boolean a, final boolean b) {
            System.out.println(b || (J.c[0] = a));
            return J.c[0];
        }

        public boolean test8(final boolean a, final boolean b) {
            System.out.println(b && a == (J.c[0] = b) && b && J.c[0]);
            return J.c[0];
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private static class K {
        private static int i;
        private int j;

        private static int index() {
            return 0;
        }

        public boolean test1(final boolean a, final boolean b, final boolean[] c) {
            System.out.println((a && c[0] == (c[0] = b) && b) || !c[0]);
            return c[0];
        }

        public boolean test2(final boolean a, final boolean b, final boolean[] c) {
            System.out.println((b && a == (c[0] = !c[0])) || !c[0]);
            return c[0];
        }

        public boolean test3(final boolean a, final boolean b, final boolean[] c) {
            System.out.println((a && c[index()] == (c[index()] = b) && b) || !c[index()]);
            return c[index()];
        }

        public boolean test4(final boolean a, final boolean b, final boolean[] c) {
            System.out.println((b && a == (c[index()] = !c[index()])) || !c[index()]);
            return c[index()];
        }

        public boolean test5(final boolean a, final boolean b, final boolean[] c) {
            System.out.println((a && c[index() + K.i] == (c[index() + this.j] = b) && b) || !c[index() + this.j]);
            return c[index() + K.i];
        }

        public boolean test6(final boolean a, final boolean b, final boolean[] c) {
            System.out.println((b && a == (c[index() + K.i] = !c[index() + this.j])) || !c[index() + this.j]);
            return c[index() + K.i];
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static class L {
        public static void test(final int a) {
            if (a == 0) {
            }

            if (a == 1) {
                System.getProperty("something");
            }
        }
    }

    @Test
    public void testComplexIfElse() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A {\n" +
            "    public boolean test(final List<Object> list, final Set<Object> set) {\n" +
            "        if (list == null) {\n" +
            "            if (set == null) {\n" +
            "                System.out.println(\"a\");\n" +
            "            }\n" +
            "            else {\n" +
            "                System.out.println(\"b\");\n" +
            "            }\n" +
            "        }\n" +
            "        else if (set == null) {\n" +
            "            System.out.println(\"c\");\n" +
            "        }\n" +
            "        else if (list.isEmpty()) {\n" +
            "            if (set.isEmpty()) {\n" +
            "                System.out.println(\"d\");\n" +
            "            }\n" +
            "            else {\n" +
            "                System.out.println(\"e\");\n" +
            "            }\n" +
            "        }\n" +
            "        else if (set.size() < list.size()) {\n" +
            "            System.out.println(\"f\");\n" +
            "        }\n" +
            "        else {\n" +
            "            System.out.println(\"g\");\n" +
            "        }\n" +
            "        return true;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEmptyElseBlocks() throws Throwable {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static class B {\n" +
            "    public boolean test(final List<Object> list, final Set<Object> set) {\n" +
            "        if (list == null) {\n" +
            "            if (set == null) {\n" +
            "                System.out.println(\"B\");\n" +
            "            }\n" +
            "        }\n" +
            "        else if (set == null && list.isEmpty()) {\n" +
            "            System.out.println(\"E\");\n" +
            "        }\n" +
            "        return true;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testShortCircuitEmbeddedAssignments() throws Throwable {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private static class C {\n" +
            "    public boolean test1(final boolean a, final boolean b, boolean c) {\n" +
            "        System.out.println((b && a == (c = b) && b) || !c);\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test2(final boolean a, final boolean b, boolean c) {\n" +
            "        System.out.println((b && a == (c = b)) || !c);\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test3(final boolean a, final boolean b, boolean c) {\n" +
            "        System.out.println((b && a) || (c = b) || !c);\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test4(final boolean a, final boolean b, boolean c) {\n" +
            "        System.out.println((b && (c = a)) || !c);\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test5(final boolean a, final boolean b, boolean c) {\n" +
            "        System.out.println(b || (c = a) || !c);\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test6(final boolean a, final boolean b, boolean c) {\n" +
            "        System.out.println(b && (c = a));\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test7(final boolean a, final boolean b, boolean c) {\n" +
            "        System.out.println(b || (c = a));\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test8(final boolean a, final boolean b, boolean c) {\n" +
            "        System.out.println(b && a == (c = b) && b && c);\n" +
            "        return c;\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testTernaryWithTernaryCondition() throws Throwable {
        verifyOutput(
            D.class,
            defaultSettings(),
            "private static class D {\n" +
            "    public boolean test(final boolean a, final boolean b, final boolean c, final boolean d) {\n" +
            "        return (a ? b : c) ? d : (c ? b : a);\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testLogicalAndOrWithConditionals() throws Throwable {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private static class E {\n" +
            "    public boolean test1(final boolean a, final boolean b, final boolean c) {\n" +
            "        System.out.println(a || (c ? a : b));\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test2(final boolean a, final boolean b, final boolean c) {\n" +
            "        System.out.println(a && (c ? a : b));\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test3(final boolean a, final boolean b, final boolean c) {\n" +
            "        System.out.println(!a || (c ? a : b));\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test4(final boolean a, final boolean b, final boolean c) {\n" +
            "        System.out.println(!a && (c ? a : b));\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test5(final boolean a, final boolean b, final boolean c) {\n" +
            "        System.out.println(a && (c ? (b ? a : c) : (b ? c : a)));\n" +
            "        return c;\n" +
            "    }\n" +
            "    public boolean test6(final boolean a, final boolean b, final boolean c) {\n" +
            "        System.out.println(a || (c ? (b ? a : c) : (b ? c : a)));\n" +
            "        return c;\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testShortCircuitEmbeddedInstanceFieldAssignments() throws Throwable {
        verifyOutput(
            F.class,
            defaultSettings(),
            "private static class F {\n" +
            "    private boolean c;\n" +
            "    public boolean test1(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a == (this.c = b) && b) || !this.c);\n" +
            "        return this.c;\n" +
            "    }\n" +
            "    public boolean test2(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a == (this.c = b)) || !this.c);\n" +
            "        return this.c;\n" +
            "    }\n" +
            "    public boolean test3(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a) || (this.c = b) || !this.c);\n" +
            "        return this.c;\n" +
            "    }\n" +
            "    public boolean test4(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && (this.c = a)) || !this.c);\n" +
            "        return this.c;\n" +
            "    }\n" +
            "    public boolean test5(final boolean a, final boolean b) {\n" +
            "        System.out.println(b || (this.c = a) || !this.c);\n" +
            "        return this.c;\n" +
            "    }\n" +
            "    public boolean test6(final boolean a, final boolean b) {\n" +
            "        System.out.println(b && (this.c = a));\n" +
            "        return this.c;\n" +
            "    }\n" +
            "    public boolean test7(final boolean a, final boolean b) {\n" +
            "        System.out.println(b || (this.c = a));\n" +
            "        return this.c;\n" +
            "    }\n" +
            "    public boolean test8(final boolean a, final boolean b) {\n" +
            "        System.out.println(b && a == (this.c = b) && b && this.c);\n" +
            "        return this.c;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testShortCircuitEmbeddedStaticFieldAssignments() throws Throwable {
        verifyOutput(
            G.class,
            defaultSettings(),
            "private static class G {\n" +
            "    private static boolean c;\n" +
            "    public boolean test1(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a == (G.c = b) && b) || !G.c);\n" +
            "        return G.c;\n" +
            "    }\n" +
            "    public boolean test2(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a == (G.c = b)) || !G.c);\n" +
            "        return G.c;\n" +
            "    }\n" +
            "    public boolean test3(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a) || (G.c = b) || !G.c);\n" +
            "        return G.c;\n" +
            "    }\n" +
            "    public boolean test4(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && (G.c = a)) || !G.c);\n" +
            "        return G.c;\n" +
            "    }\n" +
            "    public boolean test5(final boolean a, final boolean b) {\n" +
            "        System.out.println(b || (G.c = a) || !G.c);\n" +
            "        return G.c;\n" +
            "    }\n" +
            "    public boolean test6(final boolean a, final boolean b) {\n" +
            "        System.out.println(b && (G.c = a));\n" +
            "        return G.c;\n" +
            "    }\n" +
            "    public boolean test7(final boolean a, final boolean b) {\n" +
            "        System.out.println(b || (G.c = a));\n" +
            "        return G.c;\n" +
            "    }\n" +
            "    public boolean test8(final boolean a, final boolean b) {\n" +
            "        System.out.println(b && a == (G.c = b) && b && G.c);\n" +
            "        return G.c;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testShortCircuitEmbeddedArrayAssignments() throws Throwable {
        verifyOutput(
            H.class,
            defaultSettings(),
            "private static class H {\n" +
            "    public boolean test1(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println((b && a == (c[0] = b) && b) || !c[0]);\n" +
            "        return c[0];\n" +
            "    }\n" +
            "    public boolean test2(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println((b && a == (c[0] = b)) || !c[0]);\n" +
            "        return c[0];\n" +
            "    }\n" +
            "    public boolean test3(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println((b && a) || (c[0] = b) || !c[0]);\n" +
            "        return c[0];\n" +
            "    }\n" +
            "    public boolean test4(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println((b && (c[0] = a)) || !c[0]);\n" +
            "        return c[0];\n" +
            "    }\n" +
            "    public boolean test5(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println(b || (c[0] = a) || !c[0]);\n" +
            "        return c[0];\n" +
            "    }\n" +
            "    public boolean test6(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println(b && (c[0] = a));\n" +
            "        return c[0];\n" +
            "    }\n" +
            "    public boolean test7(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println(b || (c[0] = a));\n" +
            "        return c[0];\n" +
            "    }\n" +
            "    public boolean test8(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println(b && a == (c[0] = b) && b && c[0]);\n" +
            "        return c[0];\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testShortCircuitEmbeddedInstanceArrayAssignments() throws Throwable {
        verifyOutput(
            I.class,
            defaultSettings(),
            "private static class I {\n" +
            "    private boolean[] c;\n" +
            "    public boolean test1(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a == (this.c[0] = b) && b) || !this.c[0]);\n" +
            "        return this.c[0];\n" +
            "    }\n" +
            "    public boolean test2(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a == (this.c[0] = b)) || !this.c[0]);\n" +
            "        return this.c[0];\n" +
            "    }\n" +
            "    public boolean test3(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a) || (this.c[0] = b) || !this.c[0]);\n" +
            "        return this.c[0];\n" +
            "    }\n" +
            "    public boolean test4(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && (this.c[0] = a)) || !this.c[0]);\n" +
            "        return this.c[0];\n" +
            "    }\n" +
            "    public boolean test5(final boolean a, final boolean b) {\n" +
            "        System.out.println(b || (this.c[0] = a) || !this.c[0]);\n" +
            "        return this.c[0];\n" +
            "    }\n" +
            "    public boolean test6(final boolean a, final boolean b) {\n" +
            "        System.out.println(b && (this.c[0] = a));\n" +
            "        return this.c[0];\n" +
            "    }\n" +
            "    public boolean test7(final boolean a, final boolean b) {\n" +
            "        System.out.println(b || (this.c[0] = a));\n" +
            "        return this.c[0];\n" +
            "    }\n" +
            "    public boolean test8(final boolean a, final boolean b) {\n" +
            "        System.out.println(b && a == (this.c[0] = b) && b && this.c[0]);\n" +
            "        return this.c[0];\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testShortCircuitEmbeddedStaticArrayAssignments() throws Throwable {
        verifyOutput(
            J.class,
            defaultSettings(),
            "private static class J {\n" +
            "    private static boolean[] c;\n" +
            "    public boolean test1(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a == (J.c[0] = b) && b) || !J.c[0]);\n" +
            "        return J.c[0];\n" +
            "    }\n" +
            "    public boolean test2(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a == (J.c[0] = b)) || !J.c[0]);\n" +
            "        return J.c[0];\n" +
            "    }\n" +
            "    public boolean test3(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && a) || (J.c[0] = b) || !J.c[0]);\n" +
            "        return J.c[0];\n" +
            "    }\n" +
            "    public boolean test4(final boolean a, final boolean b) {\n" +
            "        System.out.println((b && (J.c[0] = a)) || !J.c[0]);\n" +
            "        return J.c[0];\n" +
            "    }\n" +
            "    public boolean test5(final boolean a, final boolean b) {\n" +
            "        System.out.println(b || (J.c[0] = a) || !J.c[0]);\n" +
            "        return J.c[0];\n" +
            "    }\n" +
            "    public boolean test6(final boolean a, final boolean b) {\n" +
            "        System.out.println(b && (J.c[0] = a));\n" +
            "        return J.c[0];\n" +
            "    }\n" +
            "    public boolean test7(final boolean a, final boolean b) {\n" +
            "        System.out.println(b || (J.c[0] = a));\n" +
            "        return J.c[0];\n" +
            "    }\n" +
            "    public boolean test8(final boolean a, final boolean b) {\n" +
            "        System.out.println(b && a == (J.c[0] = b) && b && J.c[0]);\n" +
            "        return J.c[0];\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testShortCircuitEmbeddedSelfReferencingArrayAssignments() throws Throwable {
        verifyOutput(
            K.class,
            defaultSettings(),
            "private static class K {\n" +
            "    private static int i;\n" +
            "    private int j;\n" +
            "    private static int index() {\n" +
            "        return 0;\n" +
            "    }\n" +
            "    public boolean test1(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println((a && c[0] == (c[0] = b) && b) || !c[0]);\n" +
            "        return c[0];\n" +
            "    }\n" +
            "    public boolean test2(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println((b && a == (c[0] = !c[0])) || !c[0]);\n" +
            "        return c[0];\n" +
            "    }\n" +
            "    public boolean test3(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println((a && c[index()] == (c[index()] = b) && b) || !c[index()]);\n" +
            "        return c[index()];\n" +
            "    }\n" +
            "    public boolean test4(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println((b && a == (c[index()] = !c[index()])) || !c[index()]);\n" +
            "        return c[index()];\n" +
            "    }\n" +
            "    public boolean test5(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println((a && c[index() + K.i] == (c[index() + this.j] = b) && b) || !c[index() + this.j]);\n" +
            "        return c[index() + K.i];\n" +
            "    }\n" +
            "    public boolean test6(final boolean a, final boolean b, final boolean[] c) {\n" +
            "        System.out.println((b && a == (c[index() + K.i] = !c[index() + this.j])) || !c[index() + this.j]);\n" +
            "        return c[index() + K.i];\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEmptyIfFollowedByRegularIf() throws Throwable {
        verifyOutput(
            L.class,
            defaultSettings(),
            "private static class L {\n" +
            "    public static void test(final int a) {\n" +
            "        if (a == 0) {}\n" +
            "        if (a == 1) {\n" +
            "            System.getProperty(\"something\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }
}
