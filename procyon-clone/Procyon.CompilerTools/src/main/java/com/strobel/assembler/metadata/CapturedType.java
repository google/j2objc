/*
 * CapturedType.java
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

import com.strobel.core.VerifyArgument;

public final class CapturedType extends TypeReference implements ICapturedType {
    private final TypeReference _superBound;
    private final TypeReference _extendsBound;
    private final WildcardType _wildcard;

    CapturedType(final TypeReference superBound, final TypeReference extendsBound, final WildcardType wildcard) {
        _superBound = superBound != null ? superBound : BuiltinTypes.Bottom;
        _extendsBound = extendsBound != null ? extendsBound : BuiltinTypes.Object;
        _wildcard = VerifyArgument.notNull(wildcard, "wildcard");
    }

    @Override
    public final WildcardType getWildcard() {
        return _wildcard;
    }

    @Override
    public final TypeReference getExtendsBound() {
        return _extendsBound;
    }

    @Override
    public final TypeReference getSuperBound() {
        return _superBound;
    }

    @Override
    public final boolean hasExtendsBound() {
        return _extendsBound != null &&
               !MetadataHelper.isSameType(_extendsBound, BuiltinTypes.Object);
    }

    @Override
    public final boolean hasSuperBound() {
        return _superBound != BuiltinTypes.Bottom;
    }

    @Override
    public final boolean isBoundedType() {
        return true;
    }

    @Override
    public String getSimpleName() {
        return "capture of " + _wildcard.getSimpleName();
    }

    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitCapturedType(this, parameter);
    }

    @Override
    protected final StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
       return _wildcard.appendName(sb.append("capture of "), fullName, dottedName);
    }
}
