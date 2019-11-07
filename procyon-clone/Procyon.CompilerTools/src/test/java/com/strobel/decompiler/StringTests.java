package com.strobel.decompiler;

import org.junit.Test;

public class StringTests extends DecompilerTest {
    private static class A {
        public String test() {
            return "\0\b\f\t\r\n\"";
        }
    }

    @Test
    public void testStringEscaping() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A {\n" +
            "    public String test() {\n" +
            "        return \"\\u0000\\b\\f\\t\\r\\n\\\"\";\n" +
            "    }\n" +
            "}\n"
        );
    }
}
