/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang.invoke;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

public class MethodHandles {

    public static Lookup lookup() { return null; }

    public static Lookup publicLookup() { return null; }

    public static <T extends Member> T
    reflectAs(Class<T> expected, MethodHandle target) { return null; }

    public static final
    class Lookup {
        public static final int PUBLIC = 0;

        public static final int PRIVATE = 0;

        public static final int PROTECTED = 0;

        public static final int PACKAGE =  0;

        public Class<?> lookupClass() { return null; }

        public int lookupModes() { return 0; }

        public Lookup in(Class<?> requestedLookupClass) { return null; }

        public
        MethodHandle findStatic(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException { return null; }

        public MethodHandle findVirtual(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException { return null; }

        public MethodHandle findConstructor(Class<?> refc, MethodType type) throws NoSuchMethodException, IllegalAccessException { return null; }

        public MethodHandle findSpecial(Class<?> refc, String name, MethodType type,
                                        Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException { return null; }

        public MethodHandle findGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException { return null; }

        public MethodHandle findSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException { return null; }

        public MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException { return null; }

        public MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException { return null; }

        public MethodHandle bind(Object receiver, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException { return null; }

        public MethodHandle unreflect(Method m) throws IllegalAccessException { return null; }

        public MethodHandle unreflectSpecial(Method m, Class<?> specialCaller) throws IllegalAccessException { return null; }

        public MethodHandle unreflectConstructor(Constructor<?> c) throws IllegalAccessException { return null; }

        public MethodHandle unreflectGetter(Field f) throws IllegalAccessException { return null; }

        public MethodHandle unreflectSetter(Field f) throws IllegalAccessException { return null; }

        public MethodHandleInfo revealDirect(MethodHandle target) { return null; }

    }

    public static
    MethodHandle arrayElementGetter(Class<?> arrayClass) throws IllegalArgumentException { return null; }

    public static
    MethodHandle arrayElementSetter(Class<?> arrayClass) throws IllegalArgumentException { return null; }

    static public
    MethodHandle spreadInvoker(MethodType type, int leadingArgCount) { return null; }

    static public
    MethodHandle exactInvoker(MethodType type) { return null; }

    static public
    MethodHandle invoker(MethodType type) { return null; }

    public static
    MethodHandle explicitCastArguments(MethodHandle target, MethodType newType) { return null; }

    public static
    MethodHandle permuteArguments(MethodHandle target, MethodType newType, int... reorder) { return null; }

    public static
    MethodHandle constant(Class<?> type, Object value) { return null; }

    public static
    MethodHandle identity(Class<?> type) { return null; }

    public static
    MethodHandle insertArguments(MethodHandle target, int pos, Object... values) { return null; }

    public static
    MethodHandle dropArguments(MethodHandle target, int pos, List<Class<?>> valueTypes) { return null; }

    public static
    MethodHandle dropArguments(MethodHandle target, int pos, Class<?>... valueTypes) { return null; }

    public static
    MethodHandle filterArguments(MethodHandle target, int pos, MethodHandle... filters) { return null; }

    public static
    MethodHandle collectArguments(MethodHandle target, int pos, MethodHandle filter) { return null; }

    public static
    MethodHandle filterReturnValue(MethodHandle target, MethodHandle filter) { return null; }

    public static
    MethodHandle foldArguments(MethodHandle target, MethodHandle combiner) { return null; }

    public static
    MethodHandle guardWithTest(MethodHandle test,
                               MethodHandle target,
                               MethodHandle fallback) { return null; }

    public static
    MethodHandle catchException(MethodHandle target,
                                Class<? extends Throwable> exType,
                                MethodHandle handler) { return null; }

    public static
    MethodHandle throwException(Class<?> returnType, Class<? extends Throwable> exType) { return null; }
}
