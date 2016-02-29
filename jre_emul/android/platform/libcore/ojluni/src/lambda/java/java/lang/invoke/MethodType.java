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

import java.util.List;

public final
class MethodType implements java.io.Serializable {

    public static
    MethodType methodType(Class<?> rtype, Class<?>[] ptypes) {
        return null;
    }

    public static
    MethodType methodType(Class<?> rtype, List<Class<?>> ptypes) {
        return null;
    }

    public static
    MethodType methodType(Class<?> rtype, Class<?> ptype0, Class<?>... ptypes) { return null; }

    public static
    MethodType methodType(Class<?> rtype) { return null; }

    public static
    MethodType methodType(Class<?> rtype, Class<?> ptype0) { return null; }

    public static
    MethodType methodType(Class<?> rtype, MethodType ptypes) { return null; }

    public static
    MethodType genericMethodType(int objectArgCount, boolean finalArray) { return null; }

    public static
    MethodType genericMethodType(int objectArgCount) { return null; }

    public MethodType changeParameterType(int num, Class<?> nptype) { return null; }

    public MethodType insertParameterTypes(int num, Class<?>... ptypesToInsert) { return null; }

    public MethodType appendParameterTypes(Class<?>... ptypesToInsert) { return null; }

    public MethodType insertParameterTypes(int num, List<Class<?>> ptypesToInsert) { return null; }

    public MethodType appendParameterTypes(List<Class<?>> ptypesToInsert) { return null; }

    public MethodType dropParameterTypes(int start, int end) { return null; }

    public MethodType changeReturnType(Class<?> nrtype) { return null; }

    public boolean hasPrimitives() { return false; }

    public boolean hasWrappers() { return false; }

    public MethodType erase() { return null; }

    public MethodType generic() { return null; }

    public MethodType wrap() { return null; }

    public MethodType unwrap() { return null; }

    public Class<?> parameterType(int num) { return null; }

    public int parameterCount() { return 0; }

    public Class<?> returnType() { return null; }

    public List<Class<?>> parameterList() { return null; }

    public Class<?>[] parameterArray() { return null; }

    public static MethodType fromMethodDescriptorString(String descriptor, ClassLoader loader)
        throws IllegalArgumentException, TypeNotPresentException { return null; }

    public String toMethodDescriptorString() { return null; }

}
