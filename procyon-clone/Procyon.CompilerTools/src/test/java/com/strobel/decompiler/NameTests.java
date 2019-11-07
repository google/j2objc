/*
 * NameTests.java
 *
 * Copyright (c) 2014 Mike Strobel
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

@SuppressWarnings("SuspiciousNameCombination")
public class NameTests extends DecompilerTest {
    final static class A {
        final static class Inner {
            void f(final int x, final Object Integer) {
                final x z = new x();
                System.out.println(x);
                System.out.println(z);
                System.out.println(A.x.y);
                System.out.println(Integer instanceof Integer);
            }
        }

        static class x {
            static int y;
        }
    }

    final static class B {
        void f(final Object o) {
            class Integer {}

            if (o instanceof Integer) {
                System.out.println(Integer.class);
            }
            else if (o instanceof B.Integer) {
                System.out.println(B.Integer.class);
            }
            else if (o instanceof java.lang.Integer) {
                System.out.println(java.lang.Integer.class);
            }
        }

        static class Integer {}
    }

    final static class C {
        static float Float = java.lang.Float.NaN;

        void f() {
            System.out.println(Float);
            System.out.println(java.lang.Float.NaN);
        }
    }

    final static class D {
        void f(final int x) {
            System.out.println(x * 2);
            System.out.println(D.x.y);
        }
        static class x {
            static int y;
        }
    }

    final static class E<x> {
        x x;
        void f(final x x) {
            this.x = x;
            System.out.println(x);
            System.out.println(this.x);
        }
    }

    final static class F {
        <x> void f(final x z) {
            System.out.println(z);
            System.out.println(F.x.y);
        }

        static class x {
            static int y;
        }
    }

    final static class G<x> {
        void f(final x z) {
            System.out.println(z);
            System.out.println(G.x.y);
        }

        static class x {
            static int y;
        }
    }

    static class DeclaresX {
        static class X {}
    }

    interface IDeclaresX {
        static class X {}
    }

    final static class H extends DeclaresX {
        X z;

        void f(final DeclaresX.X x) {
            System.out.println(x);
            System.out.println(z);
        }

        static class X {
        }
    }

    final static class I extends DeclaresX implements IDeclaresX {
        DeclaresX.X x;

        void f(final IDeclaresX.X z) {
            System.out.println(x);
            System.out.println(z);
        }
    }

    final static class J extends DeclaresX implements IDeclaresX {
        X z;

        void f(final DeclaresX.X x, final IDeclaresX.X y) {
            System.out.println(x);
            System.out.println(y);
            System.out.println(z);
        }

        static class X {
        }
    }

    @Test
    public void testVariablesHideClasses() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "static final class A {\n" +
            "    static final class Inner {\n" +
            "        void f(final int x, final Object Integer) {\n" +
            "            final x z = new x();\n" +
            "            System.out.println(x);\n" +
            "            System.out.println(z);\n" +
            "            System.out.println(A.x.y);\n" +
            "            System.out.println(Integer instanceof Integer);\n" +
            "        }\n" +
            "    }\n" +
            "    static class x {\n" +
            "        static int y;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testLocalImportedAndNestedTypeNameCollisions() throws Throwable {
        verifyOutput(
            B.class,
            defaultSettings(),
            "static final class B {\n" +
            "    void f(final Object o) {\n" +
            "        class Integer {\n" +
            "        }\n" +
            "        if (o instanceof Integer) {\n" +
            "            System.out.println(Integer.class);\n" +
            "        }\n" +
            "        else if (o instanceof B.Integer) {\n" +
            "            System.out.println(B.Integer.class);\n" +
            "        }\n" +
            "        else if (o instanceof java.lang.Integer) {\n" +
            "            System.out.println(java.lang.Integer.class);\n" +
            "        }\n" +
            "    }\n" +
            "    static class Integer {\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testFieldHidesImportedTypeName() throws Throwable {
        verifyOutput(
            C.class,
            defaultSettings(),
            "static final class C {\n" +
            "    static float Float;\n" +
            "    void f() {\n" +
            "        System.out.println(C.Float);\n" +
            "        System.out.println(java.lang.Float.NaN);\n" +
            "    }\n" +
            "    static {\n" +
            "        C.Float = java.lang.Float.NaN;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testFormalParameterHidesNestedClass() throws Throwable {
        verifyOutput(
            D.class,
            defaultSettings(),
            "static final class D {\n" +
            "    void f(final int x) {\n" +
            "        System.out.println(x * 2);\n" +
            "        System.out.println(D.x.y);\n" +
            "    }\n" +
            "    static class x {\n" +
            "        static int y;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testFormalParameterHidesClassTypeParameter() throws Throwable {
        verifyOutput(
            E.class,
            defaultSettings(),
            "static final class E<x> {\n" +
            "    x x;\n" +
            "    void f(final x x) {\n" +
            "        this.x = x;\n" +
            "        System.out.println(x);\n" +
            "        System.out.println(this.x);\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testMethodTypeParameterHidesInnerClass() throws Throwable {
        verifyOutput(
            F.class,
            defaultSettings(),
            "static final class F {\n" +
            "    <x> void f(final x z) {\n" +
            "        System.out.println(z);\n" +
            "        System.out.println(F.x.y);\n" +
            "    }\n" +
            "    \n" +
            "    static class x {\n" +
            "        static int y;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testClassTypeParameterHidesInnerClass() throws Throwable {
        verifyOutput(
            G.class,
            defaultSettings(),
            "static final class G<x> {\n" +
            "    void f(final x z) {\n" +
            "        System.out.println(z);\n" +
            "        System.out.println(G.x.y);\n" +
            "    }\n" +
            "    \n" +
            "    static class x {\n" +
            "        static int y;\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testInnerClassHidesBaseInnerClass() throws Throwable {
        verifyOutput(
            H.class,
            defaultSettings(),
            "static final class H extends DeclaresX {\n" +
            "    X z;\n" +
            "    void f(final DeclaresX.X x) {\n" +
            "        System.out.println(x);\n" +
            "        System.out.println(this.z);\n" +
            "    }\n" +
            "    static class X {\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testBaseInnerClassAndInterfaceInnerClassCollision() throws Throwable {
        verifyOutput(
            I.class,
            defaultSettings(),
            "static final class I extends DeclaresX implements IDeclaresX {\n" +
            "    DeclaresX.X x;\n" +
            "    void f(final IDeclaresX.X z) {\n" +
            "        System.out.println(this.x);\n" +
            "        System.out.println(z);\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testInnerClassHidesBaseInnerClassAndInterfaceInnerClass() throws Throwable {
        verifyOutput(
            J.class,
            defaultSettings(),
            "static final class J extends DeclaresX implements IDeclaresX {\n" +
            "    X z;\n" +
            "    void f(final DeclaresX.X x, final IDeclaresX.X y) {\n" +
            "        System.out.println(x);\n" +
            "        System.out.println(y);\n" +
            "        System.out.println(this.z);\n" +
            "    }\n" +
            "    static class X {\n" +
            "    }\n" +
            "}\n"
        );
    }
}

