/*
 * RawType.java
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

/**
 * @author Mike Strobel
 */
public final class RawType extends TypeReference {
    private final TypeReference _genericTypeDefinition;

    public RawType(final TypeReference genericTypeDefinition) {
        _genericTypeDefinition = VerifyArgument.notNull(genericTypeDefinition, "genericTypeDefinition");
    }

    @Override
    public String getFullName() {
        return _genericTypeDefinition.getFullName();
    }

    @Override
    public String getInternalName() {
        return _genericTypeDefinition.getInternalName();
    }

    @Override
    public TypeReference getDeclaringType() {
        return _genericTypeDefinition.getDeclaringType();
    }

    @Override
    public String getSimpleName() {
        return _genericTypeDefinition.getSimpleName();
    }

    @Override
    public String getPackageName() {
        return _genericTypeDefinition.getPackageName();
    }

    @Override
    public String getName() {
        return _genericTypeDefinition.getName();
    }

    @Override
    public TypeReference getUnderlyingType() {
        return _genericTypeDefinition;
    }

    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitRawType(this, parameter);
    }

    @Override
    public TypeDefinition resolve() {
        return getUnderlyingType().resolve();
    }
}
