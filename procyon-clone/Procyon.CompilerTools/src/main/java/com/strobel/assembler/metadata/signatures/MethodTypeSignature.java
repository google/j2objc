/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
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

package com.strobel.assembler.metadata.signatures;

public final class MethodTypeSignature implements Signature {
    private final FormalTypeParameter[] formalTypeParams;
    private final TypeSignature[] parameterTypes;
    private final ReturnType returnType;
    private final FieldTypeSignature[] exceptionTypes;

    private MethodTypeSignature(
        final FormalTypeParameter[] ftps,
        final TypeSignature[] pts,
        final ReturnType rt,
        final FieldTypeSignature[] ets) {

        formalTypeParams = ftps;
        parameterTypes = pts;
        returnType = rt;
        exceptionTypes = ets;
    }

    public static MethodTypeSignature make(
        final FormalTypeParameter[] ftps,
        final TypeSignature[] pts,
        final ReturnType rt,
        final FieldTypeSignature[] ets) {
        return new MethodTypeSignature(ftps, pts, rt, ets);
    }

    public FormalTypeParameter[] getFormalTypeParameters() {
        return formalTypeParams;
    }

    public TypeSignature[] getParameterTypes() {
        return parameterTypes;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public FieldTypeSignature[] getExceptionTypes() {
        return exceptionTypes;
    }

    public void accept(final Visitor v) {
        v.visitMethodTypeSignature(this);
    }
}
