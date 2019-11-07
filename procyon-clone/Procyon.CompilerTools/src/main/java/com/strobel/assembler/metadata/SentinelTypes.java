/*
 * SentinelTypes.java
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

final class BottomType extends TypeDefinition {
    final static BottomType INSTANCE = new BottomType();

    private BottomType() {
        setName("__Bottom");
    }

    @Override
    public String getSimpleName() {
        return "__Bottom";
    }

    @Override
    public String getFullName() {
        return getSimpleName();
    }

    @Override
    public String getInternalName() {
        return getSimpleName();
    }

    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitBottomType(this, parameter);
    }
}

final class NullType extends TypeDefinition {
    final static NullType INSTANCE = new NullType();

    private NullType() {
        setName("__Null");
    }

    @Override
    public String getSimpleName() {
        return "__Null";
    }

    @Override
    public String getFullName() {
        return getSimpleName();
    }

    @Override
    public String getInternalName() {
        return getSimpleName();
    }

    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitNullType(this, parameter);
    }
}
