package com.strobel.decompiler;

import org.junit.Test;

@SuppressWarnings("UnusedDeclaration")
public class SyncTests extends DecompilerTest {
    private static class A {
        public String test(final Object o) {
            synchronized (o) {
                return "";
            }
        }
    }

    private static class B {
        public String test(final Object o, final Object p) {
            synchronized (o) {
                synchronized (p) {
                    return "";
                }
            }
        }
    }

    private static class C {
        public String test(final Object o, final Object p, final Object q) {
            synchronized (o) {
                synchronized (p) {
                    synchronized (q) {
                        return "";
                    }
                }
            }
        }
    }

    private static class D {
        public String test(final Object o, final Object p, final Object q, final Object r) {
            synchronized (o) {
                synchronized (p) {
                    synchronized (q) {
                        synchronized (r) {
                            return "";
                        }
                    }
                }
            }
        }
    }

    private static class E {
        public String test(final Object o, final Object p, final Object q, final Object r) {
            final String result;

            synchronized (o) {
                System.out.println("enter(o)");
                synchronized (p) {
                    System.out.println("enter(p)");
                    synchronized (q) {
                        System.out.println("enter(q)");
                        synchronized (r) {
                            System.out.println("enter(r)");
                            result = "";
                            System.out.println("exit(r)");
                        }
                        System.out.println("exit(q)");
                    }
                    System.out.println("exit(p)");
                }
                System.out.println("exit(o)");
            }

            return result;
        }
    }

    private static class F {
        Object x;
        Object y;
        Object z;

        public String test() {
            synchronized (this.x) {
                synchronized (this.y) {
                    synchronized (this.z) {
                        return "";
                    }
                }
            }
        }
    }

    @Test
    public void testSimpleSynchronized() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A {\n" +
            "    public String test(final Object o) {\n" +
            "        synchronized (o) {\n" +
            "            return \"\";\n" +
            "        }\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testTwoTightlyNestedSynchronized() throws Throwable {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static class B {\n" +
            "    public String test(final Object o, final Object p) {\n" +
            "        synchronized (o) {\n" +
            "            synchronized (p) {\n" +
            "                return \"\";\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testThreeTightlyNestedSynchronized() throws Throwable {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private static class C {\n" +
            "    public String test(final Object o, final Object p, final Object q) {\n" +
            "        synchronized (o) {\n" +
            "            synchronized (p) {\n" +
            "                synchronized (q) {\n" +
            "                    return \"\";\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testFourTightlyNestedSynchronized() throws Throwable {
        verifyOutput(
            D.class,
            defaultSettings(),
            "private static class D {\n" +
            "    public String test(final Object o, final Object p, final Object q, final Object r) {\n" +
            "        synchronized (o) {\n" +
            "            synchronized (p) {\n" +
            "                synchronized (q) {\n" +
            "                    synchronized (r) {\n" +
            "                        return \"\";\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testFourLooselyNestedSynchronized() throws Throwable {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private static class E {\n" +
            "    public String test(final Object o, final Object p, final Object q, final Object r) {\n" +
            "        final String result;\n" +
            "        synchronized (o) {\n" +
            "            System.out.println(\"enter(o)\");\n" +
            "            synchronized (p) {\n" +
            "                System.out.println(\"enter(p)\");\n" +
            "                synchronized (q) {\n" +
            "                    System.out.println(\"enter(q)\");\n" +
            "                    synchronized (r) {\n" +
            "                        System.out.println(\"enter(r)\");\n" +
            "                        result = \"\";\n" +
            "                        System.out.println(\"exit(r)\");\n" +
            "                    }\n" +
            "                    System.out.println(\"exit(q)\");\n" +
            "                }\n" +
            "                System.out.println(\"exit(p)\");\n" +
            "            }\n" +
            "            System.out.println(\"exit(o)\");\n" +
            "        }\n" +
            "        return result;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testNestedSynchronizeOnFields() throws Throwable {
        verifyOutput(
            F.class,
            defaultSettings(),
            "private static class F {\n" +
            "    Object x;\n" +
            "    Object y;\n" +
            "    Object z;\n" +
            "    public String test() {\n" +
            "        synchronized (this.x) {\n" +
            "            synchronized (this.y) {\n" +
            "                synchronized (this.z) {\n" +
            "                    return \"\";\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"
        );
    }
}
