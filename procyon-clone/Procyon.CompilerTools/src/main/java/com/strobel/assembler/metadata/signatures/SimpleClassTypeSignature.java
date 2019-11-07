/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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

public final class SimpleClassTypeSignature implements FieldTypeSignature {
    private final boolean _dollar;
    private final String _name;
    private final TypeArgument[] _typeArguments;

    private SimpleClassTypeSignature(final String n, final boolean dollar, final TypeArgument[] tas) {
        _name = n;
        _dollar = dollar;
        _typeArguments = tas;
    }

    public static SimpleClassTypeSignature make(
        final String n,
        final boolean dollar,
        final TypeArgument[] tas) {
        return new SimpleClassTypeSignature(n, dollar, tas);
    }

    /*
     * Should a '$' be used instead of '.' to separate this component
     * of the name from the previous one when composing a string to
     * pass to Class.forName; in other words, is this a transition to
     * a nested class.
     */
    public boolean useDollar() {
        return _dollar;
    }

    public String getName() {
        return _name;
    }

    public TypeArgument[] getTypeArguments() {
        return _typeArguments;
    }

    public void accept(final TypeTreeVisitor<?> v) {
        v.visitSimpleClassTypeSignature(this);
    }
}
