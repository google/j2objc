/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Member;

public
interface MethodHandleInfo {
    public static final int REF_getField                = 0;
    public static final int REF_getStatic               = 0;
    public static final int REF_putField                = 0;
    public static final int REF_putStatic               = 0;
    public static final int REF_invokeVirtual           = 0;
    public static final int REF_invokeStatic            = 0;
    public static final int REF_invokeSpecial           = 0;
    public static final int REF_newInvokeSpecial        = 0;
    public static final int REF_invokeInterface         = 0;

    public int getReferenceKind();

    public Class<?> getDeclaringClass();

    public String getName();

    public MethodType getMethodType();

    public <T extends Member> T reflectAs(Class<T> expected, Lookup lookup);

    public int getModifiers();

    public default boolean isVarArgs()  { return false; }

    public static String referenceKindToString(int referenceKind) { return null; }

    public static String toString(int kind, Class<?> defc, String name, MethodType type) {
        return null;
    }
}
