/*
 * EnhancedTryTests.java
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

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class EnhancedTryTests extends DecompilerTest {
    private static final class A {
        public void test() throws IOException {
            try (final StringWriter writer = new StringWriter()) {
                writer.write("This is only a test.");
            }
        }
    }

    private static final class B {
        public void test() throws IOException {
            try (final StringWriter writer1 = new StringWriter();
                 final StringWriter writer2 = new StringWriter()) {
                writer1.write("This is only a test.");
                writer2.write("This is also a test.");
            }
        }
    }

    private static final class C {
        public void test() throws IOException {
            try (final StringWriter writer = new StringWriter()) {
                writer.write("This is only a test.");
            }
            try (final StringWriter writer = new StringWriter()) {
                writer.write("This is also a test.");
            }
        }
    }

    private static final class D {
        public void test() throws IOException {
            try (final StringWriter writer1 = new StringWriter()) {
                writer1.write("This is only a test.");
                try (final StringWriter writer2 = new StringWriter()) {
                    writer2.write("This is also a test.");
                }
            }
        }
    }

    private static final class E {
        public void test() throws IOException {
            try (final StringWriter writer1 = new StringWriter()) {
                try (final StringWriter writer2 = new StringWriter()) {
                    writer1.write("This is only a test.");
                    writer2.write("This is also a test.");
                }
            }
        }
    }

    private static final class F {
        public void test() throws IOException {
            try (final StringWriter writer1 = new StringWriter();
                 final StringWriter writer2 = new StringWriter()) {
                writer1.write("This is only a test.");
                writer2.write("This is also a test.");
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static final class G {
        public void test() throws IOException {
            try (final StringWriter writer1 = new StringWriter();
                 final StringWriter writer2 = new StringWriter()) {
                writer1.write("This is only a test.");
                writer2.write("This is also a test.");
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private static final class H {
        public void test() throws IOException {
            try (final StringWriter writer1 = new StringWriter()) {
                try (final StringWriter writer2 = new StringWriter()) {
                    writer1.write("This is only a test.");
                    writer2.write("This is also a test.");
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    private static final class I {
        public void test() throws IOException {
            try (final StringWriter writer1 = new StringWriter()) {
                try (final StringWriter writer2 = new StringWriter()) {
                    writer1.write("This is only a test.");
                    writer2.write("This is also a test.");
                }
                catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final class J {
        public void test() throws IOException {
            try (final StringWriter writer1 = new StringWriter()) {
            }
        }
    }

    private static final class K {
        public void test() throws IOException {
            try (final StringWriter writer1 = new StringWriter();
                 final StringWriter writer2 = new StringWriter()) {
            }
        }
    }

    @Test
    public void testEnhancedTryOneResource() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static final class A {\n" +
            "    public void test() throws IOException {\n" +
            "        try (final StringWriter writer = new StringWriter()) {\n" +
            "            writer.write(\"This is only a test.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnhancedTryTwoResources() throws Throwable {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static final class B {\n" +
            "    public void test() throws IOException {\n" +
            "        try (final StringWriter writer1 = new StringWriter();\n" +
            "             final StringWriter writer2 = new StringWriter()) {\n" +
            "            writer1.write(\"This is only a test.\");\n" +
            "            writer2.write(\"This is also a test.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnhancedTryTwoSuccessive() throws Throwable {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private static final class C {\n" +
            "    public void test() throws IOException {\n" +
            "        try (final StringWriter writer = new StringWriter()) {\n" +
            "            writer.write(\"This is only a test.\");\n" +
            "        }\n" +
            "        try (final StringWriter writer = new StringWriter()) {\n" +
            "            writer.write(\"This is also a test.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnhancedTryTwoNestedWithIntermediateStatement() throws Throwable {
        verifyOutput(
            D.class,
            defaultSettings(),
            "private static final class D {\n" +
            "    public void test() throws IOException {\n" +
            "        try (final StringWriter writer1 = new StringWriter()) {\n" +
            "            writer1.write(\"This is only a test.\");\n" +
            "            try (final StringWriter writer2 = new StringWriter()) {\n" +
            "                writer2.write(\"This is also a test.\");\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnhancedTryTwoNested() throws Throwable {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private static final class E {\n" +
            "    public void test() throws IOException {\n" +
            "        try (final StringWriter writer1 = new StringWriter();" +
            "             final StringWriter writer2 = new StringWriter()) {\n" +
            "            writer1.write(\"This is only a test.\");\n" +
            "            writer2.write(\"This is also a test.\");\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnhancedTryTwoNestedOuterCatchesThrowable() throws Throwable {
        verifyOutput(
            F.class,
            defaultSettings(),
            "private static final class F {\n" +
            "    public void test() throws IOException {\n" +
            "        try (final StringWriter writer1 = new StringWriter();\n" +
            "             final StringWriter writer2 = new StringWriter()) {\n" +
            "            writer1.write(\"This is only a test.\");\n" +
            "            writer2.write(\"This is also a test.\");\n" +
            "        }\n" +
            "        catch (Throwable t) {\n" +
            "            t.printStackTrace();\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnhancedTryTwoNestedOuterCatchesRuntimeException() throws Throwable {
        verifyOutput(
            G.class,
            defaultSettings(),
            "private static final class G {\n" +
            "    public void test() throws IOException {\n" +
            "        try (final StringWriter writer1 = new StringWriter();\n" +
            "             final StringWriter writer2 = new StringWriter()) {\n" +
            "            writer1.write(\"This is only a test.\");\n" +
            "            writer2.write(\"This is also a test.\");\n" +
            "        }\n" +
            "        catch (RuntimeException e) {\n" +
            "            e.printStackTrace();\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnhancedTryTwoNestedInnerCatchesThrowable() throws Throwable {
        verifyOutput(
            H.class,
            defaultSettings(),
            "private static final class H {\n" +
            "    public void test() throws IOException {\n" +
            "        try (final StringWriter writer1 = new StringWriter()) {\n" +
            "            try (final StringWriter writer2 = new StringWriter()) {\n" +
            "                writer1.write(\"This is only a test.\");\n" +
            "                writer2.write(\"This is also a test.\");\n" +
            "            }\n" +
            "            catch (Throwable t) {\n" +
            "                t.printStackTrace();\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnhancedTryTwoNestedInnerCatchesRuntimeException() throws Throwable {
        verifyOutput(
            I.class,
            defaultSettings(),
            "private static final class I {\n" +
            "    public void test() throws IOException {\n" +
            "        try (final StringWriter writer1 = new StringWriter()) {\n" +
            "            try (final StringWriter writer2 = new StringWriter()) {\n" +
            "                writer1.write(\"This is only a test.\");\n" +
            "                writer2.write(\"This is also a test.\");\n" +
            "            }\n" +
            "            catch (RuntimeException e) {\n" +
            "                e.printStackTrace();\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    @Ignore
    public void testEnhancedTryEmptyBody() throws Throwable {
        verifyOutput(
            J.class,
            defaultSettings(),
            "private static final class J {\n" +
            "    public void test() throws IOException {\n" +
            "        try (final StringWriter writer1 = new StringWriter()) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    @Ignore
    public void testEnhancedTryTwoResourcesEmptyBody() throws Throwable {
        verifyOutput(
            K.class,
            defaultSettings(),
            "private static final class K {\n" +
            "    public void test() throws IOException {\n" +
            "        try (final StringWriter writer1 = new StringWriter();\n" +
            "             final StringWriter writer2 = new StringWriter()) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }
}
