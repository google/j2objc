/*
 * LoopTests.java
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoopTests extends DecompilerTest {
    private static final class StringList extends ArrayList<String> {}

    private static class A {
        public void test(final Iterable<String> items) {
            for (final String item : items) {
                System.out.println(item);
            }
        }
    }

    private static class B {
        public void test(final List<String> items) {
            for (final String item : items) {
                System.out.println(item);
            }
        }
    }

    private static class C {
        public void test(final StringList items) {
            for (final String item : items) {
                System.out.println(item);
            }
        }
    }

    private static class D {
        public void test(final String[] items) {
            for (final String item : items) {
                System.out.println(item);
            }
        }
    }

    private static class E {
        public void test(final List<String> items) {
            for (int n = items.size(), i = 0; i < n; i++) {
                final String item = items.get(i);
                System.out.println(item);
            }
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static class F {
        public void test(final String[] items) {
            for (int i = 0; i < items.length; i++) {
                final String item = items[i];
                System.out.println(item);
            }
        }
    }

    private static class G {
        public void test(final String[] items) {
            if (items == null || items.length == 0) {
                return;
            }

            int i = 0;

            do {
                System.out.println(items[i]);
                ++i;
            }
            while (i < items.length);
        }
    }

    @SuppressWarnings("LocalCanBeFinal")
    private static class H {
        public void test(final List<Integer> items) {
            for (int item : items) {
                System.out.println(item);
            }
        }
    }

    @SuppressWarnings({ "LocalCanBeFinal", "UnusedAssignment" })
    private static class I {
        public void canRedeclare(final List<List<Integer>> lists) {
            for (final List<Integer> list : lists) {
                Iterator<Integer> it;

                it = list.iterator();

                while (it.hasNext()) {
                    final Integer next = it.next();
                    System.out.println(next);
                }

                it = list.iterator();

                while (it.hasNext()) {
                    final Integer next = it.next();
                    System.out.println(next);
                }
            }
        }

        public void cannotRedeclare1(final List<List<Integer>> lists) {
            for (final List<Integer> list : lists) {
                Iterator<Integer> it = null;

                it = list.iterator();

                while (it.hasNext()) {
                    final Integer next = it.next();
                    System.out.println(next);
                }

                System.out.println(it);
                it = list.iterator();

                while (it.hasNext()) {
                    final Integer next = it.next();
                    System.out.println(next);
                }
            }
        }

        public void cannotRedeclare2(final List<Integer> list) {
            final Iterator<Integer> iterator = list.iterator();

            while (iterator.hasNext()) {
                Integer next = iterator.next();
                System.out.println(next);

                for (int i = 0; i < 10; i++) {
                    next = iterator.hasNext() ? iterator.next() : null;
                    System.out.println(next);
                }
            }
        }
    }

    @SuppressWarnings("WhileLoopReplaceableByForEach")
    private final static class J {
        public void itemVariableCanBeFinal(final List<Integer> list) {
            final Iterator<Integer> iterator = list.iterator();

            while (iterator.hasNext()) {
                final Integer next = iterator.next();
                System.out.println(next);
            }
        }

        public void itemVariableCannotBeFinal1(final List<Integer> list) {
            final Iterator<Integer> iterator = list.iterator();

            while (iterator.hasNext()) {
                Integer next = iterator.next();
                System.out.println(next);
                next = 5;
                System.out.println(next);
            }
        }

        public void itemVariableCannotBeFinal2(final List<Integer> list) {
            final Iterator<Integer> iterator = list.iterator();

            while (iterator.hasNext()) {
                Integer next = iterator.next();
                System.out.println(next);

                for (int i = 0; i < 10; i++) {
                    next = i;
                    System.out.println(next);
                }
            }
        }
    }

    private final static class K {
        public void cannotBeForLoop1() {
            int i = 0;

            while (true) {
                if (i >= 10) {
                    break;
                }
                if (i == 2) {
                    continue;
                }
                System.out.println(i);
                ++i;
            }
        }

        public void cannotBeForLoop2() {
            int i = 0;
            int j = 0;

            while (true) {
                if (i >= 10 || j >= 10) {
                    break;
                }
                if (i == 2) {
                    ++i;
                    continue;
                }
                System.out.println(i);
                System.out.println(j);
                ++i;
                ++j;
            }
        }

        public void canBeForLoop1() {
            int i = 0;

            while (true) {
                if (i >= 10) {
                    break;
                }
                if (i == 2) {
                    ++i;
                    continue;
                }
                System.out.println(i);
                ++i;
            }
        }

        public void canBeForLoop2() {
            for (int i = 0; i < 10; i++) {
                if (i == 2) {
                    continue;
                }
                System.out.println(i);
            }
        }

        public void canBeForLoop3() {
            int i = 0;

            while (true) {
                if (i >= 10) {
                    break;
                }
                if (i == 2) {
                    ++i;
                    continue;
                }
                System.out.println(i);
                ++i;
            }
        }

        public void canBeForLoop4() {
            int i = 0;
            int j = 0;

            while (true) {
                if (i >= 10 || j >= 10) {
                    break;
                }
                if (i == 2) {
                    ++j;
                    continue;
                }
                System.out.println(i);
                System.out.println(j);
                ++i;
                ++j;
            }
        }

        public void canBeForLoop5() {
            for (int i = 0; i < 10; i++) {
                if (i == 2) {
                    continue;
                }
                System.out.println(i);
            }
        }

        public void canBeForLoop6() {
            for (int i = 0, j = 0; i < 10 && j < 10; i++, j++) {
                if (i == 2) {
                    continue;
                }
                System.out.println(i);
                if (j == 2) {
                    continue;
                }
                System.out.println(j);
            }
        }
    }

    @SuppressWarnings("ParameterCanBeLocal")
    private final static class L {
        public void f(int size) {
            size = 10;
            for (int i = 0; i < size; ++i) {
                System.out.println(i);
            }
        }
    }

    @Test
    public void testEnhancedForInIterable() {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A\n" +
            "{\n" +
            "    public void test(final Iterable<String> items) {\n" +
            "        for (final String item : items) {\n" +
            "            System.out.println(item);\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnhancedForInList() {
        verifyOutput(
            B.class,
            defaultSettings(),
            "private static class B\n" +
            "{\n" +
            "    public void test(final List<String> items) {\n" +
            "        for (final String item : items) {\n" +
            "            System.out.println(item);\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnhancedForInNonGenericCollection() {
        verifyOutput(
            C.class,
            defaultSettings(),
            "private static class C\n" +
            "{\n" +
            "    public void test(final StringList items) {\n" +
            "        for (final String item : items) {\n" +
            "            System.out.println(item);\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testEnhancedForInArray() {
        verifyOutput(
            D.class,
            defaultSettings(),
            "private static class D\n" +
            "{\n" +
            "    public void test(final String[] items) {\n" +
            "        for (final String item : items) {\n" +
            "            System.out.println(item);\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testForWithList() {
        verifyOutput(
            E.class,
            defaultSettings(),
            "private static class E {\n" +
            "    public void test(final List<String> items) {\n" +
            "        for (int n = items.size(), i = 0; i < n; ++i) {\n" +
            "            final String item = items.get(i);\n" +
            "            System.out.println(item);\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testForWithArray() {
        verifyOutput(
            F.class,
            defaultSettings(),
            "private static class F {\n" +
            "    public void test(final String[] items) {\n" +
            "        for (int i = 0; i < items.length; ++i) {\n" +
            "            final String item = items[i];\n" +
            "            System.out.println(item);\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testDoWhileLoop() {
        verifyOutput(
            G.class,
            defaultSettings(),
            "private static class G {\n" +
            "    public void test(final String[] items) {\n" +
            "        if (items == null || items.length == 0) {\n" +
            "            return;\n" +
            "        }\n" +
            "        int i = 0;\n" +
            "        do {\n" +
            "            System.out.println(items[i]);\n" +
            "        }\n" +
            "        while (++i < items.length);\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testUnboxingEnhancedForLoop() {
        verifyOutput(
            H.class,
            defaultSettings(),
            "private static class H {\n" +
            "    public void test(final List<Integer> items) {\n" +
            "        for (final int item : items) {\n" +
            "            System.out.println(item);\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testIteratorVariableRedeclaration() {
        verifyOutput(
            I.class,
            defaultSettings(),
            "private static class I {\n" +
            "    public void canRedeclare(final List<List<Integer>> lists) {\n" +
            "        for (final List<Integer> list : lists) {\n" +
            "            for (final Integer next : list) {\n" +
            "                System.out.println(next);\n" +
            "            }\n" +
            "            for (final Integer next : list) {\n" +
            "                System.out.println(next);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    public void cannotRedeclare1(final List<List<Integer>> lists) {\n" +
            "        for (final List<Integer> list : lists) {\n" +
            "            Iterator<Integer> it = null;\n" +
            "            it = list.iterator();\n" +
            "            while (it.hasNext()) {\n" +
            "                final Integer next = it.next();\n" +
            "                System.out.println(next);\n" +
            "            }\n" +
            "            System.out.println(it);\n" +
            "            it = list.iterator();\n" +
            "            while (it.hasNext()) {\n" +
            "                final Integer next = it.next();\n" +
            "                System.out.println(next);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    public void cannotRedeclare2(final List<Integer> list) {\n" +
            "        final Iterator<Integer> iterator = list.iterator();\n" +
            "        while (iterator.hasNext()) {\n" +
            "            Integer next = iterator.next();\n" +
            "            System.out.println(next);\n" +
            "            for (int i = 0; i < 10; ++i) {\n" +
            "                next = (iterator.hasNext() ? iterator.next() : null);\n" +
            "                System.out.println(next);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testForEachVariableFinalAnalysis() {
        verifyOutput(
            J.class,
            defaultSettings(),
            "private static final class J {\n" +
            "        public void itemVariableCanBeFinal(final List<Integer> list) {\n" +
            "        for (final Integer next : list) {\n" +
            "            System.out.println(next);\n" +
            "        }\n" +
            "    }\n" +
            "    public void itemVariableCannotBeFinal1(final List<Integer> list) {\n" +
            "        for (Integer next : list) {\n" +
            "            System.out.println(next);\n" +
            "            next = 5;\n" +
            "            System.out.println(next);\n" +
            "        }\n" +
            "    }\n" +
            "    public void itemVariableCannotBeFinal2(final List<Integer> list) {\n" +
            "        for (Integer next : list) {\n" +
            "            System.out.println(next);\n" +
            "            for (int i = 0; i < 10; ++i) {\n" +
            "                next = i;\n" +
            "                System.out.println(next);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testTrickyForLoopTransforms() {
        verifyOutput(
            K.class,
            defaultSettings(),
            "private static final class K {\n" +
            "    public void cannotBeForLoop1() {\n" +
            "        int i = 0;\n" +
            "        while (i < 10) {\n" +
            "            if (i == 2) {\n" +
            "                continue;\n" +
            "            }\n" +
            "            System.out.println(i);\n" +
            "            ++i;\n" +
            "        }\n" +
            "    }\n" +
            "    public void cannotBeForLoop2() {\n" +
            "        int i = 0;\n" +
            "        int j = 0;\n" +
            "        while (i < 10 && j < 10) {\n" +
            "            if (i == 2) {\n" +
            "                ++i;\n" +
            "            }\n" +
            "            else {\n" +
            "                System.out.println(i);\n" +
            "                System.out.println(j);\n" +
            "                ++i;\n" +
            "                ++j;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    public void canBeForLoop1() {\n" +
            "        for (int i = 0; i < 10; ++i) {\n" +
            "            if (i != 2) {\n" +
            "                System.out.println(i);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    public void canBeForLoop2() {\n" +
            "        for (int i = 0; i < 10; ++i) {\n" +
            "            if (i != 2) {\n" +
            "                System.out.println(i);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    public void canBeForLoop3() {\n" +
            "        for (int i = 0; i < 10; ++i) {\n" +
            "            if (i != 2) {\n" +
            "                System.out.println(i);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    public void canBeForLoop4() {\n" +
            "        for (int i = 0, j = 0; i < 10 && j < 10; ++j) {\n" +
            "            if (i != 2) {\n" +
            "                System.out.println(i);\n" +
            "                System.out.println(j);\n" +
            "                ++i;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    public void canBeForLoop5() {\n" +
            "        for (int i = 0; i < 10; ++i) {\n" +
            "            if (i != 2) {\n" +
            "                System.out.println(i);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    public void canBeForLoop6() {\n" +
            "        for (int i = 0, j = 0; i < 10 && j < 10; ++i, ++j) {\n" +
            "            if (i != 2) {\n" +
            "                System.out.println(i);\n" +
            "                if (j != 2) {\n" +
            "                    System.out.println(j);\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

    @Test
    public void testParameterAssignmentNotMovedIntoForLoopInitializer() {
        verifyOutput(
            L.class,
            defaultSettings(),
            "private static final class L {\n" +
            "    public void f(int size) {\n" +
            "        size = 10;\n" +
            "        for (int i = 0; i < size; ++i) {\n" +
            "            System.out.println(i);\n" +
            "        }\n" +
            "    }\n" +
            "}"
        );
    }
}
