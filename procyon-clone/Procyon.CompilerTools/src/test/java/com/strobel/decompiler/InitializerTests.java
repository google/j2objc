package com.strobel.decompiler;

import org.junit.Test;

public class InitializerTests extends DecompilerTest {
    private static class A {
        static final int c = 42;
        final int i = 42;
    }

    @Test
    public void testConstantNotInitializedInConstructor() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A {\n" +
            "    static final int c = 42;\n" +
            "    final int i = 42;\n" +
            "}\n"
        );
    }
}
