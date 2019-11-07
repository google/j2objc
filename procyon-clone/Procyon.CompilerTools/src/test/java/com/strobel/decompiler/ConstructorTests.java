package com.strobel.decompiler;

import org.junit.Test;

public class ConstructorTests extends DecompilerTest {
    private @interface IX {}

    private static final class A {
        public <T> A() {
        }
    }

    private static final class B {
        @IX
        public B() {
        }
    }

    private static final class C {
        public C() throws Exception {
        }
    }

    private static final class D {
        private static final class D1 {
            D1() {
            }
        }

        private static final class D2 {
            protected D2() {
            }
        }

        private static final class D3 {
            public D3() {
            }
        }

        protected static final class D4 {
            private D4() {
            }
        }

        protected static final class D5 {
            D5() {
            }
        }

        protected static final class D6 {
            public D6() {
            }
        }

        public static final class D7 {
            private D7() {
            }
        }

        public static final class D8 {
            D8() {
            }
        }

        public static final class D9 {
            protected D9() {
            }
        }
    }

    private static final class E {
        private static final class E1 {
            private E1() {
            }
        }

        static final class E2 {
            E2() {
            }
        }

        protected static final class E3 {
            protected E3() {
            }
        }

        public static final class E4 {
            public E4() {
            }
        }
    }

    @Test
    public void testEmptyConstructorWithTypeArgumentNotRemoved() throws Exception {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static final class A {\n" +
            "    public <T> A() {\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEmptyConstructorWithAnnotationNotRemoved() throws Exception {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static final class B {\n" +
            "    @IX\n" +
            "    public B() {\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEmptyConstructorWithThrowsSignatureNotRemoved() throws Exception {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private static final class C {\n" +
            "    public C() throws Exception {\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEmptyConstructorWithAccessModifierClashNotRemoved() throws Exception {
        verifyOutput(
            D.class,
            defaultSettings(),
            "private static final class D {\n" +
            "    private static final class D1 {\n" +
            "        D1() {\n" +
            "        }\n" +
            "    }\n" +
            "    private static final class D2 {\n" +
            "        protected D2() {\n" +
            "        }\n" +
            "    }\n" +
            "    private static final class D3 {\n" +
            "        public D3() {\n" +
            "        }\n" +
            "    }\n" +
            "    protected static final class D4 {\n" +
            "        private D4() {\n" +
            "        }\n" +
            "    }\n" +
            "    protected static final class D5 {\n" +
            "        D5() {\n" +
            "        }\n" +
            "    }\n" +
            "    protected static final class D6 {\n" +
            "        public D6() {\n" +
            "        }\n" +
            "    }\n" +
            "    public static final class D7 {\n" +
            "        private D7() {\n" +
            "        }\n" +
            "    }\n" +
            "    public static final class D8 {\n" +
            "        D8() {\n" +
            "        }\n" +
            "    }\n" +
            "    public static final class D9 {\n" +
            "        protected D9() {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testDefaultConstructorsRemoved() throws Exception {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private static final class E {\n" +
            "    private static final class E1 {\n" +
            "    }\n" +
            "    static final class E2 {\n" +
            "    }\n" +
            "    protected static final class E3 {\n" +
            "    }\n" +
            "    public static final class E4 {\n" +
            "    }\n" +
            "}\n"
        );
    }
}
