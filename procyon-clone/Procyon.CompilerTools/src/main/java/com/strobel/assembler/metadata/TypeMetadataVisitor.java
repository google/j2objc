/*
 * TypeMetadataVisitor.java
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

/**
 * @author Mike Strobel
 */
public interface TypeMetadataVisitor<P, R> {
    R visitType(final TypeReference t, final P p);
    R visitArrayType(final ArrayType t, final P p);
    R visitGenericParameter(final GenericParameter t, final P p);
    R visitWildcard(final WildcardType t, final P p);
    R visitCapturedType(final CapturedType t, final P p);
    R visitCompoundType(final CompoundTypeReference t, final P p);
    R visitParameterizedType(final TypeReference t, final P p);
    R visitPrimitiveType(final PrimitiveType t, final P p);
    R visitClassType(final TypeReference t, final P p);
    R visitNullType(final TypeReference t, final P p);
    R visitBottomType(final TypeReference t, final P p);
    R visitRawType(final RawType t, final P p);
}
