package com.strobel.decompiler;

import com.strobel.assembler.Collection;
import org.junit.Test;

public class ImportTests extends DecompilerTest {
    @SuppressWarnings("UnusedDeclaration")
    private abstract class A implements java.util.Collection<String> {
        private Collection<String> innerCollection;
    }

    @Test
    public void testImportCollision() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private abstract class A implements Collection<String> {\n" +
            "    private com.strobel.assembler.Collection<String> innerCollection;\n" +
            "}\n"
        );
    }
}
