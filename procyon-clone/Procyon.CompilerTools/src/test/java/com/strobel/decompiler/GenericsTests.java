package com.strobel.decompiler;

import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

@SuppressWarnings("unchecked")
public class GenericsTests extends DecompilerTest {
    private static class A {
        public static <T> void sort1(final List<T> list, final Comparator<? super T> comparator) {
            final Object[] a = list.toArray();
            Arrays.sort(a, (Comparator) comparator);
            final ListIterator i = list.listIterator();
            for (int j=0; j<a.length; j++) {
                i.next();
                i.set(a[j]);
            }
        }

        public static <T> void sort2(final List<T> list, final Comparator<? super T> comparator) {
            final Object[] a = list.toArray();
            Arrays.sort(a, (Comparator) comparator);
            final ListIterator<T> i = list.listIterator();
            for (int j=0; j<a.length; j++) {
                i.next();
                i.set((T)a[j]);
            }
        }
    }

    @Test
    public void testMixingGenericAndRawTypes() throws Throwable {
        verifyOutput(
            A.class,
            defaultSettings(),
            "private static class A {\n" +
            "    public static <T> void sort1(final List<T> list, final Comparator<? super T> comparator) {\n" +
            "        final Object[] a = list.toArray();\n" +
            "        Arrays.sort(a, (Comparator<? super Object>)comparator);\n" +
            "        final ListIterator i = list.listIterator();\n" +
            "        for (int j = 0; j < a.length; ++j) {\n" +
            "            i.next();\n" +
            "            i.set(a[j]);\n" +
            "        }\n" +
            "    }\n" +
            "    public static <T> void sort2(final List<T> list, final Comparator<? super T> comparator) {\n" +
            "        final Object[] a = list.toArray();\n" +
            "        Arrays.sort(a, (Comparator<? super Object>)comparator);\n" +
            "        final ListIterator<T> i = list.listIterator();\n" +
            "        for (int j = 0; j < a.length; ++j) {\n" +
            "            i.next();\n" +
            "            i.set((T)a[j]);\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );

        verifyOutput(
            A.class,
            createSettings(OPTION_RETAIN_CASTS | OPTION_EXPLICIT_TYPE_ARGUMENTS),
            "private static class A {\n" +
            "    public static <T> void sort1(final List<T> list, final Comparator<? super T> comparator) {\n" +
            "        final Object[] a = list.toArray();\n" +
            "        Arrays.<Object>sort(a, (Comparator<? super Object>)comparator);\n" +
            "        final ListIterator i = list.listIterator();\n" +
            "        for (int j = 0; j < a.length; ++j) {\n" +
            "            i.next();\n" +
            "            i.set(a[j]);\n" +
            "        }\n" +
            "    }\n" +
            "    public static <T> void sort2(final List<T> list, final Comparator<? super T> comparator) {\n" +
            "        final Object[] a = list.toArray();\n" +
            "        Arrays.<Object>sort(a, (Comparator<? super Object>)comparator);\n" +
            "        final ListIterator<T> i = list.listIterator();\n" +
            "        for (int j = 0; j < a.length; ++j) {\n" +
            "            i.next();\n" +
            "            i.set((T)a[j]);\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }
}
