/*
 * DefaultTypeVisitor.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.assembler.metadata;

public abstract class DefaultTypeVisitor<P, R> implements TypeMetadataVisitor<P, R> {
    public R visit(final TypeReference t) {
        return visit(t, null);
    }

    public R visit(final TypeReference t, final P p) {
        return t.accept(this, p);
    }

    @Override
    public R visitType(final TypeReference t, final P p) {
        return null;
    }

    @Override
    public R visitArrayType(final ArrayType t, final P p) {
        return visitType(t, p);
    }

    @Override
    public R visitBottomType(final TypeReference t, final P p) {
        return visitType(t, p);
    }

    @Override
    public R visitClassType(final TypeReference t, final P p) {
        return visitType(t, p);
    }

    @Override
    public R visitCompoundType(final CompoundTypeReference t, final P p) {
        return visitType(t, p);
    }

    @Override
    public R visitGenericParameter(final GenericParameter t, final P p) {
        return visitType(t, p);
    }

    @Override
    public R visitNullType(final TypeReference t, final P p) {
        return visitType(t, p);
    }

    @Override
    public R visitParameterizedType(final TypeReference t, final P p) {
        return visitClassType(t, p);
    }

    @Override
    public R visitPrimitiveType(final PrimitiveType t, final P p) {
        return visitType(t, p);
    }

    @Override
    public R visitRawType(final RawType t, final P p) {
        return visitClassType(t, p);
    }

    @Override
    public R visitWildcard(final WildcardType t, final P p) {
        return visitType(t, p);
    }

    @Override
    public R visitCapturedType(final CapturedType t, final P p) {
        return visitType(t, p);
    }
}
