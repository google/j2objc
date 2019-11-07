package com.strobel.decompiler;

import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

@SuppressWarnings({ "UnnecessarySemicolon", "UnusedDeclaration", "UnnecessaryEnumModifier" })
public class EnumTests extends DecompilerTest {
    private enum A {
        FOO,
        BAR,
        BAP;
    }

    private enum B {
        FOO(2),
        BAR(1),
        BAZ(5);

        private final int x;
        private static final Map<String, B> map;
        private static final B temp;

        private B(final int x) {
            this.x = x;
        }

        public static void test() {
            System.out.println(B.FOO);
        }

        static {
            map = Collections.emptyMap();
            temp = B.BAZ;
        }
    }

    private enum C {
        X {
            public void f() {
                System.out.println(this.name().toLowerCase());
            }
        },
        Y {
            public void f() {
                System.out.println("y");
            }
        };

        public abstract void f();
    }

    private enum D implements Comparator<String> {
        ORDINAL {
            @Override
            public int compare(final String s1, final String s2) {
                if (s1 == null) {
                    return s2 == null ? 0 : -1;
                }
                if (s2 == null) {
                    return 1;
                }
                return s1.compareTo(s2);
            }
        },
        ORDINAL_IGNORE_CASE {
            @Override
            public int compare(final String s1, final String s2) {
                if (s1 == null) {
                    return s2 == null ? 0 : -1;
                }
                if (s2 == null) {
                    return 1;
                }
                return s1.compareToIgnoreCase(s2);
            }
        };
    }

    private enum E {
        VALUE(1) {
            {
                System.out.println(this + ": " + this.code);
            }
        };

        public final int code;

        private E(final int code) {
            this.code = code;
        }
    }

    private enum F {
        VALUE("one") {
            {
                String w = "w";
                System.out.println(w);
            }

        };

        F(String a) {
        }
    }

    @Test
    public void testSimpleEnum() {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private enum A {\n" +
            "    FOO,\n" +
            "    BAR,\n" +
            "    BAP;\n" +
            "}\n"
        );
    }

    @Test
    public void testEnumWithFieldsAndConstructor() {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private enum B {\n" +
            "    FOO(2),\n" +
            "    BAR(1),\n" +
            "    BAZ(5);\n" +
            "    private final int x;\n" +
            "    private static final Map<String, B> map;\n" +
            "    private static final B temp;\n" +
            "    private B(final int x) {\n" +
            "        this.x = x;\n" +
            "    }\n" +
            "    public static void test() {\n" +
            "        System.out.println(B.FOO);\n" +
            "    }\n" +
            "    static {\n" +
            "        map = Collections.emptyMap();\n" +
            "        temp = B.BAZ;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnumWithAnonymousClassValues() {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private enum C {\n" +
            "    X {\n" +
            "        @Override\n" +
            "        public void f() {\n" +
            "            System.out.println(this.name().toLowerCase());\n" +
            "        }\n" +
            "    },\n" +
            "    Y {\n" +
            "        @Override\n" +
            "        public void f() {\n" +
            "            System.out.println(\"y\");\n" +
            "        }\n" +
            "    };\n" +
            "    public abstract void f();\n" +
            "}\n"
        );
    }

    @Test
    public void testEnumImplementingInterface() {
        verifyOutput(
            D.class,
            defaultSettings(),
            "private enum D implements Comparator<String> {\n" +
            "    ORDINAL {\n" +
            "        @Override\n" +
            "        public int compare(final String s1, final String s2) {\n" +
            "            if (s1 == null) {\n" +
            "                return (s2 == null) ? 0 : -1;\n" +
            "            }\n" +
            "            if (s2 == null) {\n" +
            "                return 1;\n" +
            "            }\n" +
            "            return s1.compareTo(s2);\n" +
            "        }\n" +
            "    }, \n" +
            "    ORDINAL_IGNORE_CASE {\n" +
            "        @Override\n" +
            "        public int compare(final String s1, final String s2) {\n" +
            "            if (s1 == null) {\n" +
            "                return (s2 == null) ? 0 : -1;\n" +
            "            }\n" +
            "            if (s2 == null) {\n" +
            "                return 1;\n" +
            "            }\n" +
            "            return s1.compareToIgnoreCase(s2);\n" +
            "        }\n" +
            "    };\n" +
            "}"
        );
    }

    @Test
    public void testEnumValueWithInitializerBlock() {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private enum E {\n" +
            "    VALUE(1) {\n" +
            "        {\n" +
            "            System.out.println(this + \": \" + this.code);\n" +
            "        }\n" +
            "    };\n" +
            "    public final int code;\n" +
            "    private E(final int code) {\n" +
            "        this.code = code;\n" +
            "    }\n" +
            "}"
        );
    }

    @Test
    public void testEnumInitializerVariableDeclaredAfterSuperCall() {
        verifyOutput(
            F.class,
            defaultSettings(),
            "private enum F {\n" +
            "    VALUE(\"one\") {\n" +
            "        {\n" +
            "            final String w = \"w\";\n" +
            "            System.out.println(w);\n" +
            "        }\n" +
            "    };\n" +
            "    private F(final String a) {\n" +
            "    }\n" +
            "}\n"
        );
    }
}
